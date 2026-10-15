package com.company.redemption.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "redemption_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedemptionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "redemption_id")
    private Long redemptionId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "hotel_name", nullable = false, length = 100)
    private String hotelName;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "nights", nullable = false)
    private Integer nights;

    @Column(name = "redemption_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RedemptionStatus status;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (status == null) {
            status = RedemptionStatus.PENDING;
        }
    }
}
