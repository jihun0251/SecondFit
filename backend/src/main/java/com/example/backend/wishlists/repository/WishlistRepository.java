package com.example.backend.wishlists.repository;

import com.example.backend.wishlists.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

    List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** 상품 삭제 시 이 상품을 찜한 기록을 함께 정리한다 (FK 제약 위반 방지) */
    void deleteByProductId(Long productId);
}
