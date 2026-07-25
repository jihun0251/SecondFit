import Header from "../components/Header";
import StepIndicator from "../components/StepIndicator";
import "./DeliveryPage.css";

const timeline = [
  { title: "상품이 배송 중이에요", sub: "CJ대한통운 · 송장 6234-1188-0092", time: "오늘 09:12", active: true },
  { title: "본사 출고 완료", sub: "7/2 18:40 · 물류센터", time: "", active: false },
  { title: "결제 완료", sub: "7/2 15:20", time: "", active: false },
];

function DeliveryPage() {
  return (
    <div className="delivery-page">
      <Header loggedIn userName="buyer_lee" />
      <div className="delivery-body">
        <StepIndicator
          current={3}
          labels={["결제완료", "출고", "배송중", "배송완료", "거래확정"]}
        />

        <div className="delivery-header">
          <h3 className="delivery-title">배송 현황</h3>
          <span className="badge-shipping">● 출고 · 배송중</span>
        </div>

        <div className="delivery-content">
          {/* 좌측 타임라인 */}
          <div className="timeline-card">
            {timeline.map((t, i) => (
              <div className="timeline-item" key={i}>
                <div className="timeline-marker">
                  <div className={`timeline-dot ${t.active ? "active" : ""}`} />
                  {i < timeline.length - 1 && <div className="timeline-line" />}
                </div>
                <div className="timeline-text">
                  <div className="timeline-top">
                    <span className={`timeline-title ${t.active ? "active" : ""}`}>
                      {t.title}
                    </span>
                    {t.time && <span className="timeline-time">{t.time}</span>}
                  </div>
                  <span className="timeline-sub">{t.sub}</span>
                </div>
              </div>
            ))}
          </div>

          {/* 우측 요약 */}
          <aside className="delivery-side">
            <div className="side-card side-product">
              <div className="side-thumb" />
              <div className="side-product-info">
                <span className="side-name">데님 트러커 자켓</span>
                <span className="side-price">92,000원</span>
              </div>
            </div>

            <div className="side-card">
              <h4 className="side-title">배송지</h4>
              <p className="side-text">이OO · 010-****-1234</p>
              <p className="side-text">서울시 마포구 OO로 12, 302호</p>
            </div>

            <button className="btn-confirm">수령 확인 · 거래 확정</button>
            <p className="confirm-note">확정 시 판매자 정산이 진행됩니다</p>
          </aside>
        </div>
      </div>
    </div>
  );
}

export default DeliveryPage;