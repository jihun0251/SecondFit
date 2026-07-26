-- =========================================================
--  SecondFit 시연용 시드 데이터
--
--  IntelliJ Database 탭 → secondfit@localhost → 쿼리 콘솔에서
--  전체 선택 후 실행(Ctrl+Enter)하면 됩니다.
--
--  ⚠️ 실행 전 확인할 것
--   1) 백엔드를 한 번 실행해서 테이블/컬럼이 만들어져 있어야 합니다.
--      (ddl-auto: update 가 inbounds, orders, wishlists 등 새 테이블과
--       products.suspended 같은 새 컬럼을 자동으로 만들어 줍니다)
--   2) 그 다음 IntelliJ Database 탭에서 secondfit 우클릭 → 새로고침(Ctrl+F5)
--      해야 IDE가 새 컬럼을 인식합니다. 안 하면 '열 suspended 해결 불가'로 빨갛게 뜹니다.
--   3) 판매자 계정(min@email.com)이 가입되어 있어야 합니다.
--   4) 여러 번 실행하면 상품이 중복 생성됩니다. 맨 아래 정리 쿼리를 먼저 돌리세요.
-- =========================================================

-- ---------------------------------------------------------
-- 1. 카테고리 (이미 있으면 건너뜀 — name이 UNIQUE라서 IGNORE가 먹습니다)
-- ---------------------------------------------------------
INSERT IGNORE INTO categories (parent_id, name, sort_order) VALUES
    (NULL, '아우터', 1),
    (NULL, '상의',   2),
    (NULL, '하의',   3),
    (NULL, '신발',   4),
    (NULL, '가방',   5);

-- 소분류 (대분류 id를 이름으로 찾아서 연결)
INSERT IGNORE INTO categories (parent_id, name, sort_order)
SELECT c.id, sub.name, sub.sort_order
FROM (
    SELECT '아우터' AS parent, '데님 자켓'  AS name, 1 AS sort_order UNION ALL
    SELECT '아우터', '코트',        2 UNION ALL
    SELECT '아우터', '패딩',        3 UNION ALL
    SELECT '상의',   '니트',        1 UNION ALL
    SELECT '상의',   '셔츠',        2 UNION ALL
    SELECT '상의',   '맨투맨',      3 UNION ALL
    SELECT '하의',   '데님 팬츠',   1 UNION ALL
    SELECT '하의',   '슬랙스',      2 UNION ALL
    SELECT '신발',   '스니커즈',    1 UNION ALL
    SELECT '가방',   '토트백',      1
) AS sub
JOIN categories c ON c.name = sub.parent AND c.parent_id IS NULL;

-- ---------------------------------------------------------
-- 2. 판매자 확인
--    없으면 여기서 멈춥니다. 먼저 회원가입부터 해주세요.
-- ---------------------------------------------------------
SET @seller_id = (SELECT id FROM users WHERE email = 'min@email.com' LIMIT 1);

-- 결과가 NULL이면 아래 INSERT들이 전부 실패합니다.
SELECT IF(@seller_id IS NULL,
          '❌ min@email.com 계정이 없습니다. 회원가입 먼저 해주세요.',
          CONCAT('✅ 판매자 id = ', @seller_id)) AS check_seller;

-- ---------------------------------------------------------
-- 3. 상품 12건
--    status='ON_SALE' 로 바로 넣습니다 → 입고 확인 없이 목록에 뜹니다.
--    description 앞의 [SEED] 표시는 나중에 정리할 때 찾기 쉬우라고 붙였습니다.
-- ---------------------------------------------------------
INSERT INTO products
    (seller_id, category_id, title, description, price, size, color,
     condition_grade, status, view_count, suspended, created_at, updated_at)
SELECT
    @seller_id,
    (SELECT id FROM categories WHERE name = p.cat LIMIT 1),
    p.title, CONCAT('[SEED] ', p.descr), p.price, p.sz, p.color,
    p.grade, 'ON_SALE', FLOOR(RAND() * 200), FALSE,
    -- 등록일을 조금씩 다르게 해서 "최신순" 정렬이 눈에 보이게 합니다
    DATE_SUB(NOW(), INTERVAL p.days_ago DAY),
    NOW()
