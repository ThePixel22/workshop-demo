package com.company.member.dto;

import com.company.member.entity.MembershipTier;
import com.company.member.entity.MemberStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMemberRequest {

    @NotBlank(message = "Member name is mandatory")
    private String name;

    @NotBlank(message = "Member email is mandatory")
    @Email(message = "Email must be in a valid email format")
    private String email;

    @NotNull(message = "Membership tier is mandatory")
    private MembershipTier tier;

    @NotNull(message = "Member status is mandatory")
    private MemberStatus status;
}