import { useNavigate, useParams, Link } from "react-router-dom";
import Header from "../components/Header";
import { useLikes } from "../contexts/LikesContext";
import { mockProducts } from "../mocks/data";
import "./ProductDetailPage.css";

function ProductDetailPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const { isLiked, toggleLike } = useLikes();

  const product = mockProducts.find((p) => p.id === Number(productId));

  if (!product) {
    return (
      <div className="detail-page">
        <Header />
        <div className="detail-notfound">
          <p>존재하지 않는 상품입니다.</p>
          <Link to="/products" className="btn-back">
            상품 목록으로
          </Link>
        </div>
      </div>
    );
  }

  const liked = isLiked(product.id);

  return (
    <div className="detail-page">
      <Header />
      <div className="detail-body">
        {/* 좌측 이미지 */}
        <div className="detail-images">
          <div className="detail-main-image">대표 이미지</div>
          <div className="detail-thumbs">
            {[1, 2, 3, 4].map((n) => (
              <div className="detail-thumb" key={n} />
            ))}
          </div>
        </div>

        {/* 우측 정보 */}
        <div className="detail-info">
          <div className="detail-top">
            <span className="badge-onsale">● 판매중</span>
            <span className="detail-category">
              {product.category} · {product.subCategory}
            </span>
          </div>

          <h1 className="detail-name">{product.name}</h1>
          <p className="detail-price">{product.price.toLocaleString()}원</p>

          <div className="detail-spec">
            <div className="spec-row">
              <span className="spec-label">사이즈</span>
              <span className="spec-value">{product.size}</span>
            </div>
            <div className="spec-row">
              <span className="spec-label">상태 등급</span>
              <span className="spec-value">{product.grade}</span>
            </div>
            <div className="spec-row">
              <span className="spec-label">색상</span>
              <span className="spec-value">{product.color}</span>
            </div>
            <div className="spec-row">
              <span className="spec-label">판매자</span>
              <span className="spec-value">
                {product.seller} · ★ {product.sellerRating}
              </span>
            </div>
          </div>

          <h4 className="detail-desc-title">상품 설명</h4>
          <p className="detail-desc-text">{product.description}</p>

          <div className="detail-actions">
            <button
              className={`btn-like ${liked ? "liked" : ""}`}
              onClick={() => toggleLike(product.id)}
            >
              {liked ? "♥ 찜한 상품" : "♡ 찜하기"}
            </button>
            <button
              className="btn-buy"
              onClick={() => navigate(`/checkout/${product.id}`)}
            >
              바로 구매
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductDetailPage;