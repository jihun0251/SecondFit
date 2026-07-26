package com.example.backend.wishlists.service;

import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.products.entity.Product;
import com.example.backend.products.repository.ProductRepository;
import com.example.backend.users.entity.User;
import com.example.backend.users.repository.UserRepository;
import com.example.backend.wishlists.dto.WishlistDtos;
import com.example.backend.wishlists.entity.Wishlist;
import com.example.backend.wishlists.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /** 내 찜 목록 (명세상 페이징 없이 배열로 반환) */
    public List<WishlistDtos.Item> getMyWishlists(Long userId) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(WishlistDtos.Item::from)
                .toList();
    }

    /** 찜 추가 — 중복 불가 */
    @Transactional
    public WishlistDtos.Created add(Long userId, WishlistDtos.Request request) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new BusinessException(ErrorCode.WISHLIST_DUPLICATED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Wishlist wishlist = wishlistRepository.save(Wishlist.create(user, product));
        return WishlistDtos.Created.from(wishlist);
    }

    /** 찜 취소 — productId로 지운다 (wishlistId가 아님에 주의) */
    @Transactional
    public void remove(Long userId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WISHLIST_NOT_FOUND));

        wishlistRepository.delete(wishlist);
    }
}
