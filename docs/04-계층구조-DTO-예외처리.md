# 04. 계층 구조 · DTO · 예외 처리

> "왜 클래스를 이렇게 많이 만드나?"에 대한 답입니다. 상품 도메인 하나에 파일이 12개인데, 각각이 왜 필요한지 설명합니다.

---

## 1. 왜 계층을 나누나

한 파일에 다 넣으면 안 될까요? 이렇게요.

```java
// ❌ 전부 컨트롤러에
@PostMapping("/products")
public ResponseEntity<?> create(@RequestBody Map<String, Object> body, HttpServletRequest req) {
    String token = req.getHeader("Authorization").substring(7);
    Long userId = jwtParser.parse(token);

    Connection conn = dataSource.getConnection();
    PreparedStatement ps = conn.prepareStatement("INSERT INTO products ...");
    ...
}
```

작은 기능 하나면 이게 더 빠릅니다. 문제는 **기능이 45개**가 됐을 때입니다.

- 상품 등록 규칙을 배치 프로그램에서도 쓰고 싶다 → HTTP 없이는 못 부름
- 테스트를 쓰고 싶다 → 가짜 HTTP 요청을 만들어야 함
- DB를 바꾸고 싶다 → 45개 파일을 다 고쳐야 함

**계층 분리는 "변하는 이유가 다른 것을 떼어놓는" 작업입니다.**

| 계층 | 변하는 이유 |
|---|---|
| Controller | API 스펙이 바뀔 때 (URL, 응답 형식) |
| Service | 비즈니스 규칙이 바뀔 때 (수수료율, 취소 조건) |
| Repository | 데이터 저장 방식이 바뀔 때 (MySQL → MongoDB) |
| Entity | 도메인 개념이 바뀔 때 |

수수료율이 바뀌었는데 컨트롤러를 고칠 일이 없어야 정상입니다.

---

## 2. 상품 도메인의 파일 12개

```
products/
├── controller/
│   └── ProductController.java        HTTP 담당
├── service/
│   └── ProductService.java           비즈니스 규칙
├── repository/
│   ├── ProductRepository.java        DB 접근
│   └── ProductSpecification.java     동적 검색 조건
├── entity/
│   ├── Product.java                  상품 (상태머신 포함)
│   └── ProductImage.java             상품 이미지
├── client/
│   └── AiTaggingClient.java          외부 AI 서버 호출
└── dto/
    ├── ProductCreateRequest.java     등록 요청
    ├── ProductUpdateRequest.java     수정 요청
    ├── ProductSearchCondition.java   검색 조건
    ├── ProductCreateResponse.java    등록 응답
    ├── ProductSummaryResponse.java   목록용 응답
    ├── ProductDetailResponse.java    상세용 응답
    ├── ProductUpdateResponse.java    수정 응답
    ├── ProductImageUploadResponse.java
    └── AiTaggingResponse.java
```

DTO가 9개나 됩니다. 과한 걸까요? 아래에서 이유를 봅시다.

---

## 3. DTO — 왜 엔티티를 그대로 반환하면 안 되나

**DTO(Data Transfer Object)** 는 계층 간에 데이터를 나르는 전용 객체입니다.

```java
// ❌ 엔티티를 그대로 반환한다면
@GetMapping("/{productId}")
public Product getDetail(@PathVariable Long productId) {
    return productRepository.findById(productId).orElseThrow();
}
```

### 문제 1 — 감춰야 할 게 새어나간다

`Product`는 `seller`(User)를 갖고 있고, `User`는 `password`(BCrypt 해시)를 갖고 있습니다. Jackson이 JSON으로 바꿀 때 **연관 객체를 따라가며 전부 직렬화**하므로, 비밀번호 해시가 응답에 실려 나갑니다.

### 문제 2 — 무한 루프

```java
Product.images → ProductImage.product → Product.images → ProductImage.product → ...
```

양방향 연관관계를 직렬화하면 **StackOverflowError**가 납니다.

### 문제 3 — 필요 없는 쿼리 폭탄

Jackson이 `product.getSeller()`를 부르는 순간 LAZY 프록시가 초기화되어 쿼리가 나갑니다. 목록 20건이면 20번 나가고요.

### 문제 4 — 내부 구조가 API 스펙이 된다

엔티티 필드명을 바꾸는 순간 API 응답이 바뀌어서 프론트가 깨집니다. **DB 리팩터링이 클라이언트에 영향을 주면 안 됩니다.**

### 그래서 DTO로 변환합니다

