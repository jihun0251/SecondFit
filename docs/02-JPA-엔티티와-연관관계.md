# 02. JPA — 엔티티와 연관관계

> 오늘 만든 엔티티는 11개입니다. 이 문서를 읽고 나면 `@ManyToOne(fetch = FetchType.LAZY)` 같은 줄이 왜 붙어 있는지 설명할 수 있게 됩니다.

---

## 1. JPA가 뭘 해결하려는 건가

JPA 없이 DB를 쓰면 이렇습니다.

```java
// JDBC 직접 사용
String sql = "SELECT id, title, price FROM products WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setLong(1, productId);
ResultSet rs = ps.executeQuery();

Product product = new Product();
if (rs.next()) {
    product.setId(rs.getLong("id"));
    product.setTitle(rs.getString("title"));
    product.setPrice(rs.getInt("price"));
}
```

컬럼이 20개면 이 매핑 코드가 20줄입니다. 테이블이 11개면... 끔찍하죠.

**JPA는 "자바 객체 ↔ DB 테이블" 변환을 자동화합니다.** 우리는 "이 클래스는 이 테이블에 대응한다"고 선언만 하고, 나머지는 Hibernate(JPA 구현체)가 처리합니다.

```java
Product product = productRepository.findById(productId).orElseThrow(...);
// SQL 작성 없음, 매핑 코드 없음
```

이런 도구를 **ORM(Object-Relational Mapping)** 이라고 합니다.

---

## 2. 엔티티 기본 구조

가장 단순한 엔티티부터 봅시다.

```java
// categories/entity/Category.java
@Entity
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_categories_name", columnNames = "name"),
        indexes = @Index(name = "idx_categories_parent", columnList = "parent_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;
    ...
}
```

| 어노테이션 | 뜻 |
|---|---|
| `@Entity` | "이 클래스는 DB 테이블과 매핑된다" |
| `@Table(name="categories")` | 대응하는 테이블 이름 |
| `@Id` | 기본키(PK) |
| `@GeneratedValue(IDENTITY)` | PK를 DB가 자동 생성 (MySQL의 AUTO_INCREMENT) |
| `@Column(nullable=false, length=50)` | NOT NULL, VARCHAR(50) |

### `@NoArgsConstructor(access = PROTECTED)` — 왜 이런 이상한 걸?

두 가지 요구가 충돌합니다.

1. **JPA는 기본 생성자를 요구합니다.** DB에서 읽은 데이터로 객체를 만들 때 리플렉션으로 `new Category()`를 호출해야 하거든요.
2. **하지만 아무나 빈 객체를 만들면 안 됩니다.** `new Category()`로 만들면 이름도 없고 아무것도 없는 반쪽짜리 객체가 생깁니다.

`PROTECTED`는 절충안입니다.
- JPA는 리플렉션을 쓰니까 protected여도 접근 가능 → 요구 1 충족
- 같은 패키지나 상속 관계가 아니면 못 부름 → 요구 2 충족

### `@Setter`가 없는 이유

엔티티에 `@Setter`를 붙이면 어디서든 상태를 바꿀 수 있습니다.

```java
product.setStatus(Product.Status.SETTLED);   // ❌ 어디서든 아무 상태로 점프 가능
```

이러면 "결제도 안 했는데 정산완료가 됐다" 같은 일이 벌어지고, **어디서 바뀌었는지 추적이 불가능**해집니다.

대신 우리는 **의미 있는 메서드**만 열어뒀습니다.

```java
product.markPaid();       // ON_SALE일 때만 PAID로 (아니면 예외)
product.increaseViewCount();
```

이게 03번 문서의 핵심 주제입니다.

---

## 3. 연관관계 — 테이블의 FK를 객체로 표현하기

DB에서는 이렇게 씁니다.

```sql
CREATE TABLE products (
    id        BIGINT,
    seller_id BIGINT NOT NULL,          -- users.id를 가리킴
    CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);
```

자바 객체로는 이렇게 표현합니다.

```java
// products/entity/Product.java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "seller_id", nullable = false)
private User seller;
```

`Long sellerId`가 아니라 **`User seller` 객체**를 들고 있습니다. 그래서 이렇게 쓸 수 있습니다.

