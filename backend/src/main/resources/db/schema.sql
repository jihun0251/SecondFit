-- 이 파일은 SecondFit의 정식 스키마 정의(SoT)입니다.
-- 엔티티를 고치면 여기도 함께 갱신하세요.
-- 적용: mysql -u root -p secondfit < schema.sql

-- =========================================================
--  SecondFit DDL (MySQL 8.0)
--  위탁·입고 중개 모델 / 검수 없음
--  charset: utf8mb4, engine: InnoDB
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------
--  users : 회원 (USER / ADMIN)
-- ---------------------------------------------------------
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,          -- BCrypt 해시
    nickname      VARCHAR(50)  NOT NULL,
    role          ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    phone         VARCHAR(20)  NULL,
    profile_image VARCHAR(500) NULL,
    -- 판매자 정산 계좌. PATCH /users/me 명세에 있으나 최초 DDL에 누락되어 추가함.
    settlement_account VARCHAR(100) NULL,
    status        ENUM('ACTIVE','SUSPENDED','WITHDRAWN') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_nickname (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
--  categories : 카테고리 (상의/하의/아우터 등, 자기참조 계층)
-- ---------------------------------------------------------
CREATE TABLE categories (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    parent_id  BIGINT      NULL,
    name       VARCHAR(50) NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name),
    KEY idx_categories_parent (parent_id),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id)
        REFERENCES categories (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
--  products : 상품 (의류)
--  거래 상태 머신: 입고대기 → 판매중 → 결제완료 → 출고 → 배송완료 → 정산완료
-- ---------------------------------------------------------
CREATE TABLE products (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    seller_id     BIGINT       NOT NULL,             -- 판매자 (users.id)
    category_id   BIGINT       NULL,
    title         VARCHAR(150) NOT NULL,
    description   TEXT         NULL,
    price         INT          NOT NULL,             -- 원 단위
    size          VARCHAR(20)  NULL,                 -- S/M/L/XL, 사이즈 표기
    color         VARCHAR(30)  NULL,
    condition_grade ENUM('NEW','LIKE_NEW','GOOD','FAIR','POOR') NOT NULL DEFAULT 'GOOD',
    status        ENUM('PENDING_INBOUND','ON_SALE','PAID','SHIPPED','DELIVERED','SETTLED')
                  NOT NULL DEFAULT 'PENDING_INBOUND',
    view_count    INT          NOT NULL DEFAULT 0,
    -- AI 자동 태깅 제안값 (판매자 확인/수정 전 원본 예측 보관)
    ai_suggested_category VARCHAR(50)  NULL,
    ai_suggested_color    VARCHAR(30)  NULL,
    ai_confidence         DECIMAL(5,4) NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_products_seller (seller_id),
    KEY idx_products_category (category_id),
    KEY idx_products_status (status),
    KEY idx_products_created (created_at),
    CONSTRAINT fk_products_seller FOREIGN KEY (seller_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id)
        REFERENCES categories (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
--  product_images : 상품 이미지 (1:N, 대표 이미지 플래그)
-- ---------------------------------------------------------
CREATE TABLE product_images (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    product_id BIGINT       NOT NULL,
    image_url  VARCHAR(500) NOT NULL,
    is_thumbnail BOOLEAN     NOT NULL DEFAULT FALSE,  -- AI 추론 대상 대표 이미지
    sort_order INT          NOT NULL DEFAULT 0,
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_product_images_product (product_id),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
--  wishlists : 찜 (users N:M products)
-- ---------------------------------------------------------
CREATE TABLE wishlists (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    user_id    BIGINT   NOT NULL,
    product_id BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wishlists_user_product (user_id, product_id),
    KEY idx_wishlists_product (product_id),
    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlists_product FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
--  inbounds : 본사 입고 (판매자 발송 → 관리자 입고 확인)
--  검수 제거 모델: 입고 확인 시 곧바로 판매중 전환
-- ---------------------------------------------------------
CREATE TABLE inbounds (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    product_id    BIGINT   NOT NULL,
    admin_id      BIGINT   NULL,                 -- 입고 확인 처리 관리자
    status        ENUM('AWAITING','CONFIRMED') NOT NULL DEFAULT 'AWAITING',
    tracking_no   VARCHAR(50) NULL,              -- 판매자 → 본사 발송 송장
    confirmed_at  DATETIME NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_inbounds_product (product_id), -- 상품당 입고 1건
    KEY idx_inbounds_admin (admin_id),
    CONSTRAINT fk_inbounds_product FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_inbounds_admin FOREIGN KEY (admin_id)
        REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
--  orders : 주문 (구매자 결제 → 출고 → 배송 → 확정)
-- ---------------------------------------------------------
CREATE TABLE orders (
    id             BIGINT   NOT NULL AUTO_INCREMENT,
    product_id     BIGINT   NOT NULL,
    buyer_id       BIGINT   NOT NULL,
    order_price    INT      NOT NULL,            -- 결제 시점 가격 스냅샷
    status         ENUM('PAID','SHIPPED','DELIVERED','CONFIRMED','CANCELLED')
                   NOT NULL DEFAULT 'PAID',
    -- 배송지 정보
    -- ⚠️ NOT NULL이었으나 NULL 허용으로 변경함.
    --    거래 흐름상 POST /orders(주문 생성) 시점에는 배송지가 없고,
    --    이후 PATCH /orders/{id}/shipping에서 채워지기 때문에 NOT NULL이면 주문 생성 자체가 불가능하다.
    receiver_name  VARCHAR(50)  NULL,
    receiver_phone VARCHAR(20)  NULL,
    zipcode        VARCHAR(10)  NULL,
    address1       VARCHAR(255) NULL,
    address2       VARCHAR(255) NULL,
    memo           VARCHAR(255) NULL,            -- 배송 메모 (PATCH /orders/{id}/shipping)
    cancel_reason  VARCHAR(255) NULL,            -- 주문 취소 사유
    tracking_no    VARCHAR(50)  NULL,            -- 본사 → 구매자 출고 송장
    paid_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shipped_at     DATETIME NULL,
    delivered_at   DATETIME NULL,
    confirmed_at   DATETIME NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    -- ⚠️ uk_orders_product(유니크)에서 일반 인덱스로 변경함.
    --    주문 취소 시 상품이 ON_SALE로 복귀하는데, 유니크면 그 상품의 재주문이 막힌다.
    --    동시 구매 방지는 애플리케이션의 products.status(ON_SALE) 검사로 처리한다.
    KEY idx_orders_product (product_id),
    KEY idx_orders_buyer (buyer_id),
    KEY idx_orders_status (status),
    CONSTRAINT fk_orders_product FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id)
        REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
--  payments : 결제 (모의 결제, orders 1:1)
-- ---------------------------------------------------------
CREATE TABLE payments (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    order_id      BIGINT   NOT NULL,
    amount        INT      NOT NULL,
    method        ENUM('CARD','BANK_TRANSFER','MOCK') NOT NULL DEFAULT 'MOCK',
    status        ENUM('PAID','REFUNDED','FAILED') NOT NULL DEFAULT 'PAID',
    pg_tid        VARCHAR(100) NULL,             -- 모의 거래 식별자
    paid_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_order (order_id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
--  settlements : 정산 (거래 확정 후 판매자 정산, orders 1:1)
-- ---------------------------------------------------------
CREATE TABLE settlements (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    order_id      BIGINT   NOT NULL,
    seller_id     BIGINT   NOT NULL,
    admin_id      BIGINT   NULL,                 -- 정산 처리 관리자
    gross_amount  INT      NOT NULL,             -- 판매가
    fee_amount    INT      NOT NULL DEFAULT 0,   -- 수수료
    net_amount    INT      NOT NULL,             -- 실지급액
    status        ENUM('PENDING','COMPLETED') NOT NULL DEFAULT 'PENDING',
    settled_at    DATETIME NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_settlements_order (order_id),
    KEY idx_settlements_seller (seller_id),
    KEY idx_settlements_admin (admin_id),
    CONSTRAINT fk_settlements_order FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_settlements_seller FOREIGN KEY (seller_id)
        REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_settlements_admin FOREIGN KEY (admin_id)
        REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
--  reviews : 리뷰/평점 (거래 확정 건, order 1:1)
-- ---------------------------------------------------------
CREATE TABLE reviews (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    order_id    BIGINT   NOT NULL,
    reviewer_id BIGINT   NOT NULL,               -- 구매자
    seller_id   BIGINT   NOT NULL,               -- 대상 판매자
    rating      TINYINT  NOT NULL,               -- 1~5
    content     VARCHAR(1000) NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reviews_order (order_id),
    KEY idx_reviews_seller (seller_id),
    CONSTRAINT fk_reviews_order FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_seller FOREIGN KEY (seller_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
--  reports : 신고 (부적절 게시물)
-- ---------------------------------------------------------
CREATE TABLE reports (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    reporter_id  BIGINT   NOT NULL,
    product_id   BIGINT   NULL,                  -- 신고 대상 상품
    admin_id     BIGINT   NULL,                  -- 처리 관리자
    reason       ENUM('FAKE','ABUSE','PROHIBITED','SPAM','ETC') NOT NULL,
    detail       VARCHAR(500) NULL,
    status       ENUM('RECEIVED','REVIEWING','RESOLVED','REJECTED') NOT NULL DEFAULT 'RECEIVED',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at  DATETIME NULL,
    PRIMARY KEY (id),
    KEY idx_reports_reporter (reporter_id),
    KEY idx_reports_product (product_id),
    KEY idx_reports_admin (admin_id),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_product FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE SET NULL,
    CONSTRAINT fk_reports_admin FOREIGN KEY (admin_id)
        REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
