package com.company.redemption.dto;

import com.company.redemption.entity.RedemptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedemptionResponse {
    private Long redemptionId;
    private Long memberId;
    private String hotelName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer nights;
    private RedemptionStatus status;
}