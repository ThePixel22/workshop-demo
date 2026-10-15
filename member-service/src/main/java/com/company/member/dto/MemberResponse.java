package com.company.member.dto;

import com.company.member.entity.MembershipTier;
import com.company.member.entity.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long memberId;
    private String name;
    private String email;
    private MembershipTier tier;
    private MemberStatus status;
}
