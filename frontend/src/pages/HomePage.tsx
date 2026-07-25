import { Link } from "react-router-dom";
import Header from "../components/Header";
import { useLikes } from "../contexts/LikesContext";
import { mockProducts } from "../mocks/data";
import "./HomePage.css";

const categories = ["전체", "아우터", "상의", "하의", "신발", "가방", "악세서리"];

function HomePage() {
  const { isLiked, toggleLike } = useLikes();

  // 최신 5개만 노출
  const latest = [...mockProducts].sort((a, b) => b.id - a.id).slice(0, 5);

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
          {categories.map((cat, index) => (
            <button
              key={cat}
              className={`category-chip ${index === 0 ? "active" : ""}`}
            >
              {cat}
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
        <div className="product-grid">
          {latest.map((product) => (
            <Link
              to={`/products/${product.id}`}
              className="product-card"
              key={product.id}
            >
              <div className="product-thumb">
                <span className="product-badge">● 판매중</span>
                <button
                  className={`product-like ${isLiked(product.id) ? "liked" : ""}`}
                  onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    toggleLike(product.id);
                  }}
                >
                  {isLiked(product.id) ? "♥" : "♡"}
                </button>
              </div>
              <div className="product-info">
                <span className="product-name">{product.name}</span>
                <span className="product-price">
                  {product.price.toLocaleString()}원
                </span>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}

export default HomePage;