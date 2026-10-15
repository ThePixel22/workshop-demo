package com.company.redemption.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberValidationResponse {
    private Long memberId;
    private String name;
    private String email;
    private String tier;
    private String status;
}