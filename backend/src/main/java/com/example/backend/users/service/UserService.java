package com.example.backend.users.service;

import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.orders.entity.Order;
import com.example.backend.orders.repository.OrderRepository;
import com.example.backend.products.entity.Product;
import com.example.backend.products.repository.ProductRepository;
import com.example.backend.reviews.repository.ReviewRepository;
import com.example.backend.users.dto.SignupRequest;
import com.example.backend.users.dto.SignupResponse;
import com.example.backend.users.dto.UserDtos;
import com.example.backend.users.entity.User;
import com.example.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }

        // 2. 닉네임 중복 검사
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // 3. 비밀번호 암호화 + 엔티티 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .build();

        // 4. 저장 후 응답 DTO로 변환
        User saved = userRepository.save(user);
        return SignupResponse.from(saved);
    }

    /** 내 프로필 조회 */
    public UserDtos.MyProfile getMyProfile(Long userId) {
        User user = findUser(userId);
        return UserDtos.MyProfile.of(user, averageRating(userId), tradeCount(userId));
    }

    /** 내 프로필 수정 */
    @Transactional
    public UserDtos.Updated updateMyProfile(Long userId, UserDtos.UpdateRequest request) {
        User user = findUser(userId);

        // 닉네임을 실제로 "다른 값"으로 바꿀 때만 중복 검사한다.
        // (자기 닉네임을 그대로 다시 보내는 경우까지 409로 막으면 안 됨)
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())
                && userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
        }

        user.updateProfile(request.getNickname(), request.getPhone(),
                request.getProfileImage(), request.getSettlementAccount());

        return UserDtos.Updated.from(user);
    }

    /** 회원 탈퇴 — 레코드 삭제가 아니라 status를 WITHDRAWN으로 */
    @Transactional
    public void withdraw(Long userId) {
        findUser(userId).withdraw();
    }

    /** 특정 회원 공개 프로필 (판매중 상품 + 평점) */
    public UserDtos.PublicProfile getPublicProfile(Long userId) {
        User user = findUser(userId);

        List<Product> onSale = productRepository
                .findTop20BySellerIdAndStatusAndSuspendedFalseOrderByCreatedAtDesc(
                        userId, Product.Status.ON_SALE);

        return UserDtos.PublicProfile.of(user, averageRating(userId), tradeCount(userId), onSale);
    }

    // ===================== 집계 =====================

    /** 리뷰 평균 평점 (리뷰가 없으면 0.0) */
    private double averageRating(Long userId) {
        Double average = reviewRepository.findAverageRatingBySellerId(userId);
        return average == null ? 0.0 : Math.round(average * 10) / 10.0;
    }

    /** 거래 완료(CONFIRMED) 건수 */
    private long tradeCount(Long userId) {
        return orderRepository.countBySellerIdAndStatus(userId, Order.Status.CONFIRMED);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
