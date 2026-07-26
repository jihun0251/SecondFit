package com.example.backend.reviews.service;

import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.orders.entity.Order;
import com.example.backend.orders.repository.OrderRepository;
import com.example.backend.reviews.dto.ReviewDtos;
import com.example.backend.reviews.entity.Review;
import com.example.backend.reviews.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    /** 리뷰 작성 — 거래 확정된 본인 주문에만 1건 */
    @Transactional
    public ReviewDtos.Created create(Long reviewerId, ReviewDtos.Request request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.isOwnedBy(reviewerId)) {
            throw new BusinessException(ErrorCode.ORDER_FORBIDDEN);
        }
        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_WRITTEN);
        }

        // CONFIRMED 검사는 Review.create()가 담당한다
        Review review = reviewRepository.save(
                Review.create(order, request.getRating(), request.getContent()));

        return ReviewDtos.Created.from(review);
    }

    /** 판매자가 받은 리뷰 목록 + 평균 평점 */
    public ReviewDtos.SellerReviews getSellerReviews(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                size <= 0 ? 20 : Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Review> result = reviewRepository.findBySellerId(sellerId, pageable);

        Double average = reviewRepository.findAverageRatingBySellerId(sellerId);
        double averageRating = average == null ? 0.0 : Math.round(average * 10) / 10.0; // 소수 1자리

        return new ReviewDtos.SellerReviews(
                averageRating,
                result.getTotalElements(),
                result.getContent().stream().map(ReviewDtos.Item::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /** 리뷰 삭제 — 작성자 본인만 */
    @Transactional
    public void delete(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.isWrittenBy(userId)) {
            throw new BusinessException(ErrorCode.REVIEW_FORBIDDEN);
        }

        reviewRepository.delete(review);
    }
}
