package com.company.redemption.controller;

import com.company.redemption.dto.*;
import com.company.redemption.service.RedemptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/redemptions")
@RequiredArgsConstructor
public class RedemptionController {

    private final RedemptionService redemptionService;

    @PostMapping
    public ResponseEntity<RedeemResponse> redeemHotelStay(@Valid @RequestBody RedeemRequest request) {
        RedeemResponse response = redemptionService.redeemHotelStay(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{redemptionId}")
    public ResponseEntity<RedemptionResponse> getRedemptionById(@PathVariable Long redemptionId) {
        RedemptionResponse response = redemptionService.getRedemptionById(redemptionId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/{redemptionId}/complete")
    public ResponseEntity<CompleteRedemptionResponse> completeRedemption(@PathVariable Long redemptionId) {
        CompleteRedemptionResponse response = redemptionService.completeRedemption(redemptionId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<RedemptionSummaryDTO>> getRedemptionsByMemberId(@PathVariable Long memberId) {
        List<RedemptionSummaryDTO> response = redemptionService.getRedemptionsByMemberId(memberId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}