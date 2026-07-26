package com.example.backend.inbounds.dto;

import com.example.backend.inbounds.entity.Inbound;
import com.example.backend.products.entity.Product;
import lombok.Getter;

import java.time.LocalDateTime;

/** 입고 확인 응답: { inboundId, productId, productStatus, confirmedAt } */
@Getter
public class InboundConfirmResponse {

    private final Long inboundId;
    private final Long productId;
    private final Product.Status productStatus;
    private final LocalDateTime confirmedAt;

    private InboundConfirmResponse(Inbound inbound) {
        this.inboundId = inbound.getId();
        this.productId = inbound.getProduct().getId();
        this.productStatus = inbound.getProduct().getStatus();
        this.confirmedAt = inbound.getConfirmedAt();
    }

    public static InboundConfirmResponse from(Inbound inbound) {
        return new InboundConfirmResponse(inbound);
    }
}
