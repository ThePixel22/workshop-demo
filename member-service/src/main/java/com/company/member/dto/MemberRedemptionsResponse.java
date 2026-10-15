package com.company.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRedemptionsResponse {
    private Long memberId;
    private List<RedemptionSummaryDTO> redemptions;
}