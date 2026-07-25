import { Link } from "react-router-dom";
import Header from "../components/Header";
import MyPageSidebar from "../components/MyPageSidebar";
import { useLikes } from "../contexts/LikesContext";
import { mockProducts } from "../mocks/data";
import "./LikesPage.css";

function LikesPage() {
  const { likedIds, toggleLike } = useLikes();

  // 찜한 id에 해당하는 상품만 추려내기
  const likedProducts = mockProducts.filter((p) => likedIds.includes(p.id));

  return (
    <div className="likes-page">
      <Header loggedIn userName="buyer_lee" />
      <div className="mypage-body">
        <MyPageSidebar />
        <main className="mypage-main">
          <div className="mypage-header">
            <h2 className="mypage-title">찜 목록</h2>
            <span className="mypage-desc">
              가격 변동·판매완료 여부를 확인하세요
            </span>
          </div>

          {likedProducts.length === 0 ? (
            <div className="likes-empty">
              <p>찜한 상품이 없습니다.</p>
              <Link to="/products" className="btn-goshop">
                상품 둘러보기
              </Link>
            </div>
          ) : (
            <div className="likes-grid">
              {likedProducts.map((p) => {
                const sold = p.status === "SOLD";
                return (
                  <Link
                    to={`/products/${p.id}`}
                    className={`like-card ${sold ? "sold" : ""}`}
                    key={p.id}
                  >
                    <div className="like-thumb">
                      <span
                        className={`badge ${sold ? "badge-sold" : "badge-onsale"}`}
                      >
                        ● {sold ? "판매완료" : "판매중"}
                      </span>
                      <button
                        className="heart"
                        onClick={(e) => {
                          e.preventDefault();
                          e.stopPropagation();
                          toggleLike(p.id);
                        }}
                      >
                        ♥
                      </button>
                    </div>
                    <div className="like-info">
                      <span className="like-name">{p.name}</span>
                      <span className="like-price">
                        {p.price.toLocaleString()}원
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