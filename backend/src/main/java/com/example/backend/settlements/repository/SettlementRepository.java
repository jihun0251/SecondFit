package com.example.backend.settlements.repository;

import com.example.backend.settlements.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Page<Settlement> findBySellerId(Long sellerId, Pageable pageable);

    Page<Settlement> findBySellerIdAndStatus(Long sellerId, Settlement.Status status, Pageable pageable);

    /** 관리자 정산 목록 (상태 필터) */
    Page<Settlement> findByStatus(Settlement.Status status, Pageable pageable);
}
