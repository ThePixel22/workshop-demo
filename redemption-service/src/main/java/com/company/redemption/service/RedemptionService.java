package com.company.redemption.service;

import com.company.redemption.client.MemberServiceClient;
import com.company.redemption.dto.*;
import com.company.redemption.entity.RedemptionRequest;
import com.company.redemption.entity.RedemptionStatus;
import com.company.redemption.exception.*;
import com.company.redemption.repository.RedemptionRequestRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedemptionService {

    private final RedemptionRequestRepository redemptionRepository;
    private final MemberServiceClient memberServiceClient;

    @Transactional
    public RedeemResponse redeemHotelStay(RedeemRequest request) {
        log.info("Processing redemption request for memberId: {}", request.getMemberId());

        // 1. Validate member exists by calling Member Service
        validateMember(request.getMemberId());

        // 2. Validate date range (check-out must be after check-in)
        validateDateRange(request.getCheckInDate(), request.getCheckOutDate());

        // 3. Validate nights match the period
        validateNightsMatch(request.getCheckInDate(), request.getCheckOutDate(), request.getNights());

        // 4. Check for overlapping stays
        checkOverlappingStays(request);

        // 5. Check free night balance (but don't deduct yet)
        checkFreeNightBalance(request.getMemberId(), request.getNights());

        // 6. Create redemption request with PENDING status
        RedemptionRequest redemption = createRedemptionRequest(request);

        // 7. Save and return response
        RedemptionRequest savedRedemption = redemptionRepository.save(redemption);
        log.info("Redemption request created with id: {}", savedRedemption.getRedemptionId());

        return convertToRedeemResponse(savedRedemption);
    }

    public RedemptionResponse getRedemptionById(Long redemptionId) {
        log.info("Fetching redemption request with id: {}", redemptionId);

        RedemptionRequest redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new RedemptionNotFoundException(
                        "Redemption request not found with ID: " + redemptionId
                ));

        log.info("Redemption request found: {}", redemption);
        return convertToRedemptionResponse(redemption);
    }

    @Transactional
    public CompleteRedemptionResponse completeRedemption(Long redemptionId) {
        log.info("Completing redemption request with id: {}", redemptionId);

        // 1. Find redemption request
        RedemptionRequest redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new RedemptionNotFoundException(
                        "Redemption request not found with ID: " + redemptionId
                ));

        // 2. Check if status is PENDING
        if (redemption.getStatus() != RedemptionStatus.PENDING) {
            throw new InvalidRedemptionStatusException(
                    String.format("Redemption request with ID %d has status %s. Only PENDING requests can be completed.",
                            redemptionId, redemption.getStatus())
            );
        }

        // 3. Update balance in Member Service
        try {
            UpdateBalanceRequest balanceRequest = new UpdateBalanceRequest(
                    redemption.getMemberId(),
                    redemption.getNights()
            );

            UpdateBalanceResponse balanceResponse = memberServiceClient.updateBalance(
                    redemption.getMemberId(),
                    balanceRequest
            );

            log.info("Balance updated successfully for member {}: new balance = {}",
                    redemption.getMemberId(), balanceResponse.getNewBalance());

        } catch (FeignException e) {
            throw new BalanceUpdateException(
                    "Failed to update member balance: " + e.getMessage(), e
            );
        }

        // 4. Update redemption status to COMPLETED
        redemption.setStatus(RedemptionStatus.COMPLETED);
        RedemptionRequest updatedRedemption = redemptionRepository.save(redemption);

        log.info("Redemption request {} completed successfully", redemptionId);

        return new CompleteRedemptionResponse(
                updatedRedemption.getRedemptionId(),
                updatedRedemption.getStatus()
        );
    }

    private void validateMember(Long memberId) {
        try {
            MemberValidationResponse member = memberServiceClient.getMember(memberId);
            if (member == null) {
                throw new MemberNotFoundException("Member not found with ID: " + memberId);
            }
            log.info("Member validated: {}", member);
        } catch (FeignException.NotFound e) {
            throw new MemberNotFoundException("Member not found with ID: " + memberId);
        } catch (FeignException e) {
            throw new RuntimeException("Error validating member: " + e.getMessage(), e);
        }
    }

    private void validateDateRange(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.equals(checkInDate)) {
            throw new InvalidDateRangeException(
                    "Check-out date must be after the check-in date"
            );
        }
    }

    private void validateNightsMatch(LocalDate checkInDate, LocalDate checkOutDate, Integer requestedNights) {
        long calculatedNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (calculatedNights != requestedNights) {
            throw new InvalidDateRangeException(
                    String.format("Number of redemption nights (%d) does not match the hotel stay period (%d nights)",
                            requestedNights, calculatedNights)
            );
        }
    }

    private void checkOverlappingStays(RedeemRequest request) {
        List<RedemptionStatus> statuses = Arrays.asList(
                RedemptionStatus.PENDING,
                RedemptionStatus.COMPLETED
        );

        List<RedemptionRequest> overlappingRequests = redemptionRepository.findOverlappingRequests(
                request.getMemberId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                statuses
        );

        if (!overlappingRequests.isEmpty()) {
            throw new OverlappingStayException(
                    String.format("Member has overlapping hotel stay from %s to %s",
                            overlappingRequests.get(0).getCheckInDate(),
                            overlappingRequests.get(0).getCheckOutDate())
            );
        }
    }

    private void checkFreeNightBalance(Long memberId, Integer nights) {
        try {
            FreeNightBalanceResponse balance = memberServiceClient.getFreeNightBalance(memberId);

            if (balance == null || balance.getBalance() == null) {
                throw new RuntimeException("Could not retrieve free night balance");
            }

            if (balance.getBalance() < nights) {
                throw new InsufficientBalanceException(
                        String.format("Insufficient free night balance. Available: %d, Required: %d",
                                balance.getBalance(), nights)
                );
            }

            log.info("Member {} has sufficient balance: {}", memberId, balance.getBalance());

        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Free night balance not found for member: " + memberId);
        } catch (FeignException e) {
            throw new RuntimeException("Error checking balance: " + e.getMessage(), e);
        }
    }

    private RedemptionRequest createRedemptionRequest(RedeemRequest request) {
        RedemptionRequest redemption = new RedemptionRequest();
        redemption.setMemberId(request.getMemberId());
        redemption.setHotelName(request.getHotelName());
        redemption.setCheckInDate(request.getCheckInDate());
        redemption.setCheckOutDate(request.getCheckOutDate());
        redemption.setNights(request.getNights());
        redemption.setStatus(RedemptionStatus.PENDING);
        return redemption;
    }

    public List<RedemptionSummaryDTO> getRedemptionsByMemberId(Long memberId) {
        log.info("Fetching redemptions for memberId: {}", memberId);

        List<RedemptionRequest> redemptions = redemptionRepository.findByMemberId(memberId);

        // Sort by checkInDate descending
        redemptions.sort((r1, r2) -> r2.getCheckInDate().compareTo(r1.getCheckInDate()));

        return redemptions.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    private RedemptionSummaryDTO convertToSummaryDTO(RedemptionRequest redemption) {
        return new RedemptionSummaryDTO(
                redemption.getRedemptionId(),
                redemption.getCheckInDate(),
                redemption.getCheckOutDate(),
                redemption.getNights(),
                redemption.getStatus()
        );
    }

    private RedeemResponse convertToRedeemResponse(RedemptionRequest redemption) {
        return new RedeemResponse(
                redemption.getRedemptionId(),
                redemption.getMemberId(),
                redemption.getHotelName(),
                redemption.getCheckInDate(),
                redemption.getCheckOutDate(),
                redemption.getNights(),
                redemption.getStatus()
        );
    }

    private RedemptionResponse convertToRedemptionResponse(RedemptionRequest redemption) {
        return new RedemptionResponse(
                redemption.getRedemptionId(),
                redemption.getMemberId(),
                redemption.getHotelName(),
                redemption.getCheckInDate(),
                redemption.getCheckOutDate(),
                redemption.getNights(),
                redemption.getStatus()
        );
    }
}