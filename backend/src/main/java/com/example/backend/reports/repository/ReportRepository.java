package com.example.backend.reports.repository;

import com.example.backend.reports.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatus(Report.Status status, Pageable pageable);

    /** 같은 상품에 신고가 몇 건 쌓였는지 (관리자 목록의 count) */
    long countByProductId(Long productId);

    /** 상품 삭제 시 참조를 끊어주기 위해 조회한다 (신고 기록 자체는 남긴다) */
    List<Report> findByProductId(Long productId);
}
