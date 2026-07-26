import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import StepIndicator from "../components/StepIndicator";
import { orderApi } from "../api";
import { ORDER_STATUS_LABEL } from "../api/types";
import type { OrderDetail } from "../api/types";
import { useAction, useAsync } from "../hooks/useAsync";
import "./DeliveryPage.css";

/** 주문 상태에 따라 StepIndicator의 현재 단계를 정한다 */
const stepOf: Record<string, number> = {
  PAID: 1,
  SHIPPED: 3,
  DELIVERED: 4,
  CONFIRMED: 5,
  CANCELLED: 1,
};

/** 타임라인은 최신 이벤트가 위로 오도록 뒤집어서 만든다 */
function buildTimeline(order: OrderDetail) {
  const events: { title: string; sub: string; time: string }[] = [];
  const { timeline, shipping } = order;

  if (timeline.paidAt) {
    events.push({
      title: "결제 완료",
      sub: `주문번호 ${order.orderId}`,
      time: new Date(timeline.paidAt).toLocaleString("ko-KR"),
    });
  }
  if (timeline.shippedAt) {
    events.push({
      title: "본사 출고 완료",
      sub: shipping.trackingNo ? `송장 ${shipping.trackingNo}` : "물류센터",
      time: new Date(timeline.shippedAt).toLocaleString("ko-KR"),
    });
  }
  if (timeline.deliveredAt) {
    events.push({
      title: "배송이 완료되었어요",
      sub: "수령 확인 후 거래를 확정해 주세요",
      time: new Date(timeline.deliveredAt).toLocaleString("ko-KR"),
    });
  }
  if (timeline.confirmedAt) {
    events.push({
      title: "거래 확정",
      sub: "판매자 정산이 진행됩니다",
      time: new Date(timeline.confirmedAt).toLocaleString("ko-KR"),
    });
  }

  return events.reverse();
}

function DeliveryPage() {
  const { orderId } = useParams();
  const id = Number(orderId);
  const navigate = useNavigate();
  const { run, running } = useAction();

  const { data: order, loading, error, reload } = useAsync(() => orderApi.getDetail(id), [id]);

  const handleConfirm = async () => {
    if (!window.confirm("수령을 확인하고 거래를 확정할까요?\n확정하면 판매자 정산이 시작됩니다."))
      return;
    await run(async () => {
      await orderApi.confirm(id);
      await reload();
    }, "거래가 확정되었습니다.");
  };

  if (loading) {
    return (
      <div className="delivery-page">
        <Header />
        <div className="delivery-body">불러오는 중...</div>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="delivery-page">
        <Header />
        <div className="delivery-body">
          <p>{error ?? "주문을 찾을 수 없습니다."}</p>
          <button className="btn-confirm" onClick={() => navigate("/my/orders")}>
            구매 내역으로
          </button>
        </div>
      </div>
    );
  }

  const timeline = buildTimeline(order);

  return (
    <div className="delivery-page">
      <Header />
      <div className="delivery-body">
        <StepIndicator
          current={stepOf[order.status] ?? 1}
          labels={["결제완료", "출고", "배송중", "배송완료", "거래확정"]}
        />

        <div className="delivery-header">
          <h3 className="delivery-title">배송 현황</h3>
          <span className="badge-shipping">● {ORDER_STATUS_LABEL[order.status]}</span>
        </div>

        <div className="delivery-content">
          {/* 좌측 타임라인 */}
          <div className="timeline-card">
            {timeline.length === 0 ? (
              <p>아직 배송 이력이 없습니다.</p>
            ) : (
              timeline.map((t, i) => (
                <div className="timeline-item" key={t.title}>
                  <div className="timeline-marker">
                    <div className={`timeline-dot ${i === 0 ? "active" : ""}`} />
                    {i < timeline.length - 1 && <div className="timeline-line" />}
                  </div>
                  <div className="timeline-text">
                    <div className="timeline-top">
                      <span className={`timeline-title ${i === 0 ? "active" : ""}`}>
                        {t.title}
                      </span>
                      <span className="timeline-time">{t.time}</span>
                    </div>
                    <span className="timeline-sub">{t.sub}</span>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* 우측 요약 */}
          <aside className="delivery-side">
            <div className="side-card side-product">
              <div className="side-thumb" />
              <div className="side-product-info">
                <span className="side-name">{order.product.title}</span>
                <span className="side-price">{order.orderPrice.toLocaleString()}원</span>
              </div>
            </div>

            <div className="side-card">
              <h4 className="side-title">배송지</h4>
              {order.shipping.receiverName ? (
                <>
                  <p className="side-text">
                    {order.shipping.receiverName} · {order.shipping.receiverPhone}
                  </p>
                  <p className="side-text">
                    ({order.shipping.zipcode}) {order.shipping.address1} {order.shipping.address2 ?? ""}
                  </p>
                  {order.shipping.memo && <p className="side-text">메모: {order.shipping.memo}</p>}
                </>
              ) : (
                <p className="side-text">배송지가 아직 입력되지 않았습니다.</p>
              )}
            </div>

            {/* 배송완료 상태에서만 확정할 수 있다 (서버도 409로 막는다) */}
            {order.status === "DELIVERED" && (
              <>
                <button className="btn-confirm" onClick={handleConfirm} disabled={running}>
                  수령 확인 · 거래 확정
                </button>
                <p className="confirm-note">확정 시 판매자 정산이 진행됩니다</p>
              </>
            )}
            {order.status === "CONFIRMED" && (
              <p className="confirm-note">거래가 확정된 주문입니다.</p>
            )}
          </aside>
        </div>
      </div>
    </div>
  );
}

export default DeliveryPage;
