package com.example.backend.reports.service;

import com.example.backend.global.common.PageResponse;
import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.products.entity.Product;
import com.example.backend.products.repository.ProductRepository;
import com.example.backend.reports.dto.ReportDtos;
import com.example.backend.reports.entity.Report;
import com.example.backend.reports.repository.ReportRepository;
import com.example.backend.users.entity.User;
import com.example.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /** 신고 접수 */
    @Transactional
    public ReportDtos.Created create(Long reporterId, ReportDtos.Request request) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Report report = reportRepository.save(
                Report.create(reporter, product, request.getReason(), request.getDetail()));

        return ReportDtos.Created.from(report);
    }

    /** 관리자 신고 목록 */
    public PageResponse<ReportDtos.Item> getReports(Report.Status status, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                size <= 0 ? 20 : Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Report> result = (status == null)
                ? reportRepository.findAll(pageable)
                : reportRepository.findByStatus(status, pageable);

        // 같은 상품에 신고가 몇 건 쌓였는지 함께 보여준다 (반복 신고 = 우선 처리 신호)
        return PageResponse.of(result, report -> {
            long count = report.getProduct() == null
                    ? 0
                    : reportRepository.countByProductId(report.getProduct().getId());
            return ReportDtos.Item.of(report, count);
        });
    }

    /**
     * 관리자 신고 처리.
     * status=RESOLVED + action=SUSPEND_PRODUCT면 상품이 노출 중지된다 (Report.handle이 처리).
     */
    @Transactional
    public ReportDtos.Handled handle(Long adminId, Long reportId, ReportDtos.HandleRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        report.handle(admin, request.getStatus(), request.getAction());

        return ReportDtos.Handled.from(report);
    }
}