```java
// products/dto/ProductDetailResponse.java
@Getter
public class ProductDetailResponse {

    private final Long productId;
    private final String title;
    ...
    private final SellerInfo seller;   // 필요한 것만 담은 중첩 클래스

    private ProductDetailResponse(Product p) {
        this.productId = p.getId();
        this.title = p.getTitle();
        ...
        this.seller = SellerInfo.from(p.getSeller());   // 닉네임, 평점만
    }

    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(product);
    }

    @Getter
    public static class SellerInfo {
        private final Long userId;
        private final String nickname;
        private final Double rating;
        // password, email 없음 ✅
    }
}
```

---

## 4. 왜 같은 도메인에 응답 DTO가 여러 개인가

`ProductSummaryResponse`와 `ProductDetailResponse`가 따로 있습니다.

| | 목록용 Summary | 상세용 Detail |
|---|---|---|
| 필드 수 | 8개 | 12개 + 이미지 배열 + 판매자 |
| 이미지 | 대표 1장 URL만 | 전체 배열 |
| 설명 | 없음 | 있음 |

**목록에서 20건을 보여주는데 각각의 상품 설명(TEXT)과 이미지 8장 정보를 다 실어 보낼 이유가 없습니다.** 응답 크기가 10배 차이 납니다.

> 💡 화면마다 필요한 데이터가 다르면 DTO를 나눕니다. "하나로 통일해서 다 담자"가 편해 보이지만, 결국 아무도 안 쓰는 필드를 매번 조회하고 전송하게 됩니다.

---

## 5. 정적 팩토리 메서드 `from()`

우리 DTO는 전부 이 패턴입니다.

```java
private ProductDetailResponse(Product p) { ... }   // 생성자는 private

public static ProductDetailResponse from(Product product) {
    return new ProductDetailResponse(product);
}
```

**왜 생성자를 직접 안 쓰고 `from()`을 만드나?**

1. **이름이 있다.** `from(product)`는 "상품으로부터 만든다"가 읽힙니다. 생성자는 이름이 클래스명으로 고정이라 의도를 못 담습니다.
2. **메서드 참조로 쓸 수 있다.**
   ```java
   return PageResponse.of(page, ProductSummaryResponse::from);   // 깔끔
   ```
3. **나중에 캐싱이나 다른 구현으로 바꿔도 호출부가 안 바뀝니다.**

이름 관례도 있습니다.
- `from(A)` : 하나의 인자로 변환
- `of(A, B, ...)` : 여러 인자를 모아서 생성 — 예: `LoginResponse.of(accessToken, refreshToken, user)`

---

## 6. Request DTO와 검증

```java
// products/dto/ProductCreateRequest.java
@Getter
public class ProductCreateRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 150, message = "상품명은 150자 이하여야 합니다.")
    private String title;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @NotEmpty(message = "상품 이미지는 1장 이상 필요합니다.")
    @Size(max = 8, message = "상품 이미지는 최대 8장까지 등록할 수 있습니다.")
    private List<@NotBlank String> images;
    ...
}
```

이게 **Bean Validation**입니다. 컨트롤러에서 `@Valid`를 붙이면 자동으로 검사합니다.

```java
@PostMapping
public ResponseEntity<...> create(@AuthenticationPrincipal UserPrincipal principal,
                                  @Valid @RequestBody ProductCreateRequest request) {
```

검증에 실패하면 서비스 코드가 **실행되기도 전에** `MethodArgumentNotValidException`이 발생합니다.

### `@NotNull` vs `@NotBlank` vs `@NotEmpty`

| 어노테이션 | `null` | `""` | `"  "` | 빈 리스트 |
|---|---|---|---|---|
| `@NotNull` | ❌ | ✅통과 | ✅통과 | ✅통과 |
| `@NotEmpty` | ❌ | ❌ | ✅통과 | ❌ |
| `@NotBlank` | ❌ | ❌ | ❌ | (문자열 전용) |

문자열에는 보통 `@NotBlank`가 맞습니다. 공백만 있는 이름은 없는 것과 같으니까요.

### `Integer price` vs `int price` — 왜 래퍼 타입?

```java
@NotNull(message = "가격은 필수입니다.")
private Integer price;
```

`int price`로 하면 JSON에 `price`가 없을 때 **자동으로 0**이 들어갑니다. "가격을 안 보냈다"와 "0원이다"를 구분할 수 없죠.

`Integer`는 없으면 `null`이라 `@NotNull`로 잡을 수 있습니다. **요청 DTO에서는 래퍼 타입을 쓰는 게 안전합니다.**

반대로 PATCH(부분 수정) 요청에서는 `null`이 "이 필드는 안 바꿈"을 뜻합니다.

