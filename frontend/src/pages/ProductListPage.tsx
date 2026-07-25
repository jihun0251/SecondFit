import { useState } from "react";
import { Link } from "react-router-dom";
import Header from "../components/Header";
import { useLikes } from "../contexts/LikesContext";
import { mockProducts } from "../mocks/data";
import "./ProductListPage.css";

const categories = ["아우터", "상의", "하의", "신발", "가방"];
const sizes = ["S", "M", "L", "XL"];
const gradeOptions = [
  { label: "S (새상품급)", value: "S급" },
  { label: "A (사용감 적음)", value: "A급" },
  { label: "B (사용감 있음)", value: "B급" },
];

function ProductListPage() {
  const [selectedCats, setSelectedCats] = useState<string[]>(["아우터"]);
  const [selectedSizes, setSelectedSizes] = useState<string[]>([]);
  const [selectedGrades, setSelectedGrades] = useState<string[]>([]);
  const [maxPrice, setMaxPrice] = useState("");
  const [sort, setSort] = useState<"latest" | "price">("latest");

  const { isLiked, toggleLike } = useLikes();

  const toggle = (
    list: string[],
    value: string,
    setter: (v: string[]) => void
  ) => {
    setter(
      list.includes(value) ? list.filter((v) => v !== value) : [...list, value]
    );
  };

  // 필터링
  let filtered = mockProducts.filter((p) => {
    if (selectedCats.length > 0 && !selectedCats.includes(p.category)) return false;
    if (selectedSizes.length > 0 && !selectedSizes.includes(p.size)) return false;
    if (selectedGrades.length > 0 && !selectedGrades.includes(p.grade)) return false;
    if (maxPrice && p.price > Number(maxPrice)) return false;
    return true;
  });

  // 정렬
  filtered = [...filtered].sort((a, b) =>
    sort === "price" ? a.price - b.price : b.id - a.id
  );

  const titleText =
    selectedCats.length === 0
      ? "전체"
      : selectedCats.length === 1
      ? selectedCats[0]
      : `${selectedCats[0]} 외 ${selectedCats.length - 1}개`;

  return (
    <div className="product-list-page">
      <Header />
      <div className="list-body">
        {/* 좌측 필터 */}
        <aside className="filter-sidebar">
          <div className="filter-group">
            <h4 className="filter-title">카테고리</h4>
            {categories.map((cat) => (
              <label className="filter-check" key={cat}>
                <input
                  type="checkbox"
                  checked={selectedCats.includes(cat)}
                  onChange={() => toggle(selectedCats, cat, setSelectedCats)}
                />
                {cat}
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
                  className={`size-chip ${
                    selectedSizes.includes(size) ? "active" : ""
                  }`}
                  onClick={() => toggle(selectedSizes, size, setSelectedSizes)}
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
            {gradeOptions.map((g) => (
              <label className="filter-check" key={g.value}>
                <input
                  type="checkbox"
                  checked={selectedGrades.includes(g.value)}
                  onChange={() =>
                    toggle(selectedGrades, g.value, setSelectedGrades)
                  }
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
              {titleText}
              <span className="list-count">· 판매중 {filtered.length}건</span>
            </h2>
            <div className="sort-buttons">
              <button
                className={`sort-btn ${sort === "latest" ? "active" : ""}`}
                onClick={() => setSort("latest")}
              >
                최신순
              </button>
              <button
                className={`sort-btn ${sort === "price" ? "active" : ""}`}
                onClick={() => setSort("price")}
              >
                낮은 가격순
              </button>
            </div>
          </div>

          {filtered.length === 0 ? (
            <div className="list-empty">조건에 맞는 상품이 없습니다.</div>
          ) : (
            <div className="list-grid">
              {filtered.map((p) => (
                <Link to={`/products/${p.id}`} className="list-card" key={p.id}>
                  <div className="list-thumb">
                    <span className="badge-onsale">● 판매중</span>
                    <button
                      className={`like-btn ${isLiked(p.id) ? "liked" : ""}`}
                      onClick={(e) => {
                        e.preventDefault();   // 카드 링크 이동 막기
                        e.stopPropagation();
                        toggleLike(p.id);
                      }}
                    >
                      {isLiked(p.id) ? "♥" : "♡"}
                    </button>
                  </div>
                  <div className="list-info">
                    <span className="list-name">{p.name}</span>
                    <div className="list-tags">
                      <span className="tag">{p.size}</span>
                      <span className="tag">{p.grade}</span>
                    </div>
                    <span className="list-price">
                      {p.price.toLocaleString()}원
                    </span>
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