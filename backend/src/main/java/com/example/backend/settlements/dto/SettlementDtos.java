package com.example.backend.settlements.dto;

import com.example.backend.settlements.entity.Settlement;
import lombok.Getter;

import java.time.LocalDateTime;

/** 정산 응답 DTO 모음 */
public final class SettlementDtos {

    private SettlementDtos() {
    }

    /** GET /settlements/me 한 줄 */
    @Getter
    public static class Summary {
        private final Long settlementId;
        private final Long orderId;
        private final int grossAmount;
        private final int feeAmount;
        private final int netAmount;
        private final Settlement.Status status;

        private Summary(Settlement s) {
            this.settlementId = s.getId();
            this.orderId = s.getOrder().getId();
            this.grossAmount = s.getGrossAmount();
            this.feeAmount = s.getFeeAmount();
            this.netAmount = s.getNetAmount();
            this.status = s.getStatus();
        }

        public static Summary from(Settlement settlement) {
            return new Summary(settlement);
        }
    }

    /** GET /settlements/{settlementId} */
    @Getter
    public static class Detail {
        private final Long settlementId;
        private final Long orderId;
        private final Long sellerId;
        private final int grossAmount;
        private final int feeAmount;
        private final int netAmount;
        private final Settlement.Status status;
        private final LocalDateTime settledAt;

        private Detail(Settlement s) {
            this.settlementId = s.getId();
            this.orderId = s.getOrder().getId();
            this.sellerId = s.getSeller().getId();
            this.grossAmount = s.getGrossAmount();
            this.feeAmount = s.getFeeAmount();
            this.netAmount = s.getNetAmount();
            this.status = s.getStatus();
            this.settledAt = s.getSettledAt();
        }

        public static Detail from(Settlement settlement) {
            return new Detail(settlement);
        }
    }

    /** POST /admin/settlements/{id}/complete → { settlementId, status, netAmount, settledAt } */
    @Getter
    public static class Completed {
        private final Long settlementId;
        private final Settlement.Status status;
        private final int netAmount;
        private final LocalDateTime settledAt;

        private Completed(Settlement s) {
            this.settlementId = s.getId();
            this.status = s.getStatus();
            this.netAmount = s.getNetAmount();
            this.settledAt = s.getSettledAt();
        }

        public static Completed from(Settlement settlement) {
            return new Completed(settlement);
        }
    }
}