```java
// products/dto/ProductUpdateRequest.java — 전부 선택
private Integer price;   // null이면 가격 유지
```

```java
// products/entity/Product.java
public void update(Category category, String title, ..., Integer price, ...) {
    if (price != null) this.price = price;   // null이면 건드리지 않음
}
```

---

## 7. 응답 봉투 — `ApiResponse`

모든 응답을 같은 모양으로 감쌉니다.

```java
// global/common/ApiResponse.java
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)   // null인 필드는 JSON에서 숨김
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorBody error;

    public static <T> ApiResponse<T> success(T data) { return new ApiResponse<>(true, data, null); }
    public static ApiResponse<Void> success()        { return new ApiResponse<>(true, null, null); }
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }
}
```

성공하면:
```json
{ "success": true, "data": { "productId": 1042, "status": "PENDING_INBOUND" } }
```

실패하면:
```json
{ "success": false, "error": { "code": "PRODUCT_404", "message": "존재하지 않는 상품입니다." } }
```

### 장점

프론트가 **한 가지 형태만** 처리하면 됩니다.

```typescript
// frontend/src/api/client.ts
if (!response.ok || payload.success === false) {
    throw new ApiError(response.status, payload.error?.code ?? "UNKNOWN", payload.error?.message ?? "...");
}
return payload.data as T;
```

이 코드 한 번으로 45개 API의 에러 처리가 끝납니다.

### 단점도 있습니다

HTTP는 이미 상태 코드로 성공/실패를 표현합니다. 봉투는 그걸 한 번 더 표현하는 중복이죠. 그래서 `success` 필드를 안 쓰고 HTTP 상태 코드만 쓰는 방식(RESTful에 더 가까움)도 흔합니다.

**정답은 없습니다.** 중요한 건 **일관성**입니다. 우리는 봉투 방식을 택했고, 삭제 API의 204 No Content만 예외로 뒀습니다(본문이 아예 없어야 하므로).

### `@JsonInclude(NON_NULL)`

이게 없으면 성공 응답에 `"error": null`이 붙어서 나갑니다. 지저분하고 트래픽도 낭비되죠. `NON_NULL`은 null인 필드를 JSON에서 아예 빼줍니다.

---

## 8. 예외 처리 파이프라인

### 문제 상황

서비스 깊은 곳에서 "상품이 없다"를 발견했다고 합시다. 어떻게 404를 응답할까요?

```java
// ❌ 반환값으로 전달
public Optional<ProductDetailResponse> getDetail(Long id) { ... }
// → 컨트롤러마다 if (result.isEmpty()) return 404; 를 반복
```

```java
// ❌ 서비스가 HTTP를 안다
public ResponseEntity<?> getDetail(Long id) { ... }
// → 서비스가 HTTP에 의존. 배치에서 못 씀
```

### 우리 방식 — 예외를 던지고, 한 곳에서 변환

```
Service                     GlobalExceptionHandler          응답
   │                                │
   │ throw new BusinessException(   │
   │     ErrorCode.PRODUCT_NOT_FOUND)
   │───────────────────────────────▶│
   │                                │ ErrorCode에서 상태·코드·메시지 꺼냄
   │                                │────────────────────▶ 404
   │                                │  { success: false,
   │                                │    error: { code: "PRODUCT_404", ... } }
```

**세 조각**으로 되어 있습니다.

**① ErrorCode — 에러의 사전**

```java
// global/exception/ErrorCode.java
@Getter
public enum ErrorCode {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_404", "존재하지 않는 상품입니다."),
    PRODUCT_NOT_EDITABLE(HttpStatus.CONFLICT, "PRODUCT_409_1", "입고 확인 후에는 수정할 수 없습니다."),
    ...
    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

에러 하나가 **HTTP 상태 + 코드 + 메시지**를 함께 들고 있습니다. 이걸 한 곳에 모아두면:

- 모든 에러를 한눈에 볼 수 있음 (현재 43개)
- 메시지를 고칠 때 한 곳만 고치면 됨
- 프론트가 `code`로 분기할 수 있음 (`"EMAIL_DUPLICATED"`면 이메일 칸에 표시)

**② BusinessException — 의도적으로 던지는 예외**

```java
// global/exception/BusinessException.java
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

`RuntimeException`을 상속한 게 중요합니다. **체크 예외(`Exception`)였다면 모든 호출부에 `throws`나 `try-catch`를 써야** 합니다. 그리고 스프링은 **RuntimeException일 때만 자동 롤백**합니다.

**③ GlobalExceptionHandler — 한 곳에서 변환**