```java
product.getSeller().getNickname()   // 조인 쿼리를 직접 안 써도 됨
```

### 카디널리티(개수 관계) 어노테이션

| 어노테이션 | 의미 | 예시 |
|---|---|---|
| `@ManyToOne` | 여러 개가 하나를 가리킴 | 상품 N개 → 판매자 1명 |
| `@OneToMany` | 하나가 여러 개를 가짐 | 상품 1개 → 이미지 N장 |
| `@OneToOne` | 1:1 | 주문 1건 → 결제 1건 |

---

## 4. 양방향 연관관계와 "주인"

Product와 ProductImage는 서로를 참조합니다.

```java
// Product 쪽 — "나는 이미지를 여러 장 가진다"
@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
@OrderBy("sortOrder ASC, id ASC")
private List<ProductImage> images = new ArrayList<>();

// ProductImage 쪽 — "나는 상품 하나에 속한다"
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "product_id", nullable = false)
private Product product;
```

### `mappedBy`가 뜻하는 것

여기서 초심자가 가장 많이 헤매는 부분입니다.

**DB에는 방향이 없습니다.** `product_images.product_id` 컬럼 하나만 있으면 양쪽 관계가 다 표현됩니다. 그런데 자바 객체는 양쪽에 필드가 있죠. 그럼 **JPA는 어느 쪽 필드를 보고 FK 값을 정해야 할까요?**

이걸 정하는 게 `mappedBy`입니다.

- **`mappedBy`가 없는 쪽 = 연관관계의 주인**. FK 컬럼을 실제로 관리합니다. (여기선 `ProductImage.product`)
- **`mappedBy`가 있는 쪽 = 거울**. "나는 `ProductImage`의 `product` 필드에 의해 매핑된다"는 뜻이고, **읽기 전용**입니다.

```java
// ❌ 이렇게만 하면 DB에 저장 안 됨
product.getImages().add(image);          // 거울 쪽만 건드림

// ✅ 주인 쪽을 설정해야 FK가 들어감
image.assignProduct(product);
```

**항상 FK 컬럼을 가진 쪽(`@ManyToOne`이 붙은 쪽)이 주인입니다.**

### 연관관계 편의 메서드

양쪽 다 챙기는 걸 매번 기억하기 어려우니, 한 메서드로 묶습니다.

```java
// products/entity/Product.java
public void addImage(ProductImage image) {
    this.images.add(image);       // 자바 객체 그래프
    image.assignProduct(this);    // DB FK
}
```

```java
// products/entity/ProductImage.java
/** Product.addImage()에서만 호출한다 (연관관계 편의 메서드 전용) */
void assignProduct(Product product) {   // ← package-private
    this.product = product;
}
```

`assignProduct`를 **package-private**(접근 제어자 없음)으로 둔 게 포인트입니다. 같은 패키지의 `Product`만 부를 수 있고, 서비스 코드에서는 못 부릅니다. **"이미지를 추가하려면 반드시 `product.addImage()`를 써라"** 를 컴파일러가 강제해 줍니다.

---

## 5. 지연 로딩(LAZY) — 가장 중요한 개념

```java
@ManyToOne(fetch = FetchType.LAZY)
private User seller;
```

`FetchType`은 **"연관된 객체를 언제 DB에서 가져올까"** 를 정합니다.

| 방식 | 동작 |
|---|---|
| `EAGER` (즉시) | 상품을 조회할 때 판매자도 **무조건 같이** SELECT |
| `LAZY` (지연) | 상품만 SELECT. `getSeller()`를 실제로 부를 때 그때 SELECT |

### LAZY는 어떻게 동작하나 — 프록시

`LAZY`일 때 `product.getSeller()`가 반환하는 것은 진짜 `User`가 아니라 **프록시(가짜 객체)** 입니다.

```java
Product product = productRepository.findById(1L).get();
// SELECT * FROM products WHERE id = 1   ← 이것만 실행됨

User seller = product.getSeller();
// 아직 쿼리 안 나감. seller는 껍데기(프록시)

String nickname = seller.getNickname();
// ⭐ 이 순간 SELECT * FROM users WHERE id = ? 실행됨
```

