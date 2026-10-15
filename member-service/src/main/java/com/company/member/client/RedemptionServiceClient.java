package com.company.member.client;

import com.company.member.dto.RedemptionSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "redemption-service", url = "${redemption.service.url}")
public interface RedemptionServiceClient {

    @GetMapping("/api/redemptions/member/{memberId}")
    List<RedemptionSummaryDTO> getRedemptionsByMemberId(@PathVariable("memberId") Long memberId);
}