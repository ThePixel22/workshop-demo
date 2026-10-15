package com.company.redemption.dto;

import com.company.redemption.entity.RedemptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteRedemptionResponse {
    private Long redemptionId;
    private RedemptionStatus status;
}