프록시는 "진짜 값이 필요해질 때까지 기다렸다가, 필요해지면 그때 DB를 다녀오는" 대리인입니다.

### 왜 기본을 LAZY로 하나?

`EAGER`를 쓰면 이런 일이 벌어집니다.

```
Product를 조회
  → seller(User)도 즉시 조회
  → category(Category)도 즉시 조회
      → category의 parent도 즉시 조회
          → parent의 parent도...
```

**상품 제목 하나만 보고 싶었는데 테이블 5개가 조인됩니다.** 목록 20건이면 이게 20번 반복되고요.

`@ManyToOne`과 `@OneToOne`은 **기본값이 EAGER**라서 반드시 명시적으로 LAZY를 써줘야 합니다. (`@OneToMany`는 기본이 LAZY입니다.)

> 📌 **우리 코드에서 `@ManyToOne`이나 `@OneToOne`을 쓸 때 `fetch = FetchType.LAZY`가 빠짐없이 붙어 있는지 확인해 보세요.** 하나라도 빠지면 그 지점에서 성능이 새기 시작합니다.

### LAZY의 함정 — LazyInitializationException

프록시가 진짜 데이터를 가져오려면 **DB 연결이 살아 있어야** 합니다. 트랜잭션이 끝난 뒤에 `getSeller()`를 부르면 터집니다.

```java
// ❌ 트랜잭션 밖에서 프록시 초기화 시도
public Product getProduct(Long id) {   // @Transactional 없음
    Product p = productRepository.findById(id).get();
    return p;   // 트랜잭션 종료
}
// 나중에 컨트롤러에서 p.getSeller().getNickname() → LazyInitializationException 💥
```

**우리는 이걸 어떻게 피했나?** DTO 변환을 **서비스 안**(트랜잭션 안)에서 합니다.

```java
@Transactional
public ProductDetailResponse getDetail(Long productId) {
    Product product = productRepository.findById(productId).orElseThrow(...);
    product.increaseViewCount();
    return ProductDetailResponse.from(product);   // ⭐ 여기서 프록시가 다 풀림
}   // 트랜잭션 종료 — 이미 DTO에 값이 복사됐으니 안전
```

`ProductDetailResponse.from()` 안에서 `p.getSeller().getNickname()`, `p.getImages()` 등을 다 읽어서 값을 복사합니다. 컨트롤러로 나갈 땐 이미 순수 데이터만 담긴 DTO라 프록시 문제가 없습니다.

### `open-in-view: false`

```yaml
spring:
  jpa:
    open-in-view: false
```

스프링 부트는 기본적으로 **뷰 렌더링이 끝날 때까지 영속성 컨텍스트를 열어둡니다**(OSIV). 그러면 컨트롤러에서 프록시를 건드려도 에러가 안 납니다. 편해 보이죠?

**하지만 DB 커넥션을 요청이 끝날 때까지 붙잡고 있습니다.** 커넥션 풀이 금방 고갈됩니다. 그리고 "어디서 쿼리가 나가는지" 추적이 어려워집니다.

우리는 이걸 껐습니다. **불편함을 감수하는 대신, 트랜잭션 경계 안에서 필요한 데이터를 다 챙기는 습관**을 강제한 겁니다.

---

## 6. cascade와 orphanRemoval

```java
@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ProductImage> images = new ArrayList<>();
```

### cascade — 부모의 작업을 자식에게 전파

```java
Product product = Product.builder()...build();
product.addImage(image1);
product.addImage(image2);

productRepository.save(product);   // ⭐ product만 저장했는데
// INSERT INTO products ...
// INSERT INTO product_images ...   ← 이미지도 같이 저장됨
// INSERT INTO product_images ...
```

`CascadeType.ALL`은 저장·수정·삭제가 전부 전파된다는 뜻입니다. `productRepository.delete(product)`를 하면 이미지도 같이 삭제됩니다.

### orphanRemoval — 부모에게서 떨어진 자식은 삭제

```java
product.getImages().remove(image);   // 리스트에서 뺐을 뿐인데
// DELETE FROM product_images WHERE id = ?   ← DB에서도 삭제됨
```

