package com.example.backend.inbounds.dto;

import com.example.backend.inbounds.entity.Inbound;
import lombok.Getter;

/** 관리자 입고 목록 한 줄 */
@Getter
public class InboundSummaryResponse {

    private final Long inboundId;
    private final Long productId;
    private final String title;
    private final String seller;
    private final int price;
    private final String trackingNo;
    private final Inbound.Status status;

    private InboundSummaryResponse(Inbound inbound) {
        this.inboundId = inbound.getId();
        this.productId = inbound.getProduct().getId();
        this.title = inbound.getProduct().getTitle();
        this.seller = inbound.getProduct().getSeller().getNickname();
        this.price = inbound.getProduct().getPrice();
        this.trackingNo = inbound.getTrackingNo();
        this.status = inbound.getStatus();
    }

    public static InboundSummaryResponse from(Inbound inbound) {
        return new InboundSummaryResponse(inbound);
    }
}
