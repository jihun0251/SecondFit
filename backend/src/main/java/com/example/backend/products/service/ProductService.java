package com.example.backend.products.service;

import com.example.backend.categories.entity.Category;
import com.example.backend.categories.repository.CategoryRepository;
import com.example.backend.global.common.PageResponse;
import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.global.file.FileStorageService;
import com.example.backend.inbounds.entity.Inbound;
import com.example.backend.inbounds.repository.InboundRepository;
import com.example.backend.products.client.AiTaggingClient;
import com.example.backend.products.dto.*;
import com.example.backend.products.entity.Product;
import com.example.backend.products.entity.ProductImage;
import com.example.backend.products.repository.ProductRepository;
import com.example.backend.products.repository.ProductSpecification;
import com.example.backend.reports.entity.Report;
import com.example.backend.reports.repository.ReportRepository;
import com.example.backend.wishlists.repository.WishlistRepository;
import com.example.backend.users.entity.User;
import com.example.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final int MAX_IMAGES = 8;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final InboundRepository inboundRepository;
    private final WishlistRepository wishlistRepository;
    private final ReportRepository reportRepository;
    private final FileStorageService fileStorageService;
    private final AiTaggingClient aiTaggingClient;

    // ===================== 등록 =====================

    @Transactional
    public ProductCreateResponse create(Long sellerId, ProductCreateRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .size(request.getSize())
                .color(request.getColor())
                .conditionGrade(request.getConditionGrade())
                .aiSuggestedCategory(request.getAiSuggestedCategory())
                .aiSuggestedColor(request.getAiSuggestedColor())
                .aiConfidence(request.getAiConfidence())
                .build();

        List<String> urls = request.getImages();
        int thumbnailIndex = resolveThumbnailIndex(request.getThumbnailIndex(), urls.size());

        for (int i = 0; i < urls.size(); i++) {
            product.addImage(ProductImage.builder()
                    .imageUrl(urls.get(i))
                    .thumbnail(i == thumbnailIndex)
                    .sortOrder(i)
                    .build());
        }

        // cascade = ALL 이므로 product만 저장하면 이미지도 함께 INSERT 된다
        Product saved = productRepository.save(product);

        // 상품 등록 = 판매자가 본사로 보내겠다는 뜻이므로 입고 대기 건을 같이 만든다.
        // 이게 있어야 관리자 입고 목록에 뜨고, 입고 확인 시 판매중으로 전환된다.
        inboundRepository.save(Inbound.createFor(saved));

        return ProductCreateResponse.from(saved);
    }

    /** 대표 이미지 인덱스가 없거나 범위를 벗어나면 첫 장을 대표로 */
    private int resolveThumbnailIndex(Integer requested, int imageCount) {
        if (requested == null || requested < 0 || requested >= imageCount) {
            return 0;
        }
        return requested;
    }

    // ===================== 목록 / 상세 =====================

    public PageResponse<ProductSummaryResponse> search(ProductSearchCondition condition) {
        Pageable pageable = PageRequest.of(
                Math.max(condition.getPage(), 0),
                normalizeSize(condition.getPageSize()),
                toSort(condition.getSort())
        );

        Page<Product> page = productRepository.findAll(ProductSpecification.search(condition), pageable);
        return PageResponse.of(page, ProductSummaryResponse::from);
    }

    private int normalizeSize(int size) {
        if (size <= 0) return DEFAULT_PAGE_SIZE;
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private Sort toSort(String sort) {
        if (sort == null) return Sort.by(Sort.Direction.DESC, "createdAt");
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // latest
        };
    }

    /**
     * 상세 조회 + 조회수 증가.
     * 조회수를 올리므로 readOnly가 아닌 쓰기 트랜잭션이어야 한다.
     */
    @Transactional
    public ProductDetailResponse getDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.increaseViewCount(); // 더티 체킹으로 UPDATE 됨 (save 호출 불필요)

        return ProductDetailResponse.from(product);
    }

    /** 카테고리별 판매중 상품 (GET /categories/{categoryId}/products) */
    public PageResponse<ProductSummaryResponse> getByCategory(Long categoryId, String sort,
                                                              int page, int size) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size), toSort(sort));

        return PageResponse.of(
                productRepository.findByCategoryIdAndStatusAndSuspendedFalse(
                        categoryId, Product.Status.ON_SALE, pageable),
                ProductSummaryResponse::from);
    }

    /**
     * AI 자동 태깅.
     * 추론 결과를 "제안값"으로 돌려줄 뿐 DB에는 저장하지 않는다.
     * (판매자가 확인·수정한 최종값이 상품 등록 때 들어온다)
     */
    public AiTaggingResponse aiTagging(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        return aiTaggingClient.predict(image);
    }

    /**
     * 내 판매 내역 (GET /products/me).
     * status가 있으면 해당 상태만, 없으면 전체.
     */
    public PageResponse<ProductSummaryResponse> getMyProducts(Long sellerId, Product.Status status,
                                                              int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Product> result = (status == null)
                ? productRepository.findBySellerId(sellerId, pageable)
                : productRepository.findBySellerIdAndStatus(sellerId, status, pageable);

        return PageResponse.of(result, ProductSummaryResponse::from);
    }

    // ===================== 수정 / 삭제 =====================

    @Transactional
    public ProductUpdateResponse update(Long userId, Long productId, ProductUpdateRequest request) {
        Product product = findOwnedProduct(userId, productId);

        if (!product.isEditable()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EDITABLE);
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        product.update(category, request.getTitle(), request.getDescription(), request.getPrice(),
                request.getSize(), request.getColor(), request.getConditionGrade());

        return ProductUpdateResponse.from(product);
    }

    @Transactional
    public void delete(Long userId, Long productId) {
        Product product = findOwnedProduct(userId, productId);

        if (!product.isEditable()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_DELETABLE);
        }

        // 이 상품을 참조하는 다른 테이블의 행을 먼저 정리해야 한다.
        // Product 엔티티에는 이들로 가는 연관관계가 없어서 JPA가 알아서 못 지우고,
        // 그냥 두면 외래키 제약에 걸려 삭제가 통째로 실패한다.
        //
        // (주문/결제/정산은 PENDING_INBOUND 상품에는 존재할 수 없으므로 대상이 아니다)
        inboundRepository.findByProductId(productId).ifPresent(inboundRepository::delete);
        wishlistRepository.deleteByProductId(productId);
        // 신고는 기록을 남겨야 하므로 삭제하지 않고 참조만 끊는다
        reportRepository.findByProductId(productId).forEach(Report::detachProduct);

        productRepository.delete(product); // cascade + orphanRemoval로 이미지도 함께 삭제
    }

    // ===================== 이미지 =====================

    /**
     * 이미지 1장 업로드 → 저장 → 상품에 추가.
     * 명세서상 Form 필드는 file(단수) + isThumbnail(선택).
     */
    @Transactional
    public ProductImageUploadResponse addImage(Long userId, Long productId,
                                               MultipartFile file, boolean isThumbnail) {
        Product product = findOwnedProduct(userId, productId);

        if (!product.isEditable()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EDITABLE);
        }
        if (product.getImages().size() >= MAX_IMAGES) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_LIMIT_EXCEEDED);
        }

        String url = fileStorageService.store(file);

        // 첫 이미지는 무조건 대표. 그 외에는 요청값을 따른다.
        boolean makeThumbnail = product.getImages().isEmpty() || isThumbnail;

        // 새 이미지를 대표로 지정하면 기존 대표는 해제해야 한다 (대표는 항상 1장)
        if (makeThumbnail) {
            product.getImages().forEach(img -> img.markAsThumbnail(false));
        }

        ProductImage image = ProductImage.builder()
                .imageUrl(url)
                .thumbnail(makeThumbnail)
                .sortOrder(product.getImages().size())
                .build();
        product.addImage(image);

        // flush를 해야 INSERT가 실행되어 image.id가 채워진다 (응답에 imageId가 필요)
        productRepository.flush();

        return ProductImageUploadResponse.from(image);
    }

    @Transactional
    public void deleteImage(Long userId, Long productId, Long imageId) {
        Product product = findOwnedProduct(userId, productId);

        if (!product.isEditable()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EDITABLE);
        }

        ProductImage target = product.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));

        if (product.getImages().size() <= 1) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_REQUIRED); // 마지막 1장은 못 지움
        }

        boolean wasThumbnail = target.isThumbnail();
        product.removeImage(target);

        // 대표 이미지를 지웠다면 남은 첫 장을 대표로 승격시킨다
        if (wasThumbnail) {
            product.getImages().stream().findFirst()
                    .ifPresent(img -> img.markAsThumbnail(true));
        }
    }

    // ===================== 공통 =====================

    /** 존재 + 소유권 확인을 한 번에. 404와 403을 구분해서 던진다 */
    private Product findOwnedProduct(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.PRODUCT_FORBIDDEN);
        }
        return product;
    }
}