"부모와의 연결이 끊긴 자식(고아, orphan)은 존재 이유가 없으니 지운다"는 뜻입니다.

상품 이미지는 상품 없이 혼자 존재할 이유가 없으니 딱 맞는 설정입니다.

> ⚠️ **주의**: cascade는 **JPA가 아는 연관관계에만** 적용됩니다. Product 엔티티에 `Inbound`나 `Wishlist`로 가는 필드가 없으면, 상품을 지울 때 그것들은 안 지워집니다. → **오늘 이것 때문에 버그가 났습니다. 07번 문서 참고.**

---

## 7. 자기 참조 — 카테고리 계층

카테고리는 자기 자신을 참조합니다.

```java
// categories/entity/Category.java

// 부모 방향: 나는 하나의 부모를 가진다
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private Category parent;

// 자식 방향: 나는 여러 자식을 가진다
@OneToMany(mappedBy = "parent")
@OrderBy("sortOrder ASC, id ASC")
private List<Category> children = new ArrayList<>();
```

```
아우터 (parent_id = NULL)
 ├ 데님 자켓 (parent_id = 아우터.id)
 ├ 코트
 └ 패딩
```

### `@OrderBy`를 왜 붙였나

이게 없으면 **자식들의 순서가 DB가 돌려주는 순서에 맡겨집니다.** MySQL은 순서를 보장하지 않아서, 새로고침할 때마다 카테고리 순서가 바뀔 수 있습니다.

```java
@OrderBy("sortOrder ASC, id ASC")   // 엔티티의 필드명 기준 (컬럼명 아님)
```

> ⚠️ **헷갈리기 쉬움**: JPA의 `@OrderBy`는 **엔티티 필드명**을 씁니다(`sortOrder`). SQL의 `ORDER BY`는 컬럼명(`sort_order`)이고요. 다른 어노테이션인 `@org.hibernate.annotations.OrderBy`는 SQL 문법을 쓰니 더 헷갈립니다.

---

## 8. 시간 자동 기록

```java
@CreationTimestamp
@Column(name = "created_at", updatable = false)
private LocalDateTime createdAt;

@UpdateTimestamp
@Column(name = "updated_at")
private LocalDateTime updatedAt;
```

- `@CreationTimestamp`: INSERT할 때 현재 시각을 자동으로 넣음
- `@UpdateTimestamp`: UPDATE할 때마다 갱신
- `updatable = false`: 한 번 정해진 생성 시각은 절대 바뀌지 않도록 UPDATE문에서 제외

이걸 안 쓰면 모든 서비스 메서드에서 `product.setCreatedAt(LocalDateTime.now())`를 빼먹지 않고 써야 합니다.

---

## 9. Enum 매핑 — `@Enumerated`

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private Status status;

public enum Status {PENDING_INBOUND, ON_SALE, PAID, SHIPPED, DELIVERED, SETTLED}
```

### `STRING`과 `ORDINAL`의 차이 — 반드시 STRING을 쓰세요

| 방식 | DB에 저장되는 값 |
|---|---|
| `EnumType.ORDINAL` (기본값!) | 0, 1, 2, 3... (선언 순서) |
| `EnumType.STRING` | `"PENDING_INBOUND"`, `"ON_SALE"`... |

`ORDINAL`은 **재앙**입니다. 나중에 enum 중간에 값을 하나 추가하면:

```java
// 기존
enum Status { PENDING_INBOUND, ON_SALE, PAID }   // 0, 1, 2

// 중간에 추가
enum Status { PENDING_INBOUND, INSPECTING, ON_SALE, PAID }   // 0, 1, 2, 3
                               ^^^^^^^^^^ 새로 추가
