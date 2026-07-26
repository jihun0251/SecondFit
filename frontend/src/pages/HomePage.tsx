import { Link, useNavigate } from "react-router-dom";
import Header from "../components/Header";
import { categoryApi, productApi } from "../api";
import { resolveImageUrl } from "../api/client";
import { useAsync } from "../hooks/useAsync";
import { useWishlist } from "../contexts/WishlistContext";
import "./HomePage.css";

function HomePage() {
  const navigate = useNavigate();
  const { isWished, toggleWish } = useWishlist();

  // 최신 판매중 상품 5개
  const { data: products, loading, error } = useAsync(
    () => productApi.search({ sort: "latest", page: 0, pageSize: 5 }),
    []
  );

  // 카테고리 칩 (대분류만)
  const { data: categories } = useAsync(() => categoryApi.getTree(), []);

  return (
    <div className="home">
      <Header />

      <div className="home-content">
        {/* 히어로 배너 */}
        <section className="home-hero">
          <div className="home-hero-text">
            <span className="home-hero-label">SECONDFIT PICK</span>
            <h2 className="home-hero-title">입고 완료 · 지금 바로 판매중</h2>
            <p className="home-hero-desc">본사 검수 없이도 믿고 거래하는 위탁 마켓</p>
          </div>
          <div className="home-hero-image" />
        </section>

        {/* 카테고리 칩 */}
        <div className="category-chips">
          <button className="category-chip active" onClick={() => navigate("/products")}>
            전체
          </button>
          {(categories ?? []).map((cat) => (
            <button
              key={cat.id}
              className="category-chip"
              onClick={() => navigate(`/products?categoryId=${cat.id}`)}
            >
              {cat.name}
            </button>
          ))}
        </div>

        {/* 섹션 헤더 */}
        <div className="section-header">
          <div className="section-header-left">
            <h3 className="section-title">최신 등록 상품</h3>
            <span className="section-desc">입고 확인된 판매중 상품만 노출됩니다</span>
          </div>
          <Link to="/products" className="section-more">전체보기 →</Link>
        </div>

        {/* 상품 그리드 */}
        {loading ? (
          <div className="home-state">불러오는 중...</div>
        ) : error ? (
          <div className="home-state">{error}</div>
        ) : (products?.content.length ?? 0) === 0 ? (
          <div className="home-state">
            아직 판매중인 상품이 없습니다. 관리자 입고 확인이 끝나면 여기에 표시됩니다.
          </div>
        ) : (
          <div className="product-grid">
            {products!.content.map((product) => (
              <Link
                to={`/products/${product.productId}`}
                className="product-card"
                key={product.productId}
              >
                <div
                  className="product-thumb"
                  style={
                    product.thumbnail
                      ? {
                          backgroundImage: `url(${resolveImageUrl(product.thumbnail)})`,
                          backgroundSize: "cover",
                          backgroundPosition: "center",
                        }
                      : undefined
                  }
                >
                  <span className="product-badge">● 판매중</span>
                  <button
                    className={`product-like ${isWished(product.productId) ? "liked" : ""}`}
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      void toggleWish(product.productId);
                    }}
                  >
                    {isWished(product.productId) ? "♥" : "♡"}
                  </button>
                </div>
                <div className="product-info">
                  <span className="product-name">{product.title}</span>
                  <span className="product-price">{product.price.toLocaleString()}원</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default HomePage;
