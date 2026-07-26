import { Link } from "react-router-dom";
import Header from "../components/Header";
import MyPageSidebar from "../components/MyPageSidebar";
import { useWishlist } from "../contexts/WishlistContext";
import { resolveImageUrl } from "../api/client";
import { PRODUCT_STATUS_LABEL } from "../api/types";
import "./LikesPage.css";

function LikesPage() {
  const { items, toggleWish, loading } = useWishlist();

  return (
    <div className="likes-page">
      <Header />
      <div className="mypage-body">
        <MyPageSidebar />
        <main className="mypage-main">
          <div className="mypage-header">
            <h2 className="mypage-title">찜 목록</h2>
            <span className="mypage-desc">가격 변동·판매완료 여부를 확인하세요</span>
          </div>

          {loading ? (
            <div className="likes-empty">불러오는 중...</div>
          ) : items.length === 0 ? (
            <div className="likes-empty">
              <p>찜한 상품이 없습니다.</p>
              <Link to="/products" className="btn-goshop">상품 둘러보기</Link>
            </div>
          ) : (
            <div className="likes-grid">
              {items.map((item) => {
                // ON_SALE이 아니면 더 이상 살 수 없는 상품
                const sold = item.status !== "ON_SALE";
                return (
                  <Link
                    to={`/products/${item.productId}`}
                    className={`like-card ${sold ? "sold" : ""}`}
                    key={item.productId}
                  >
                    <div
                      className="like-thumb"
                      style={
                        item.thumbnail
                          ? {
                              backgroundImage: `url(${resolveImageUrl(item.thumbnail)})`,
                              backgroundSize: "cover",
                              backgroundPosition: "center",
                            }
                          : undefined
                      }
                    >
                      <span className={`badge ${sold ? "badge-sold" : "badge-onsale"}`}>
                        ● {PRODUCT_STATUS_LABEL[item.status]}
                      </span>
                      <button
                        className="heart"
                        onClick={(e) => {
                          e.preventDefault();
                          e.stopPropagation();
                          void toggleWish(item.productId);
                        }}
                      >
                        ♥
                      </button>
                    </div>
                    <div className="like-info">
                      <span className="like-name">{item.title}</span>
                      <span className="like-price">
                        {item.price.toLocaleString()}원
                        {/* 찜한 시점 대비 가격이 내려갔으면 표시 */}
                        {item.priceChange < 0 && (
                          <span className="like-price-drop">
                            {" "}↓ {Math.abs(item.priceChange).toLocaleString()}원
                          </span>
                        )}
                      </span>
                    </div>
                  </Link>
                );
              })}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default LikesPage;
