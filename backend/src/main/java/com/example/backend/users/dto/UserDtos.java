package com.example.backend.users.dto;

import com.example.backend.products.entity.Product;
import com.example.backend.users.entity.User;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

/** 회원 프로필 요청/응답 DTO 모음 */
public final class UserDtos {

    private UserDtos() {
    }

    /** PATCH /users/me — 전부 선택 (null이면 변경 안 함) */
    @Getter
    public static class UpdateRequest {
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        private String nickname;

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        private String phone;

        @Size(max = 500)
        private String profileImage;

        @Size(max = 100)
        private String settlementAccount;
    }

    /** GET /users/me */
    @Getter
    public static class MyProfile {
        private final Long userId;
        private final String email;
        private final String nickname;
        private final String phone;
        private final String profileImage;
        private final String settlementAccount;
        private final double rating;
        private final long tradeCount;
        private final User.Role role;

        private MyProfile(User u, double rating, long tradeCount) {
            this.userId = u.getId();
            this.email = u.getEmail();
            this.nickname = u.getNickname();
            this.phone = maskPhone(u.getPhone());
            this.profileImage = u.getProfileImage();
            this.settlementAccount = u.getSettlementAccount();
            this.rating = rating;
            this.tradeCount = tradeCount;
            this.role = u.getRole();
        }

        public static MyProfile of(User user, double rating, long tradeCount) {
            return new MyProfile(user, rating, tradeCount);
        }
    }

    /** PATCH /users/me 응답 */
    @Getter
    public static class Updated {
        private final Long userId;
        private final String nickname;
        private final String phone;

        private Updated(User u) {
            this.userId = u.getId();
            this.nickname = u.getNickname();
            this.phone = maskPhone(u.getPhone());
        }

        public static Updated from(User user) {
            return new Updated(user);
        }
    }

    /** GET /users/{userId} — 공개 프로필 (이메일·전화 등 개인정보 제외) */
    @Getter
    public static class PublicProfile {
        private final Long userId;
        private final String nickname;
        private final String profileImage;
        private final double rating;
        private final long tradeCount;
        private final List<OnSaleProduct> onSaleProducts;

        private PublicProfile(User u, double rating, long tradeCount, List<Product> products) {
            this.userId = u.getId();
            this.nickname = u.getNickname();
            this.profileImage = u.getProfileImage();
            this.rating = rating;
            this.tradeCount = tradeCount;
            this.onSaleProducts = products.stream().map(OnSaleProduct::from).toList();
        }

        public static PublicProfile of(User user, double rating, long tradeCount, List<Product> products) {
            return new PublicProfile(user, rating, tradeCount, products);
        }
    }

    @Getter
    public static class OnSaleProduct {
        private final Long productId;
        private final String title;
        private final int price;
        private final String thumbnail;

        private OnSaleProduct(Product p) {
            this.productId = p.getId();
            this.title = p.getTitle();
            this.price = p.getPrice();
            this.thumbnail = p.getThumbnailUrl();
        }

        private static OnSaleProduct from(Product product) {
            return new OnSaleProduct(product);
        }
    }

    /**
     * 전화번호 가운데 자리 마스킹 (010-1234-5678 → 010-****-5678).
     * 본인 프로필 응답에도 마스킹해서 내려주는 게 명세 기준이다.
     */
    private static String maskPhone(String phone) {
        if (phone == null) return null;
        String[] parts = phone.split("-");
        if (parts.length != 3) return phone;
        return parts[0] + "-****-" + parts[2];
    }
}
