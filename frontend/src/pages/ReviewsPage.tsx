import Header from "../components/Header";
import MyPageSidebar from "../components/MyPageSidebar";
import { userApi } from "../api";
import { useAsync } from "../hooks/useAsync";
import { useAuth } from "../contexts/AuthContext";
import "./ReviewsPage.css";

// 별점을 ★★★★☆ 형태로 만드는 헬퍼
function Stars({ rating }: { rating: number }) {
  return (
    <span className="stars">
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} className={n <= rating ? "star-on" : "star-off"}>★</span>
      ))}
    </span>
  );
}

function ReviewsPage() {
  const { user } = useAuth();

  // 내가 "받은" 리뷰 = 내가 판매자인 리뷰 목록
  const { data, loading, error } = useAsync(
    () => userApi.getReviews(user!.userId, 0, 50),
    [user?.userId]
  );

  return (
    <div className="reviews-page">
      <Header />
      <div className="mypage-body">
        <MyPageSidebar />
        <main className="mypage-main">
          <h2 className="mypage-title">받은 리뷰</h2>

          {loading ? (
            <div className="review-list">불러오는 중...</div>
          ) : error ? (
            <div className="review-list">{error}</div>
          ) : (
            <>
              {/* 평점 요약 */}
              <div className="review-summary">
                <div className="review-score">
                  <span className="score-number">{data?.averageRating ?? 0}</span>
                  <Stars rating={Math.round(data?.averageRating ?? 0)} />
                </div>
                <span className="review-summary-desc">
                  거래 완료 건에 한해 작성된 리뷰 · 총 {data?.totalCount ?? 0}건
                </span>
              </div>

              {/* 리뷰 리스트 */}
              <div className="review-list">
                {(data?.content.length ?? 0) === 0 ? (
                  <p>아직 받은 리뷰가 없습니다.</p>
                ) : (
                  data!.content.map((r) => (
                    <div className="review-card" key={r.reviewId}>
                      <div className="review-card-head">
                        <div className="reviewer-info">
                          <div className="reviewer-avatar" />
                          <span className="reviewer-name">{r.reviewer}</span>
                          <Stars rating={r.rating} />
                        </div>
                        <span className="review-product">{r.productTitle}</span>
                      </div>
                      <p className="review-content">{r.content ?? "(내용 없음)"}</p>
                    </div>
                  ))
                )}
              </div>
            </>
          )}
        </main>
      </div>
    </div>
  );
}

export default ReviewsPage;
