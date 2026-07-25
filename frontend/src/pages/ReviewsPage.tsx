import Header from "../components/Header";
import MyPageSidebar from "../components/MyPageSidebar";
import { mockReviews, mockProfile } from "../mocks/data";
import "./ReviewsPage.css";

// 별점을 ★★★★☆ 형태로 만드는 헬퍼
function Stars({ rating }: { rating: number }) {
  return (
    <span className="stars">
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} className={n <= rating ? "star-on" : "star-off"}>
          ★
        </span>
      ))}
    </span>
  );
}

function ReviewsPage() {
  return (
    <div className="reviews-page">
      <Header loggedIn userName={mockProfile.nickname} />
      <div className="mypage-body">
        <MyPageSidebar />
        <main className="mypage-main">
          <h2 className="mypage-title">받은 리뷰</h2>

          {/* 평점 요약 */}
          <div className="review-summary">
            <div className="review-score">
              <span className="score-number">{mockReviews.averageRating}</span>
              <Stars rating={Math.round(mockReviews.averageRating)} />
            </div>
            <span className="review-summary-desc">
              거래 완료 건에 한해 작성된 리뷰 · 총 {mockReviews.totalCount}건
            </span>
          </div>

          {/* 리뷰 리스트 */}
          <div className="review-list">
            {mockReviews.content.map((r) => (
              <div className="review-card" key={r.reviewId}>
                <div className="review-card-head">
                  <div className="reviewer-info">
                    <div className="reviewer-avatar" />
                    <span className="reviewer-name">{r.reviewer}</span>
                    <Stars rating={r.rating} />
                  </div>
                  <span className="review-product">{r.productTitle}</span>
                </div>
                <p className="review-content">{r.content}</p>
              </div>
            ))}
          </div>
        </main>
      </div>
    </div>
  );
}

export default ReviewsPage;