FROM (
    SELECT '데님 자켓' AS cat, '빈티지 워싱 데님 트러커 자켓' AS title, 89000 AS price, 'M'  AS sz, '인디고 블루' AS color, 'LIKE_NEW' AS grade, 1  AS days_ago, '90년대 빈티지 데님 자켓입니다. 자연스러운 워싱과 페이드가 매력적이며 어깨선 상태가 양호합니다.' AS descr UNION ALL
    SELECT '코트',      '울 블렌드 오버코트',           124000, 'L',  '차콜 그레이', 'NEW',      2,  '울 70% 혼방 오버코트. 착용 3회 미만의 새상품급이며 보풀·오염 없습니다.' UNION ALL
    SELECT '데님 자켓', '코듀로이 셔츠 자켓',            56000, 'M',  '베이지',      'LIKE_NEW', 3,  '부드러운 코듀로이 셔츠 자켓. 가볍게 걸치기 좋은 간절기 아이템입니다.' UNION ALL
    SELECT '패딩',      '경량 패딩 베스트',              31000, 'S',  '블랙',        'FAIR',     4,  '경량 패딩 베스트. 지퍼 부근에 약간의 사용감이 있으나 기능상 문제 없습니다.' UNION ALL
    SELECT '맨투맨',    '플리스 후드집업',               45000, 'L',  '아이보리',    'GOOD',     5,  '도톰한 플리스 후드집업. 보온성이 좋고 데일리로 착용하기 좋습니다.' UNION ALL
    SELECT '아우터',    '레더 라이더 자켓',             156000, 'M',  '블랙',        'NEW',      6,  '소가죽 라이더 자켓. 거의 착용하지 않아 가죽 컨디션이 매우 좋습니다.' UNION ALL
    SELECT '슬랙스',    '치노 팬츠',                     33000, 'M',  '카키',        'FAIR',     7,  '베이직한 치노 팬츠. 밑단에 약간의 사용감이 있습니다.' UNION ALL
    SELECT '니트',      '캐시미어 혼방 니트',            67000, 'L',  '오트밀',      'LIKE_NEW', 8,  '캐시미어 혼방 니트. 보풀이 거의 없고 촉감이 부드럽습니다.' UNION ALL
    SELECT '셔츠',      '옥스포드 셔츠',                 24000, 'S',  '화이트',      'GOOD',     9,  '베이직 옥스포드 셔츠. 깔끔한 상태로 오피스룩에 적합합니다.' UNION ALL
    SELECT '데님 팬츠', '와이드 데님 팬츠',              48000, 'XL', '라이트 블루', 'NEW',      10, '와이드 핏 데님 팬츠. 새상품에 가까운 컨디션입니다.' UNION ALL
    SELECT '스니커즈',  '스웨이드 첼시 부츠',            72000, 'L',  '브라운',      'GOOD',     11, '스웨이드 첼시 부츠. 밑창 마모가 적고 상태가 좋습니다.' UNION ALL
    SELECT '토트백',    '캔버스 토트백',                 29000, 'M',  '네이비',      'FAIR',     12, '데일리 캔버스 토트백. 내부에 약간의 얼룩이 있습니다.'
) AS p
WHERE @seller_id IS NOT NULL;

-- ---------------------------------------------------------
-- 4. 상품 이미지
--    picsum.photos 외부 URL을 씁니다 → 파일 업로드 없이 바로 이미지가 보입니다.
--    (프론트의 resolveImageUrl은 http로 시작하면 그대로 사용합니다)
-- ---------------------------------------------------------

-- 대표 이미지 1장씩
INSERT INTO product_images (product_id, image_url, is_thumbnail, sort_order, created_at)
SELECT p.id,
       CONCAT('https://picsum.photos/seed/secondfit', p.id, '/600/800'),
       TRUE, 0, NOW()
FROM products p
WHERE p.description LIKE '[SEED]%'
  AND NOT EXISTS (SELECT 1 FROM product_images i WHERE i.product_id = p.id);

-- 추가 이미지 2장씩 (상세 화면 썸네일 확인용)
INSERT INTO product_images (product_id, image_url, is_thumbnail, sort_order, created_at)
SELECT p.id,
       CONCAT('https://picsum.photos/seed/secondfit', p.id, '-', n.i, '/600/800'),
       FALSE, n.i, NOW()
FROM products p
CROSS JOIN (SELECT 1 AS i UNION ALL SELECT 2) AS n
WHERE p.description LIKE '[SEED]%'
  AND (SELECT COUNT(*) FROM product_images i WHERE i.product_id = p.id) = 1;

-- ---------------------------------------------------------
-- 5. 입고 기록
--    API로 등록하면 자동 생성되지만 SQL로 직접 넣었으니 수동으로 맞춰줍니다.
--    이미 판매중이므로 CONFIRMED 상태로 넣습니다.
-- ---------------------------------------------------------
INSERT INTO inbounds (product_id, admin_id, status, tracking_no, confirmed_at, created_at)
SELECT p.id, NULL, 'CONFIRMED', NULL, NOW(), NOW()
FROM products p
WHERE p.description LIKE '[SEED]%'
  AND NOT EXISTS (SELECT 1 FROM inbounds i WHERE i.product_id = p.id);

-- ---------------------------------------------------------
-- 6. 확인
-- ---------------------------------------------------------
-- 이미지 개수는 GROUP BY 대신 스칼라 서브쿼리로 센다.
-- JOIN + GROUP BY를 쓰면 MySQL의 ONLY_FULL_GROUP_BY 모드에서
-- "ORDER BY에 쓴 created_at도 GROUP BY에 넣으라"는 에러가 난다.
SELECT p.id,
       p.title,
       p.price,
       p.`size`,
       p.condition_grade,
       p.status,
       c.name AS category,
       (SELECT COUNT(*) FROM product_images i WHERE i.product_id = p.id) AS image_count
FROM products p
LEFT JOIN categories c ON c.id = p.category_id
WHERE p.description LIKE '[SEED]%'
ORDER BY p.created_at DESC;


-- =========================================================
--  정리 쿼리 (시드 데이터만 삭제)
--  다시 넣고 싶을 때 이 블록만 선택해서 실행하세요.
-- =========================================================
-- DELETE FROM inbounds       WHERE product_id IN (SELECT id FROM products WHERE description LIKE '[SEED]%');
-- DELETE FROM wishlists      WHERE product_id IN (SELECT id FROM products WHERE description LIKE '[SEED]%');
-- DELETE FROM product_images WHERE product_id IN (SELECT id FROM products WHERE description LIKE '[SEED]%');
-- DELETE FROM products       WHERE description LIKE '[SEED]%';
