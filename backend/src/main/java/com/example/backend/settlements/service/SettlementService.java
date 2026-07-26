package com.example.backend.settlements.service;

import com.example.backend.global.common.PageResponse;
import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.settlements.dto.SettlementDtos;
import com.example.backend.settlements.entity.Settlement;
import com.example.backend.settlements.repository.SettlementRepository;
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
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;

    /** 내 정산 내역 (판매자) */
    public PageResponse<SettlementDtos.Summary> getMySettlements(Long sellerId, Settlement.Status status,
                                                                 int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                size <= 0 ? 20 : Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Settlement> result = (status == null)
                ? settlementRepository.findBySellerId(sellerId, pageable)
                : settlementRepository.findBySellerIdAndStatus(sellerId, status, pageable);

        return PageResponse.of(result, SettlementDtos.Summary::from);
    }

    /** 정산 상세 — 판매자 본인 또는 ADMIN */
    public SettlementDtos.Detail getDetail(Long userId, boolean isAdmin, Long settlementId) {
        Settlement settlement = findSettlement(settlementId);

        if (!isAdmin && !settlement.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.SETTLEMENT_FORBIDDEN);
        }

        return SettlementDtos.Detail.from(settlement);
    }

    /** 관리자 정산 목록 (status 없으면 전체) */
    public PageResponse<SettlementDtos.AdminItem> getSettlementsForAdmin(Settlement.Status status,
                                                                         int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                size <= 0 ? 20 : Math.min(size, 100),
                Sort.by(Sort.Direction.ASC, "createdAt") // 먼저 확정된 건부터 정산
        );

        Page<Settlement> result = (status == null)
                ? settlementRepository.findAll(pageable)
                : settlementRepository.findByStatus(status, pageable);

        return PageResponse.of(result, SettlementDtos.AdminItem::from);
    }

    /** 관리자 정산 완료 처리 (실제 송금 후 호출) */
    @Transactional
    public SettlementDtos.Completed complete(Long adminId, Long settlementId) {
        Settlement settlement = findSettlement(settlementId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        settlement.complete(admin);

        return SettlementDtos.Completed.from(settlement);
    }

    private Settlement findSettlement(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));
    }
}
