import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import StepIndicator from "../components/StepIndicator";
import "./CheckoutPage.css";

const methods = ["가상 카드결제", "무통장(모의)", "간편페이"];

function CheckoutPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const [method, setMethod] = useState(methods[0]);

  return (
    <div className="checkout-page">
      <Header loggedIn userName="buyer_lee" />
      <div className="checkout-body">
        <StepIndicator current={1} />

        <div className="checkout-content">
          {/* 좌측 */}
          <div className="checkout-left">
            <h3 className="checkout-section-title">주문 상품</h3>
            <div className="order-item">
              <div className="order-thumb" />
              <div className="order-info">
                <span className="order-name">빈티지 워싱 데님 트러커 자켓</span>
                <span className="order-meta">아우터 · M · A급</span>
              </div>
              <span className="order-price">89,000원</span>
            </div>

            <h3 className="checkout-section-title">
              결제 수단<span className="section-sub">· 모의 결제</span>
            </h3>
            <div className="method-buttons">
              {methods.map((m) => (
                <button
                  key={m}
                  className={`method-btn ${method === m ? "active" : ""}`}
                  onClick={() => setMethod(m)}
                >
                  {m}
                </button>
              ))}
            </div>

            <div className="checkout-notice">
              실제 결제 없이 상태만 결제완료로 전이되는 모의 결제입니다.
            </div>
          </div>

          {/* 우측 요약 */}
          <aside className="checkout-summary">
            <h3 className="summary-title">결제 금액</h3>
            <div className="summary-row">
              <span>상품 금액</span>
              <span>89,000원</span>
            </div>
            <div className="summary-row">
              <span>배송비</span>
              <span>3,000원</span>
            </div>
            <div className="summary-divider" />
            <div className="summary-total">
              <span>총 결제</span>
              <span className="total-price">92,000원</span>
            </div>
            <button
              className="btn-pay"
              onClick={() => navigate(`/checkout/${productId}/address`)}
            >
              결제하기
            </button>
          </aside>
        </div>
      </div>
    </div>
  );
}

export default CheckoutPage;