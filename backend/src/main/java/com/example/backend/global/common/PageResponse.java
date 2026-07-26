package com.example.backend.global.common;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * 페이징 응답 공통 포맷.
 * <p>
 * Spring의 Page 객체를 그대로 JSON으로 내보내면 pageable, sort 같은
 * 내부 구조가 통째로 노출되고 Spring 버전 올릴 때 응답 스펙이 깨진다.
 * 그래서 필요한 필드만 담은 우리 DTO로 감싸서 내려준다.
 */
@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    private PageResponse(List<T> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    /** 엔티티 Page를 DTO Page로 변환하면서 감싼다 */
    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
