package com.company.redemption.dto;

import com.company.redemption.entity.RedemptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedemptionSummaryDTO {
    private Long redemptionId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer nights;
    private RedemptionStatus status;
}