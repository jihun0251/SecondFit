package com.example.backend.products.service;

import com.example.backend.categories.entity.Category;
import com.example.backend.categories.repository.CategoryRepository;
import com.example.backend.global.common.PageResponse;
import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.global.file.FileStorageService;
import com.example.backend.products.dto.*;
import com.example.backend.products.entity.Product;
import com.example.backend.products.entity.ProductImage;
import com.example.backend.products.repository.ProductRepository;
import com.example.backend.products.repository.ProductSpecification;
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

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

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
                condition.getPageSize() <= 0 ? 20 : Math.min(condition.getPageSize(), 100),
                toSort(condition.getSort())
        );

        Page<Product> page = productRepository.findAll(ProductSpecification.search(condition), pageable);
        return PageResponse.of(page, ProductSummaryResponse::from);
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

    /** 내가 등록한 상품 목록 (판매 관리 화면) */
    public PageResponse<ProductSummaryResponse> getMyProducts(Long sellerId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                pageSize <= 0 ? 20 : Math.min(pageSize, 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return PageResponse.of(productRepository.findBySellerId(sellerId, pageable), ProductSummaryResponse::from);
    }

    // ===================== 수정 / 삭제 =====================

    @Transactional
    public void update(Long userId, Long productId, ProductUpdateRequest request) {
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
    }

    @Transactional
    public void delete(Long userId, Long productId) {
        Product product = findOwnedProduct(userId, productId);

        if (!product.isEditable()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_DELETABLE);
        }

        productRepository.delete(product); // cascade + orphanRemoval로 이미지도 함께 삭제
    }

    // ===================== 이미지 =====================

    /** 파일 업로드 → 저장 → 상품에 이미지 추가 */
    @Transactional
    public ProductDetailResponse addImages(Long userId, Long productId, List<MultipartFile> files) {
        Product product = findOwnedProduct(userId, productId);

        if (!product.isEditable()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_EDITABLE);
        }
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_REQUIRED);
        }
        if (product.getImages().size() + files.size() > MAX_IMAGES) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_LIMIT_EXCEEDED);
        }

        int nextOrder = product.getImages().size();
        for (MultipartFile file : files) {
            String url = fileStorageService.store(file);
            product.addImage(ProductImage.builder()
                    .imageUrl(url)
                    .thumbnail(product.getImages().isEmpty()) // 첫 이미지면 자동으로 대표
                    .sortOrder(nextOrder++)
                    .build());
        }

        return ProductDetailResponse.from(product);
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
