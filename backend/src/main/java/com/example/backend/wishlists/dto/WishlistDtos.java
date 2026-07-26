package com.example.backend.wishlists.dto;

import com.example.backend.products.entity.Product;
import com.example.backend.wishlists.entity.Wishlist;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/** 찜 요청/응답 DTO 모음 */
public final class WishlistDtos {

    private WishlistDtos() {
    }

    /** POST /wishlists */
    @Getter
    public static class Request {
        @NotNull(message = "상품 ID는 필수입니다.")
        private Long productId;
    }

    /** 추가 응답 → { wishlistId, productId } */
    @Getter
    public static class Created {
        private final Long wishlistId;
        private final Long productId;

        private Created(Wishlist w) {
            this.wishlistId = w.getId();
            this.productId = w.getProduct().getId();
        }

        public static Created from(Wishlist wishlist) {
            return new Created(wishlist);
        }
    }

    /** 목록 한 줄 → { productId, title, price, priceChange, status, thumbnail } */
    @Getter
    public static class Item {
        private final Long productId;
        private final String title;
        private final int price;
        private final int priceChange;
        private final Product.Status status;
        private final String thumbnail;

        private Item(Wishlist w) {
            Product p = w.getProduct();
            this.productId = p.getId();
            this.title = p.getTitle();
            this.price = p.getPrice();
            this.priceChange = w.getPriceChange();
            this.status = p.getStatus();
            this.thumbnail = p.getThumbnailUrl();
        }

        public static Item from(Wishlist wishlist) {
            return new Item(wishlist);
        }
    }
}
