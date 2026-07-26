package com.example.backend.orders.dto;

import com.example.backend.orders.entity.Order;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 주문 관련 응답 DTO 모음 (명세서 응답 샘플과 필드를 1:1로 맞춤).
 */
public final class OrderResponses {

    private OrderResponses() {
    }

    /** POST /orders → { orderId, productId, orderPrice, status } */
    @Getter
    public static class Created {
        private final Long orderId;
        private final Long productId;
        private final int orderPrice;
        private final Order.Status status;

        private Created(Order o) {
            this.orderId = o.getId();
            this.productId = o.getProduct().getId();
            this.orderPrice = o.getOrderPrice();
            this.status = o.getStatus();
        }

        public static Created from(Order order) {
            return new Created(order);
        }
    }

    /** GET /orders/me 한 줄 → { orderId, title, orderPrice, status, paidAt } */
    @Getter
    public static class Summary {
        private final Long orderId;
        private final String title;
        private final int orderPrice;
        private final Order.Status status;
        private final LocalDateTime paidAt;

        private Summary(Order o) {
            this.orderId = o.getId();
            this.title = o.getProduct().getTitle();
            this.orderPrice = o.getOrderPrice();
            this.status = o.getStatus();
            this.paidAt = o.getPaidAt();
        }

        public static Summary from(Order order) {
            return new Summary(order);
        }
    }

    /** PATCH /orders/{id}/shipping → { orderId, shippingSaved, status } */
    @Getter
    public static class ShippingSaved {
        private final Long orderId;
        private final boolean shippingSaved;
        private final Order.Status status;

        private ShippingSaved(Order o) {
            this.orderId = o.getId();
            this.shippingSaved = true;
            this.status = o.getStatus();
        }

        public static ShippingSaved from(Order order) {
            return new ShippingSaved(order);
        }
    }

    /** POST /orders/{id}/confirm → { orderId, status, settlementId } */
    @Getter
    public static class Confirmed {
        private final Long orderId;
        private final Order.Status status;
        private final Long settlementId;

        private Confirmed(Order o, Long settlementId) {
            this.orderId = o.getId();
            this.status = o.getStatus();
            this.settlementId = settlementId;
        }

        public static Confirmed of(Order order, Long settlementId) {
            return new Confirmed(order, settlementId);
        }
    }

    /** POST /orders/{id}/cancel → { orderId, status } */
    @Getter
    public static class Cancelled {
        private final Long orderId;
        private final Order.Status status;

        private Cancelled(Order o) {
            this.orderId = o.getId();
            this.status = o.getStatus();
        }

        public static Cancelled from(Order order) {
            return new Cancelled(order);
        }
    }

    /** POST /admin/orders/{id}/ship → { orderId, status, trackingNo } */
    @Getter
    public static class Shipped {
        private final Long orderId;
        private final Order.Status status;
        private final String trackingNo;

        private Shipped(Order o) {
            this.orderId = o.getId();
            this.status = o.getStatus();
            this.trackingNo = o.getTrackingNo();
        }

        public static Shipped from(Order order) {
            return new Shipped(order);
        }
    }

    /** POST /admin/orders/{id}/deliver → { orderId, status, deliveredAt } */
    @Getter
    public static class Delivered {
        private final Long orderId;
        private final Order.Status status;
        private final LocalDateTime deliveredAt;

        private Delivered(Order o) {
            this.orderId = o.getId();
            this.status = o.getStatus();
            this.deliveredAt = o.getDeliveredAt();
        }

        public static Delivered from(Order order) {
            return new Delivered(order);
        }
    }
}
