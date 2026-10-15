package com.company.redemption.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreeNightBalanceResponse {
    private Long memberId;
    private Integer balance;
}