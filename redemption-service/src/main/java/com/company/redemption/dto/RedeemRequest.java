package com.company.redemption.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RedeemRequest {

    @NotNull(message = "Member ID is mandatory")
    @Min(value = 1, message = "Member ID must be a valid positive number")
    private Long memberId;

    @NotBlank(message = "Hotel name is mandatory")
    private String hotelName;

    @NotNull(message = "Check-in date is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of nights is mandatory")
    @Min(value = 1, message = "Nights must be at least 1")
    private Integer nights;
}