```java
// global/exception/GlobalExceptionHandler.java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode ec = e.getErrorCode();
        log.warn("BusinessException: {} - {}", ec.getCode(), ec.getMessage());
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.error(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)   // @Valid 실패
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("유효성 검증에 실패했습니다.");
        return badRequest(msg);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)   // ?conditionGrade=BEST
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return badRequest("파라미터 '" + e.getName() + "'의 값이 올바르지 않습니다: " + e.getValue());
    }

    @ExceptionHandler(Exception.class)   // 최후의 보루
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);   // 스택트레이스는 로그로만
        ErrorCode ec = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.error(ec.getCode(), ec.getMessage()));
    }
}
```

`@RestControllerAdvice`는 **모든 컨트롤러를 감싸는 공통 예외 처리기**입니다. 어느 컨트롤러에서 예외가 나든 여기로 옵니다.

### 로그 레벨을 다르게 한 이유

```java
log.warn("BusinessException: ...")    // 비즈니스 예외
log.error("Unexpected error", e)      // 예상 못한 예외
```

`BusinessException`은 **정상적인 흐름**입니다. "없는 상품을 조회했다"는 버그가 아니라 사용자 실수죠. 이걸 `error`로 찍으면 로그가 노이즈로 가득 차서 진짜 장애를 못 찾습니다.

반대로 `Exception`으로 떨어진 건 **우리가 예상 못한 상황**이라 스택트레이스를 남겨야 합니다.

### 최후의 보루에서 메시지를 감춘 이유

```java
return ResponseEntity.status(ec.getStatus())
        .body(ApiResponse.error(ec.getCode(), ec.getMessage()));   // "서버 내부 오류입니다."
```

`e.getMessage()`를 그대로 내보내면 안 됩니다. DB 테이블명, 쿼리, 파일 경로 같은 **내부 구조가 노출**되어 공격자에게 힌트를 줍니다. 사용자에게는 뭉뚱그린 메시지를, 개발자에게는 로그를 남기는 게 맞습니다.

---

## 9. 필터 단계 예외는 왜 따로 처리했나

01번 문서에서 다뤘지만 다시 짚습니다.

```
요청 → [필터들] → DispatcherServlet → 컨트롤러
         ▲                              ▲
         │                              └─ @RestControllerAdvice가 여기서만 동작
         └─ 401/403은 여기서 발생 → 못 잡음
```

그래서 `JwtAuthenticationEntryPoint`(401)와 `JwtAccessDeniedHandler`(403)를 만들어 **같은 JSON 형식**으로 직접 써줍니다.

```java
objectMapper.writeValue(response.getWriter(), ApiResponse.error(ec.getCode(), ec.getMessage()));
```

프론트 입장에서는 401이든 404든 똑같은 모양이라 처리가 하나로 통일됩니다.

---

## 10. 페이징 응답 — `PageResponse`

```java
// global/common/PageResponse.java
@Getter
public class PageResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

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
```

### 스프링의 `Page`를 그대로 쓰면 안 되나?

```json
// Page를 그대로 직렬화하면
{
  "content": [...],
  "pageable": { "sort": { "sorted": true, "unsorted": false, "empty": false },
                "offset": 0, "pageNumber": 0, "pageSize": 20, "paged": true, "unpaged": false },
  "totalPages": 7, "totalElements": 128, "last": false, "size": 20, "number": 0,
  "sort": {...}, "first": true, "numberOfElements": 20, "empty": false
}
```

- **내부 구조가 통째로 노출**됩니다. `pageable`, `unpaged` 같은 건 프론트가 쓸 일이 없습니다.
- **스프링 버전을 올리면 이 구조가 바뀔 수 있습니다.** 실제로 스프링 부트 3.3부터는 `Page` 직렬화에 경고가 뜹니다.

우리 DTO로 감싸면 우리가 형식을 통제합니다.

### `Function<E, T> mapper`

```java
PageResponse.of(page, ProductSummaryResponse::from)
```

"엔티티 Page를 받아서, 각 원소를 이 함수로 변환한 뒤 감싸라"는 뜻입니다. 제네릭 두 개(`E`=엔티티, `T`=DTO)와 함수형 인터페이스를 써서 **모든 도메인에서 재사용**할 수 있게 만들었습니다.

---

## 11. 요청 DTO를 객체로 묶기 — `@ModelAttribute`

상품 검색은 쿼리 파라미터가 9개입니다.