```

DB에 `1`로 저장돼 있던 기존 데이터가 `ON_SALE`에서 **`INSPECTING`으로 의미가 바뀌어 버립니다.** 데이터를 건드리지도 않았는데요.

**`EnumType.ORDINAL`이 기본값**이라는 게 함정입니다. `@Enumerated`를 아예 안 쓰면 ORDINAL이 됩니다. 항상 `@Enumerated(EnumType.STRING)`을 명시하세요.

---

## 10. Builder 패턴

```java
@Builder
private Product(User seller, Category category, String title, String description,
                int price, String size, String color, ConditionGrade conditionGrade,
                String aiSuggestedCategory, String aiSuggestedColor, BigDecimal aiConfidence) {
    this.seller = seller;
    ...
    this.status = Status.PENDING_INBOUND;   // ⭐ 항상 입고 대기로 시작
    this.viewCount = 0;
}
```

```java
Product product = Product.builder()
        .seller(seller)
        .title("빈티지 데님 자켓")
        .price(89000)
        .conditionGrade(ConditionGrade.LIKE_NEW)
        .build();
```

### 왜 생성자 대신 Builder인가

파라미터가 11개인 생성자를 직접 부르면:

```java
new Product(seller, category, "제목", "설명", 89000, "M", "블루", LIKE_NEW, null, null, null);
//                                                     ^^^  ^^^^^^ 어느 게 size고 어느 게 color?
```

순서를 하나만 바꿔도 **컴파일은 되는데 값이 뒤바뀝니다.** 타입이 같으면(String, String) 컴파일러가 못 잡아냅니다.

Builder는 이름을 붙여서 넣으니 실수가 없고, 선택적 값(`color` 없이 만들기)도 자연스럽습니다.

### 생성자를 `private`으로 둔 이유

```java
@Builder
private Product(...) { ... }
```

`private`이라 외부에서는 **Builder를 통해서만** 만들 수 있습니다. 그리고 Builder 파라미터에 **`status`와 `viewCount`가 없습니다.**

```java
Product.builder()
        .status(Product.Status.SETTLED)   // ❌ 컴파일 에러 — 그런 메서드 없음
```

즉 **"상품은 반드시 PENDING_INBOUND로 시작한다"** 는 규칙을 컴파일 타임에 강제합니다. 이런 게 좋은 설계입니다 — 문서로 "이렇게 하세요"라고 적는 것보다, 애초에 다르게 할 수 없게 만드는 게 낫습니다.

---

## 11. `ddl-auto` — 편하지만 위험한 옵션

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

| 값 | 동작 |
|---|---|
| `none` | 아무것도 안 함 |
| `validate` | 엔티티와 테이블이 맞는지 검사만. 다르면 부팅 실패 |
| `update` | 없는 테이블·컬럼을 자동으로 추가 (기존 것은 안 건드림) |
| `create` | 시작할 때 테이블을 **전부 지우고** 새로 만듦 |
| `create-drop` | create + 종료 시 삭제 |

우리는 `update`를 씁니다. 엔티티에 필드를 추가하면 서버 재시작만으로 컬럼이 생겨서 개발할 때 편합니다.

### 하지만 이런 문제가 있습니다

**(1) 지훈님이 오늘 겪은 문제**
제가 `Product`에 `suspended` 필드를 추가했는데, 서버를 재시작하지 않아서 DB에 컬럼이 없었습니다. 그래서 시드 SQL이 "열 suspended 해결 불가"로 실패했죠. `update`는 **애플리케이션이 뜰 때만** 반영됩니다.

**(2) 삭제·변경은 반영 안 됨**
필드를 지우거나 타입을 바꿔도 `update`는 아무것도 안 합니다. 쓰지 않는 컬럼이 계속 쌓입니다.

**(3) 우리가 쓴 DDL과 달라짐**
`schema.sql`에는 `ENUM('NEW','LIKE_NEW',...)`이라고 썼지만, Hibernate가 만드는 건 `VARCHAR(20)`입니다. 문서와 실제가 어긋납니다.

**(4) 운영에서는 절대 금지**
운영 DB에 `update`를 켜두면, 실수로 필드 이름을 바꿨을 때 새 컬럼이 생기고 기존 데이터가 고립됩니다.

> 📌 **실무 방식**: 운영은 `validate`로 두고, 스키마 변경은 **Flyway**나 **Liquibase** 같은 마이그레이션 도구로 버전 관리합니다. `V1__create_products.sql`, `V2__add_suspended.sql` 같은 파일을 순서대로 실행하는 방식이라 되돌리기도 쉽습니다.
>
> 우리 프로젝트에서 `schema.sql`을 저장소에 넣어둔 게 그 방향의 첫걸음입니다.

---

## 12. 오늘 만든 엔티티 관계도

```
       User ──────┬───────────────┐
        │         │               │
   (seller)   (buyer)         (reviewer)
        │         │               │
        ▼         ▼               ▼
    Product ◀── Order ────────▶ Review
        │         │  │
        │         │  └──▶ Payment (1:1)
        │         │
        │         └─────▶ Settlement (1:1) ──▶ User (seller)
        │
        ├──▶ ProductImage (1:N, cascade)
        ├──▶ Inbound (1:1)
        ├──▶ Wishlist (N:M via 중간 테이블)
        └──▶ Report (N:1)

    Category ──▶ Category (자기 참조)
