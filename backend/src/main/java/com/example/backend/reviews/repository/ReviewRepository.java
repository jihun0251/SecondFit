package com.example.backend.reviews.repository;

import com.example.backend.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderId(Long orderId);

    Page<Review> findBySellerId(Long sellerId, Pageable pageable);

    /**
     * 판매자 평균 평점.
     * 리뷰가 하나도 없으면 AVG는 null을 반환하므로 받는 쪽 타입을 Double(래퍼)로 둔다.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.seller.id = :sellerId")
    Double findAverageRatingBySellerId(@Param("sellerId") Long sellerId);

    long countBySellerId(Long sellerId);
}
