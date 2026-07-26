import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Header from "../components/Header";
import { categoryApi, productApi } from "../api";
import { resolveImageUrl } from "../api/client";
import { CONDITION_LABEL } from "../api/types";
import type { ConditionGrade } from "../api/types";
import { useAsync } from "../hooks/useAsync";
import { useWishlist } from "../contexts/WishlistContext";
import "./ProductListPage.css";

const sizes = ["S", "M", "L", "XL"];

const gradeOptions: { label: string; value: ConditionGrade }[] = [
  { label: "새상품", value: "NEW" },
  { label: "S (사용감 거의 없음)", value: "LIKE_NEW" },
  { label: "A (사용감 적음)", value: "GOOD" },
  { label: "B (사용감 있음)", value: "FAIR" },
];

function ProductListPage() {
  // 헤더 검색과 홈 카테고리 칩이 쿼리스트링으로 넘겨준다
  const [searchParams] = useSearchParams();

  const [categoryId, setCategoryId] = useState<number | undefined>();
  const [selectedSize, setSelectedSize] = useState<string | undefined>();
  const [selectedGrade, setSelectedGrade] = useState<ConditionGrade | undefined>();
  const [maxPrice, setMaxPrice] = useState("");
  const [sort, setSort] = useState<"latest" | "price_asc">("latest");
  const [keyword, setKeyword] = useState("");

  // URL이 바뀌면 필터에 반영
  useEffect(() => {
    const urlKeyword = searchParams.get("keyword") ?? "";
    const urlCategory = searchParams.get("categoryId");
    setKeyword(urlKeyword);
    setCategoryId(urlCategory ? Number(urlCategory) : undefined);
  }, [searchParams]);

  const { data: categories } = useAsync(() => categoryApi.getTree(), []);

  /**
   * 필터가 바뀔 때마다 서버에 다시 물어본다.
   * 예전에는 목업 배열을 브라우저에서 걸렀지만, 실제로는 상품이 수천 건일 수 있어서
   * 전부 받아와 거르는 방식은 쓸 수 없다.
   */
  const { data, loading, error } = useAsync(
    () =>
      productApi.search({
        keyword: keyword || undefined,
        categoryId,
        size: selectedSize,
        conditionGrade: selectedGrade,
        maxPrice: maxPrice ? Number(maxPrice) : undefined,
        sort,
        page: 0,
        pageSize: 40,
      }),
    [keyword, categoryId, selectedSize, selectedGrade, maxPrice, sort]
  );

  const { isWished, toggleWish } = useWishlist();

  const products = data?.content ?? [];
  const selectedCategoryName = categories?.find((c) => c.id === categoryId)?.name;

  return (
    <div className="product-list-page">
      <Header />
      <div className="list-body">
        {/* 좌측 필터 */}
        <aside className="filter-sidebar">
          <div className="filter-group">
            <h4 className="filter-title">카테고리</h4>
            <label className="filter-check">
              <input
                type="radio"
                name="category"
                checked={categoryId === undefined}
                onChange={() => setCategoryId(undefined)}
              />
              전체
            </label>
            {(categories ?? []).map((cat) => (
              <label className="filter-check" key={cat.id}>
                <input
                  type="radio"
                  name="category"
                  checked={categoryId === cat.id}
                  onChange={() => setCategoryId(cat.id)}
                />
                {cat.name}
              </label>
            ))}
          </div>

          <div className="filter-divider" />

          <div className="filter-group">
            <h4 className="filter-title">사이즈</h4>
            <div className="size-chips">
              {sizes.map((size) => (
                <button
                  key={size}
                  className={`size-chip ${selectedSize === size ? "active" : ""}`}
                  onClick={() => setSelectedSize(selectedSize === size ? undefined : size)}
                >
                  {size}
                </button>
              ))}
            </div>
          </div>

          <div className="filter-divider" />

          <div className="filter-group">
            <h4 className="filter-title">가격대</h4>
            <input
              className="price-input"
              placeholder="최대 금액 (예: 100000)"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
            />
          </div>

          <div className="filter-divider" />

          <div className="filter-group">
            <h4 className="filter-title">상태 등급</h4>
            <label className="filter-check">
              <input
                type="radio"
                name="grade"
                checked={selectedGrade === undefined}
                onChange={() => setSelectedGrade(undefined)}
              />
              전체
            </label>
            {gradeOptions.map((g) => (
              <label className="filter-check" key={g.value}>
                <input
                  type="radio"
                  name="grade"
                  checked={selectedGrade === g.value}
                  onChange={() => setSelectedGrade(g.value)}
                />
                {g.label}
              </label>
            ))}
          </div>
        </aside>

        {/* 우측 상품 영역 */}
        <main className="list-main">
          <div className="list-header">
            <h2 className="list-title">
              {keyword ? `"${keyword}" 검색 결과` : selectedCategoryName ?? "전체"}
              <span className="list-count">· 판매중 {data?.totalElements ?? 0}건</span>
            </h2>
            <div className="sort-buttons">
              <button
                className={`sort-btn ${sort === "latest" ? "active" : ""}`}
                onClick={() => setSort("latest")}
              >
                최신순
              </button>
              <button
                className={`sort-btn ${sort === "price_asc" ? "active" : ""}`}
                onClick={() => setSort("price_asc")}
              >
                낮은 가격순
              </button>
            </div>
          </div>

          {loading ? (
            <div className="list-empty">불러오는 중...</div>
          ) : error ? (
            <div className="list-empty">{error}</div>
          ) : products.length === 0 ? (
            <div className="list-empty">조건에 맞는 상품이 없습니다.</div>
          ) : (
            <div className="list-grid">
              {products.map((p) => (
                <Link to={`/products/${p.productId}`} className="list-card" key={p.productId}>
                  <div
                    className="list-thumb"
                    style={
                      p.thumbnail
                        ? {
                            backgroundImage: `url(${resolveImageUrl(p.thumbnail)})`,
                            backgroundSize: "cover",
                            backgroundPosition: "center",
                          }
                        : undefined
                    }
                  >
                    <span className="badge-onsale">● 판매중</span>
                    <button
                      className={`like-btn ${isWished(p.productId) ? "liked" : ""}`}
                      onClick={(e) => {
                        e.preventDefault(); // 카드 링크 이동 막기
                        e.stopPropagation();
                        void toggleWish(p.productId);
                      }}
                    >
                      {isWished(p.productId) ? "♥" : "♡"}
                    </button>
                  </div>
                  <div className="list-info">
                    <span className="list-name">{p.title}</span>
                    <div className="list-tags">
                      {p.size && <span className="tag">{p.size}</span>}
                      <span className="tag">{CONDITION_LABEL[p.conditionGrade]}</span>
                    </div>
                    <span className="list-price">{p.price.toLocaleString()}원</span>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default ProductListPage;