```java
// ❌ 파라미터를 다 나열하면
public ResponseEntity<...> search(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Integer minPrice,
        @RequestParam(required = false) Integer maxPrice,
        @RequestParam(required = false) String size,
        @RequestParam(required = false) ConditionGrade conditionGrade,
        @RequestParam(defaultValue = "latest") String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) { ... }
```

```java
// ✅ 객체로 묶기
@GetMapping
public ResponseEntity<ApiResponse<PageResponse<ProductSummaryResponse>>> search(
        @ModelAttribute ProductSearchCondition condition) { ... }
```

`@ModelAttribute`는 쿼리 파라미터를 객체의 setter로 채워 넣습니다. 그래서 `ProductSearchCondition`에는 `@Setter`와 기본 생성자가 있습니다(DTO라 엔티티와 달리 setter를 열어도 괜찮습니다).

기본값도 필드 초기화로 처리합니다.

```java
private String sort = "latest";
private int page = 0;
private int pageSize = 20;
```

---

## 12. 계층 간 의존 방향

```
Controller ──▶ Service ──▶ Repository ──▶ Entity
     │            │
     └────────────┴──▶ DTO
```

**화살표가 한 방향입니다.** 아래 계층은 위 계층을 몰라야 합니다.

- Service가 `HttpServletRequest`를 쓰면 ❌ (HTTP에 의존)
- Repository가 비즈니스 판단을 하면 ❌
- Entity가 Repository를 부르면 ❌

우리 코드에서 이 원칙이 지켜지는지 확인해 보세요. `ProductService`를 열어서 `import`에 `jakarta.servlet`이 있는지 보면 됩니다. (없습니다.)

> 💡 `MultipartFile`은 예외적으로 서비스가 받습니다. 엄밀히는 웹 기술이지만, 파일 업로드를 추상화하려면 별도 타입을 만들어야 해서 실무에서도 보통 그냥 씁니다. 완벽한 분리보다 실용성을 택한 지점입니다.

---

## 스스로 확인해보기

**Q1.** 엔티티를 컨트롤러에서 그대로 반환하면 생기는 문제를 3가지 이상 말해 보세요.

**Q2.** 요청 DTO에서 `int price` 대신 `Integer price`를 쓰는 이유는?

**Q3.** `BusinessException`이 `RuntimeException`을 상속한 이유 두 가지는?

**Q4.** 최후의 보루 핸들러에서 `e.getMessage()`를 응답에 담지 않는 이유는?

**Q5.** 목록용 DTO와 상세용 DTO를 나눈 이유는?

<details>
<summary>답 보기</summary>

**A1.** ① `User.password` 같은 민감 정보가 노출됨 ② 양방향 연관관계 직렬화 시 무한 루프(StackOverflowError) ③ LAZY 프록시가 직렬화 중 초기화되어 예상 못한 쿼리가 대량 발생 ④ 엔티티 필드명 변경이 곧 API 스펙 변경이 되어 클라이언트가 깨짐.

**A2.** `int`는 JSON에 값이 없을 때 0으로 채워져서 "안 보냄"과 "0원"을 구분할 수 없습니다. `Integer`는 null이 되므로 `@NotNull`로 누락을 잡을 수 있습니다.

**A3.** ① 체크 예외였다면 모든 호출부에 `throws`나 try-catch를 써야 해서 코드가 지저분해집니다 ② 스프링은 기본적으로 RuntimeException일 때만 트랜잭션을 롤백합니다.

**A4.** 예상 못한 예외의 메시지에는 DB 테이블명, SQL, 파일 경로 등 내부 구조가 담길 수 있어 공격자에게 정보를 주게 됩니다. 사용자에게는 일반적인 메시지를 주고 상세 내용은 서버 로그에만 남깁니다.

**A5.** 목록은 20~40건을 한 번에 보내는데 상품 설명(TEXT)과 이미지 전체 배열까지 실으면 응답 크기가 수 배로 커지고, 그걸 만들기 위한 쿼리도 늘어납니다. 화면이 실제로 쓰는 필드만 담는 게 맞습니다.

</details>

---

## 직접 해보면 좋은 것

1. `ProductController.getDetail()`의 반환 타입을 `Product`로 바꿔서 실행해 보세요. 무한 루프나 비밀번호 노출이 실제로 일어나는 걸 확인한 뒤 되돌리세요.
2. `test.http` 15번(`?conditionGrade=BEST`)을 실행해서 400이 오는지 보세요. `GlobalExceptionHandler`에 `handleTypeMismatch`가 없었다면 500이 났을 겁니다.
3. `ErrorCode.java`를 열어서 43개 코드를 훑어보세요. 도메인별로 어떤 실패 상황을 예상했는지가 그대로 드러납니다.