```

Product를 참조하는 엔티티가 **5개**입니다(ProductImage, Inbound, Order, Wishlist, Report). 이게 07번 문서의 버그와 직결됩니다.

---

## 스스로 확인해보기

**Q1.** `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 쓰는 이유 두 가지는?

**Q2.** 양방향 연관관계에서 `mappedBy`가 붙은 쪽은 무엇을 의미하나요? 그쪽 컬렉션에 `add()`만 하면 왜 DB에 반영이 안 되나요?

**Q3.** `@Enumerated(EnumType.ORDINAL)`이 위험한 이유를 구체적인 시나리오로 설명해 보세요.

**Q4.** `FetchType.LAZY`인 필드를 트랜잭션 밖에서 접근하면 어떤 예외가 나나요? 우리 코드는 이걸 어떻게 피했나요?

**Q5.** `Product.builder()`에 `.status(...)` 메서드가 없는 이유는?

<details>
<summary>답 보기</summary>

**A1.** ① JPA가 리플렉션으로 객체를 만들 때 기본 생성자가 필요하다 ② 외부에서 `new Product()`로 아무 값도 없는 반쪽짜리 객체를 만드는 걸 막는다. PROTECTED가 두 요구를 동시에 만족시킵니다.

**A2.** `mappedBy`가 붙은 쪽은 **거울(비주인)** 이고 읽기 전용입니다. FK 컬럼을 실제로 관리하는 건 `@ManyToOne`이 붙은 주인 쪽입니다. 거울 쪽 컬렉션에만 add하면 자바 객체 그래프만 바뀌고 FK 값은 안 채워져서 DB에 반영되지 않습니다.

**A3.** enum 중간에 새 상수를 추가하면 그 뒤 상수들의 순서 번호가 전부 밀립니다. DB에 저장된 숫자는 그대로인데 의미하는 상수가 바뀌어서, 데이터를 건드리지 않았는데도 값이 통째로 오염됩니다.

**A4.** `LazyInitializationException`이 발생합니다. 우리는 DTO 변환(`XxxResponse.from()`)을 `@Transactional`이 걸린 서비스 메서드 **안에서** 수행해서, 트랜잭션이 살아 있는 동안 필요한 값을 전부 복사해 둡니다.

**A5.** `@Builder`를 붙인 생성자의 파라미터에 `status`가 없기 때문입니다. 생성자 안에서 `this.status = Status.PENDING_INBOUND`로 고정해서, "상품은 반드시 입고 대기 상태로 시작한다"는 규칙을 컴파일 타임에 강제합니다.

</details>

---

## 직접 해보면 좋은 것

1. `application.yaml`의 `show-sql: true` 덕분에 콘솔에 SQL이 찍힙니다. 상품 상세를 한 번 조회하고 **쿼리가 몇 개 나가는지** 세어 보세요.
2. `Product`의 `seller` 필드에서 `fetch = FetchType.LAZY`를 지우고(EAGER가 됨) 상품 목록을 조회해 보세요. 쿼리가 얼마나 늘어나는지 비교하면 LAZY의 필요성이 체감됩니다. **확인 후 반드시 되돌리세요.**
3. `ProductImage.assignProduct()`를 서비스 코드에서 호출해 보세요. 컴파일 에러가 나는 걸 확인하면 package-private의 의도가 이해됩니다.
