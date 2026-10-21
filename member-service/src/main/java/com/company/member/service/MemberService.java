package com.company.member.service;

import com.company.member.client.RedemptionServiceClient;
import com.company.member.dto.CreateMemberRequest;
import com.company.member.dto.CreateMemberResponse;
import com.company.member.dto.FreeNightBalanceResponse;
import com.company.member.dto.MemberRedemptionsResponse;
import com.company.member.dto.MemberResponse;
import com.company.member.dto.RedemptionSummaryDTO;
import com.company.member.dto.UpdateBalanceRequest;
import com.company.member.dto.UpdateBalanceResponse;
import com.company.member.elasticsearch.document.MemberDocument;
import com.company.member.elasticsearch.mapper.MemberDocumentMapper;
import com.company.member.elasticsearch.repository.MemberSearchRepository;
import com.company.member.entity.Member;
import com.company.member.entity.NightBalance;
import com.company.member.exception.DuplicateEmailException;
import com.company.member.exception.FreeNightBalanceNotFoundException;
import com.company.member.exception.InsufficientBalanceException;
import com.company.member.exception.InvalidEmailFormatException;
import com.company.member.exception.InvalidMemberIdException;
import com.company.member.exception.MemberCreationException;
import com.company.member.exception.MemberNotFoundException;
import com.company.member.repository.MemberRepository;
import com.company.member.repository.NightBalanceRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final NightBalanceRepository nightBalanceRepository;
    private final RedemptionServiceClient redemptionServiceClient;

    private final MemberSearchRepository memberSearchRepository;
    private final MemberDocumentMapper memberDocumentMapper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Transactional
    public CreateMemberResponse createMember(CreateMemberRequest request) {
        // Validate email format
        if (!isValidEmail(request.getEmail())) {
            throw new InvalidEmailFormatException("Email must be in a valid email format");
        }

        // Check if email already exists
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }

        try {
            // Create Member
            Member member = new Member();
            member.setName(request.getName());
            member.setEmail(request.getEmail());
            member.setTier(request.getTier());
            member.setStatus(request.getStatus());

            Member savedMember = memberRepository.save(member);

            // Create NightBalance with initial 5 available nights
            NightBalance nightBalance = new NightBalance();
            nightBalance.setMemberId(savedMember.getMemberId());
            nightBalance.setAvailableNights(5);

            nightBalanceRepository.save(nightBalance);

            MemberDocument memberDocument = memberDocumentMapper.toDocument(savedMember);
            memberSearchRepository.save(memberDocument);

            return new CreateMemberResponse(savedMember.getMemberId());
        } catch (Exception e) {
            throw new MemberCreationException("Failed to create member: " + e.getMessage(), e);
        }
    }

    public MemberResponse getMemberById(Long memberId) {
        // Validate member ID is positive
        if (memberId == null || memberId <= 0) {
            throw new InvalidMemberIdException("Member ID must be a valid positive number");
        }

        // Find member or throw exception
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with ID: " + memberId));

        // Convert to response DTO
        return new MemberResponse(
                member.getMemberId(),
                member.getName(),
                member.getEmail(),
                member.getTier(),
                member.getStatus()
        );
    }

    public FreeNightBalanceResponse getFreeNightBalance(Long memberId) {
        // Validate member ID is positive
        if (memberId == null || memberId <= 0) {
            throw new InvalidMemberIdException("Member ID must be a valid positive number");
        }

        // Check if member exists
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException("Member not found with ID: " + memberId);
        }

        // Find free night balance or throw exception
        NightBalance nightBalance = nightBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new FreeNightBalanceNotFoundException(
                        "Free night balance not found for member ID: " + memberId
                ));

        // Convert to response DTO
        return new FreeNightBalanceResponse(
                nightBalance.getMemberId(),
                nightBalance.getAvailableNights()
        );
    }

    @Transactional
    public UpdateBalanceResponse updateBalance(Long memberId, UpdateBalanceRequest request) {
        // Validate member exists
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException("Member not found with ID: " + memberId);
        }

        // Find night balance
        NightBalance nightBalance = nightBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new FreeNightBalanceNotFoundException(
                        "Free night balance not found for member ID: " + memberId
                ));

        // Check if sufficient balance
        if (nightBalance.getAvailableNights() < request.getNights()) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Available: %d, Required: %d",
                            nightBalance.getAvailableNights(), request.getNights())
            );
        }

        // Deduct nights
        int newBalance = nightBalance.getAvailableNights() - request.getNights();
        nightBalance.setAvailableNights(newBalance);
        nightBalanceRepository.save(nightBalance);

        log.info("Balance updated for member {}: {} -> {}", memberId,
                nightBalance.getAvailableNights() + request.getNights(), newBalance);

        return new UpdateBalanceResponse(memberId, newBalance);
    }

    public MemberRedemptionsResponse getMemberRedemptions(Long memberId) {
        // Validate member ID is positive
        if (memberId == null || memberId <= 0) {
            throw new InvalidMemberIdException("Member ID must be a valid positive number");
        }

        // Check if member exists
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException("Member not found with ID: " + memberId);
        }

        try {
            // Call Redemption Service to get redemptions
            List<RedemptionSummaryDTO> redemptions = redemptionServiceClient.getRedemptionsByMemberId(memberId);
            log.info("Retrieved {} redemptions for member {}", redemptions.size(), memberId);

            return new MemberRedemptionsResponse(memberId, redemptions);

        } catch (FeignException.NotFound e) {
            // Redemption service returns empty list if no redemptions
            log.info("No redemptions found for member {}", memberId);
            return new MemberRedemptionsResponse(memberId, List.of());
        } catch (FeignException e) {
            log.error("Error calling Redemption Service: {}", e.getMessage());
            throw new RuntimeException("Error retrieving redemptions: " + e.getMessage(), e);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}