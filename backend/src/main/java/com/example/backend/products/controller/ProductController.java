package com.example.backend.products.controller;

import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.global.common.ApiResponse;
import com.example.backend.global.common.PageResponse;
import com.example.backend.products.dto.*;
import com.example.backend.products.entity.Product;
import com.example.backend.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** 상품 등록 — 등록 즉시 PENDING_INBOUND(입고 대기). 201 Created */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductCreateResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProductCreateRequest request) {

        ProductCreateResponse response = productService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 상품 목록 조회 (검색/필터/정렬/페이징). 인증 불필요, ON_SALE만 노출.
     * 쿼리 파라미터가 많아서 @ModelAttribute로 한 객체에 묶어 받는다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> search(
            @ModelAttribute ProductSearchCondition condition) {

        return ResponseEntity.ok(ApiResponse.success(productService.search(condition)));
    }

    /**
     * AI 자동 태깅 — 대표 이미지를 FastAPI 추론 서버로 보내 제안값을 받는다.
     * ⚠️ /{productId} 매핑보다 위에 있어야 "ai-tagging"이 productId로 해석되지 않는다.
     * ⚠️ 추론 서버가 죽어도 200 + available=false로 응답한다 (등록 플로우를 막지 않기 위함).
     */
    @PostMapping(value = "/ai-tagging", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AiTaggingResponse>> aiTagging(
            @RequestPart("image") MultipartFile image) {

        return ResponseEntity.ok(ApiResponse.success(productService.aiTagging(image)));
    }

    /**
     * 내 판매 내역 조회.
     * ⚠️ 아래 /{productId} 매핑보다 위에 있어야 "me"가 productId로 해석되지 않는다.
     * ⚠️ 페이지 크기 파라미터가 목록 API는 pageSize, 여기는 size다 (명세서 기준).
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> getMyProducts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Product.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getMyProducts(principal.getUserId(), status, page, size)));
    }

    /** 상품 상세 조회 (조회수 +1). 인증 불필요 */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getDetail(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getDetail(productId)));
    }

    /** 상품 수정 — 본인 + 입고 확인 전(PENDING_INBOUND)에만 가능. 수정된 상품 반환 */
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductUpdateResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductUpdateResponse response = productService.update(principal.getUserId(), productId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상품 삭제 — 본인 + 입고 확인 전에만 가능.
     * 명세서상 204 No Content(본문 없음)라서 ApiResponse로 감싸지 않는다.
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId) {

        productService.delete(principal.getUserId(), productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 상품 이미지 업로드 (multipart/form-data).
     * Form 필드: file(필수), isThumbnail(선택). 201 Created.
     */
    @PostMapping(value = "/{productId}/images", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductImageUploadResponse>> addImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "isThumbnail", defaultValue = "false") boolean isThumbnail) {

        ProductImageUploadResponse response =
                productService.addImage(principal.getUserId(), productId, file, isThumbnail);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /** 상품 이미지 삭제 — 마지막 1장은 삭제 불가. 204 No Content */
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        productService.deleteImage(principal.getUserId(), productId, imageId);
        return ResponseEntity.noContent().build();
    }
}
