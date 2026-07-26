package com.example.backend.orders.dto;

import com.example.backend.orders.entity.Order;
import lombok.Getter;

import java.time.LocalDateTime;

/** 주문 상세: 상품 · 배송 · 상태 타임라인 */
@Getter
public class OrderDetailResponse {

    private final Long orderId;
    private final ProductInfo product;
    private final int orderPrice;
    private final Order.Status status;
    private final ShippingInfo shipping;
    private final Timeline timeline;

    private OrderDetailResponse(Order o) {
        this.orderId = o.getId();
        this.product = new ProductInfo(o.getProduct().getId(), o.getProduct().getTitle());
        this.orderPrice = o.getOrderPrice();
        this.status = o.getStatus();
        this.shipping = new ShippingInfo(o);
        this.timeline = new Timeline(o);
    }

    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(order);
    }

    @Getter
    public static class ProductInfo {
        private final Long productId;
        private final String title;

        private ProductInfo(Long productId, String title) {
            this.productId = productId;
            this.title = title;
        }
    }

    @Getter
    public static class ShippingInfo {
        private final String receiverName;
        private final String receiverPhone;
        private final String zipcode;
        private final String address1;
        private final String address2;
        private final String memo;
        private final String trackingNo;

        private ShippingInfo(Order o) {
            this.receiverName = o.getReceiverName();
            this.receiverPhone = o.getReceiverPhone();
            this.zipcode = o.getZipcode();
            this.address1 = o.getAddress1();
            this.address2 = o.getAddress2();
            this.memo = o.getMemo();
            this.trackingNo = o.getTrackingNo();
        }
    }

    @Getter
    public static class Timeline {
        private final LocalDateTime paidAt;
        private final LocalDateTime shippedAt;
        private final LocalDateTime deliveredAt;
        private final LocalDateTime confirmedAt;

        private Timeline(Order o) {
            this.paidAt = o.getPaidAt();
            this.shippedAt = o.getShippedAt();
            this.deliveredAt = o.getDeliveredAt();
            this.confirmedAt = o.getConfirmedAt();
        }
    }
}
