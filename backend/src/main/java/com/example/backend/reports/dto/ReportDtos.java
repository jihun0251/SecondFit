package com.example.backend.reports.dto;

import com.example.backend.reports.entity.Report;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

/** 신고 요청/응답 DTO 모음 */
public final class ReportDtos {

    private ReportDtos() {
    }

    /** POST /reports */
    @Getter
    public static class Request {
        @NotNull(message = "신고 대상 상품 ID는 필수입니다.")
        private Long productId;

        @NotNull(message = "신고 사유는 필수입니다.")
        private Report.Reason reason;

        @Size(max = 500, message = "상세 사유는 500자 이하여야 합니다.")
        private String detail;
    }

    /** PATCH /admin/reports/{reportId} */
    @Getter
    public static class HandleRequest {
        @NotNull(message = "처리 상태는 필수입니다.")
        private Report.Status status;

        /** 예: SUSPEND_PRODUCT (RESOLVED와 함께 오면 상품이 노출 중지된다) */
        @Size(max = 50)
        private String action;
    }

    /** 접수 응답 → { reportId, status } */
    @Getter
    public static class Created {
        private final Long reportId;
        private final Report.Status status;

        private Created(Report r) {
            this.reportId = r.getId();
            this.status = r.getStatus();
        }

        public static Created from(Report report) {
            return new Created(report);
        }
    }

    /** 관리자 목록 한 줄 → { reportId, productId, seller, reason, count, status } */
    @Getter
    public static class Item {
        private final Long reportId;
        private final Long productId;
        private final String seller;
        private final Report.Reason reason;
        private final String detail;
        /** 같은 상품에 누적된 신고 건수 */
        private final long count;
        private final Report.Status status;
        private final LocalDateTime createdAt;

        private Item(Report r, long count) {
            this.reportId = r.getId();
            this.productId = r.getProduct() == null ? null : r.getProduct().getId();
            this.seller = r.getProduct() == null ? null : r.getProduct().getSeller().getNickname();
            this.reason = r.getReason();
            this.detail = r.getDetail();
            this.count = count;
            this.status = r.getStatus();
            this.createdAt = r.getCreatedAt();
        }

        public static Item of(Report report, long count) {
            return new Item(report, count);
        }
    }

    /** 처리 응답 → { reportId, status, resolvedAt } */
    @Getter
    public static class Handled {
        private final Long reportId;
        private final Report.Status status;
        private final String action;
        private final LocalDateTime resolvedAt;

        private Handled(Report r) {
            this.reportId = r.getId();
            this.status = r.getStatus();
            this.action = r.getAction();
            this.resolvedAt = r.getResolvedAt();
        }

        public static Handled from(Report report) {
            return new Handled(report);
        }
    }
}
