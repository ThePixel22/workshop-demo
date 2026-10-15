package com.company.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "night_balance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NightBalance {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "available_nights", nullable = false)
    private Integer availableNights = 5; // Initial balance: 5 nights
}