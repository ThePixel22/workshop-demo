package com.company.redemption.repository;

import com.company.redemption.entity.RedemptionRequest;
import com.company.redemption.entity.RedemptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RedemptionRequestRepository extends JpaRepository<RedemptionRequest, Long> {

    @Query("SELECT r FROM RedemptionRequest r WHERE r.memberId = :memberId " +
            "AND r.status IN :statuses " +
            "AND r.checkInDate < :checkOutDate " +
            "AND r.checkOutDate > :checkInDate")
    List<RedemptionRequest> findOverlappingRequests(
            @Param("memberId") Long memberId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("statuses") List<RedemptionStatus> statuses
    );

    List<RedemptionRequest> findByMemberId(Long memberId);
}