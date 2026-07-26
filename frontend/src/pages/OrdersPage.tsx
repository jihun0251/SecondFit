import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import MyPageSidebar from "../components/MyPageSidebar";
import { orderApi, reviewApi } from "../api";
import { ORDER_STATUS_LABEL } from "../api/types";
import type { OrderStatus } from "../api/types";
import { useAction, useAsync } from "../hooks/useAsync";
import "./OrdersPage.css";

const tabs: { label: string; value?: OrderStatus }[] = [
  { label: "전체", value: undefined },
  { label: "결제완료", value: "PAID" },
  { label: "배송중", value: "SHIPPED" },
  { label: "배송완료", value: "DELIVERED" },
  { label: "거래완료", value: "CONFIRMED" },
];

function OrdersPage() {
  const [status, setStatus] = useState<OrderStatus | undefined>();
  const navigate = useNavigate();
  const { run, running } = useAction();

  const { data, loading, error, reload } = useAsync(
    () => orderApi.getMine({ status, page: 0, size: 50 }),
    [status]
  );

  const statusClass = (s: OrderStatus) => {
    if (s === "SHIPPED") return "st-shipping";
    if (s === "DELIVERED") return "st-delivered";
    if (s === "CANCELLED") return "st-cancelled";
    return "st-confirmed";
  };

  /** 상태에 따라 버튼이 달라진다 */
  const actionLabel = (s: OrderStatus) => {
    if (s === "PAID") return "주문 취소";
    if (s === "SHIPPED") return "배송 조회";
    if (s === "DELIVERED") return "거래 확정";
    if (s === "CONFIRMED") return "리뷰 작성";
    return "상세 보기";
  };

  const handleAction = async (orderId: number, s: OrderStatus) => {
    if (s === "SHIPPED" || s === "CANCELLED") {
      navigate(`/orders/${orderId}`);
      return;
    }

    if (s === "PAID") {
      const reason = window.prompt("취소 사유를 입력하세요 (선택)") ?? undefined;
      if (!window.confirm("주문을 취소할까요?")) return;
      await run(async () => {
        await orderApi.cancel(orderId, reason);
        await reload();
      }, "주문이 취소되었습니다.");
      return;
    }

    if (s === "DELIVERED") {
      if (!window.confirm("수령을 확인하고 거래를 확정할까요?\n확정하면 판매자 정산이 시작됩니다."))
        return;
      await run(async () => {
        await orderApi.confirm(orderId);
        await reload();
      }, "거래가 확정되었습니다.");
      return;
    }

    if (s === "CONFIRMED") {
      const ratingInput = window.prompt("평점을 입력하세요 (1~5)", "5");
      if (!ratingInput) return;
      const rating = Number(ratingInput);
      if (!Number.isInteger(rating) || rating < 1 || rating > 5) {
        alert("평점은 1~5 사이의 정수여야 합니다.");
        return;
      }
      const content = window.prompt("리뷰 내용 (선택)") ?? undefined;
      await run(async () => {
        await reviewApi.create(orderId, rating, content);
      }, "리뷰가 등록되었습니다.");
    }
  };

  const orders = data?.content ?? [];

  return (
    <div className="orders-page">
      <Header />
      <div className="mypage-body">
        <MyPageSidebar />
        <main className="mypage-main">
          <h2 className="mypage-title">구매 내역</h2>

          <div className="order-tabs">
            {tabs.map((t) => (
              <button
                key={t.label}
                className={`order-tab ${status === t.value ? "active" : ""}`}
                onClick={() => setStatus(t.value)}
              >
                {t.label}
              </button>
            ))}
          </div>

          {loading ? (
            <div className="order-list">불러오는 중...</div>
          ) : error ? (
            <div className="order-list">{error}</div>
          ) : orders.length === 0 ? (
            <div className="order-list">주문 내역이 없습니다.</div>
          ) : (
            <div className="order-list">
              {orders.map((o) => (
                <div className="order-row" key={o.orderId}>
                  <div className="order-row-thumb" />
                  <div className="order-row-info">
                    <span className="order-row-name">{o.title}</span>
                    <span className="order-row-meta">
                      {o.orderPrice.toLocaleString()}원 ·{" "}
                      {new Date(o.paidAt).toLocaleDateString("ko-KR")} 결제
                    </span>
                  </div>
                  <span className={`order-status ${statusClass(o.status)}`}>
                    ● {ORDER_STATUS_LABEL[o.status]}
                  </span>
                  <button
                    className={`order-action ${o.status === "DELIVERED" ? "primary" : ""}`}
                    disabled={running}
                    onClick={() => void handleAction(o.orderId, o.status)}
                  >
                    {actionLabel(o.status)}
                  </button>
                </div>
              ))}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default OrdersPage;
