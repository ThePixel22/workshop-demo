package com.company.member.repository;

import com.company.member.entity.NightBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NightBalanceRepository extends JpaRepository<NightBalance, Long> {
    Optional<NightBalance> findByMemberId(Long memberId);
    boolean existsByMemberId(Long memberId);
}
