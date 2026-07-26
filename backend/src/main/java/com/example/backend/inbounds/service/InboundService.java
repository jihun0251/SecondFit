package com.example.backend.inbounds.service;

import com.example.backend.global.common.PageResponse;
import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.inbounds.dto.InboundConfirmResponse;
import com.example.backend.inbounds.dto.InboundSummaryResponse;
import com.example.backend.inbounds.entity.Inbound;
import com.example.backend.inbounds.repository.InboundRepository;
import com.example.backend.users.entity.User;
import com.example.backend.users.repository.UserRepository;
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
public class InboundService {

    private final InboundRepository inboundRepository;
    private final UserRepository userRepository;

    /** 입고 목록 (status 없으면 전체) */
    public PageResponse<InboundSummaryResponse> getInbounds(Inbound.Status status, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                size <= 0 ? 20 : Math.min(size, 100),
                Sort.by(Sort.Direction.ASC, "createdAt") // 먼저 들어온 것부터 처리
        );

        Page<Inbound> result = (status == null)
                ? inboundRepository.findAll(pageable)
                : inboundRepository.findByStatus(status, pageable);

        return PageResponse.of(result, InboundSummaryResponse::from);
    }

    /**
     * 입고 확인 → 상품 판매 시작.
     * 상태 전이는 Inbound 엔티티의 confirm()이 책임진다 (상품 상태까지 같이 바꿈).
     */
    @Transactional
    public InboundConfirmResponse confirm(Long adminId, Long inboundId) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INBOUND_NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        inbound.confirm(admin);

        return InboundConfirmResponse.from(inbound);
    }
}
