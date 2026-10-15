package com.company.redemption.client;

import com.company.redemption.dto.FreeNightBalanceResponse;
import com.company.redemption.dto.MemberValidationResponse;
import com.company.redemption.dto.UpdateBalanceRequest;
import com.company.redemption.dto.UpdateBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "member-service", url = "${member.service.url}")
public interface MemberServiceClient {

    @GetMapping("/api/members/{memberId}")
    MemberValidationResponse getMember(@PathVariable("memberId") Long memberId);

    @GetMapping("/api/members/{memberId}/free-night-balance")
    FreeNightBalanceResponse getFreeNightBalance(@PathVariable("memberId") Long memberId);

    @PutMapping("/api/members/{memberId}/balance")
    UpdateBalanceResponse updateBalance(
            @PathVariable("memberId") Long memberId,
            @RequestBody UpdateBalanceRequest request
    );
}