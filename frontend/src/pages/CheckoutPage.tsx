import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import StepIndicator from "../components/StepIndicator";
import { orderApi, paymentApi, productApi } from "../api";
import { CONDITION_LABEL } from "../api/types";
import type { PaymentMethod } from "../api/types";
import { useAction, useAsync } from "../hooks/useAsync";
import "./CheckoutPage.css";

const methods: { label: string; value: PaymentMethod }[] = [
  { label: "가상 카드결제", value: "CARD" },
  { label: "무통장(모의)", value: "BANK_TRANSFER" },
  { label: "간편페이", value: "MOCK" },
];

function CheckoutPage() {
  const { productId } = useParams();
  const id = Number(productId);
  const navigate = useNavigate();

  const [method, setMethod] = useState<PaymentMethod>("CARD");
  const { run, running } = useAction();

  const { data: product, loading, error } = useAsync(() => productApi.getDetail(id), [id]);

  /**
   * 결제하기 = 주문 생성 + 모의 결제 두 번의 호출.
   * 주문이 만들어지는 순간 상품은 ON_SALE → PAID로 바뀌어 다른 사람이 못 산다.
   */
  const handlePay = async () => {
    if (!product) return;

    await run(async () => {
      const order = await orderApi.create(product.productId);
      await paymentApi.pay(order.orderId, order.orderPrice, method);

      // 배송지 입력 화면으로 orderId를 넘긴다 (URL에는 productId만 있으므로)
      navigate(`/checkout/${product.productId}/address`, {
        state: { orderId: order.orderId },
        replace: true,
      });
    });
  };

  if (loading) {
    return (
      <div className="checkout-page">
        <Header />
        <div className="checkout-body">불러오는 중...</div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="checkout-page">
        <Header />
        <div className="checkout-body">{error ?? "상품을 찾을 수 없습니다."}</div>
      </div>
    );
  }

  return (
    <div className="checkout-page">
      <Header />
      <div className="checkout-body">
        <StepIndicator current={1} />

        <div className="checkout-content">
          {/* 좌측 */}
          <div className="checkout-left">
            <h3 className="checkout-section-title">주문 상품</h3>
            <div className="order-item">
              <div className="order-thumb" />
              <div className="order-info">
                <span className="order-name">{product.title}</span>
                <span className="order-meta">
                  {product.category?.name ?? "미분류"} · {product.size ?? "-"} ·{" "}
                  {CONDITION_LABEL[product.conditionGrade]}
                </span>
              </div>
              <span className="order-price">{product.price.toLocaleString()}원</span>
            </div>

            <h3 className="checkout-section-title">
              결제 수단<span className="section-sub">· 모의 결제</span>
            </h3>
            <div className="method-buttons">
              {methods.map((m) => (
                <button
                  key={m.value}
                  className={`method-btn ${method === m.value ? "active" : ""}`}
                  onClick={() => setMethod(m.value)}
                >
                  {m.label}
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
              <span>{product.price.toLocaleString()}원</span>
            </div>
            <div className="summary-row">
              <span>배송비</span>
              <span>무료</span>
            </div>
            <div className="summary-divider" />
            <div className="summary-total">
              <span>총 결제</span>
              {/* 서버가 주문 금액과 결제 금액이 다르면 거절하므로 상품가와 반드시 같아야 한다 */}
              <span className="total-price">{product.price.toLocaleString()}원</span>
            </div>
            <button className="btn-pay" onClick={handlePay} disabled={running}>
              {running ? "결제 처리 중..." : "결제하기"}
            </button>
          </aside>
        </div>
      </div>
    </div>
  );
}

export default CheckoutPage;
