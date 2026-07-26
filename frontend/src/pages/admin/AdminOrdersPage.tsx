import { useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import { adminApi } from "../../api";
import { ORDER_STATUS_LABEL } from "../../api/types";
import type { OrderStatus } from "../../api/types";
import { useAction, useAsync } from "../../hooks/useAsync";

const tabs: { label: string; value?: OrderStatus }[] = [
  { label: "전체", value: undefined },
  { label: "출고 대기", value: "PAID" },
  { label: "배송중", value: "SHIPPED" },
  { label: "배송완료", value: "DELIVERED" },
  { label: "거래완료", value: "CONFIRMED" },
];

function AdminOrdersPage() {
  const [status, setStatus] = useState<OrderStatus | undefined>("PAID");

  const { data, loading, error, reload } = useAsync(
    () => adminApi.getOrders({ status, page: 0, size: 50 }),
    [status]
  );
  const { run, running } = useAction();

  const handleShip = async (orderId: number) => {
    const trackingNo = window.prompt("출고 송장번호를 입력하세요:", "6234-1188-0092");
    if (!trackingNo) return;
    await run(async () => {
      await adminApi.shipOrder(orderId, trackingNo);
      await reload();
    }, "출고 처리되었습니다.");
  };

  const handleDeliver = async (orderId: number) => {
    if (!window.confirm("배송 완료로 처리할까요?")) return;
    await run(async () => {
      await adminApi.deliverOrder(orderId);
      await reload();
    }, "배송 완료 처리되었습니다.");
  };

  const orders = data?.content ?? [];

  return (
    <AdminLayout>
      <h2 className="admin-page-title">주문 · 출고 관리</h2>
      <p className="admin-page-desc">
        결제완료된 주문에 송장을 등록하여 출고 처리합니다. 배송지가 입력되지 않은 주문은 출고할 수 없습니다.
      </p>

      <div className="sales-tabs">
        {tabs.map((t) => (
          <button
            key={t.label}
            className={`sales-tab ${status === t.value ? "active" : ""}`}
            onClick={() => setStatus(t.value)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {loading ? (
        <p>불러오는 중...</p>
      ) : error ? (
        <p>{error}</p>
      ) : orders.length === 0 ? (
        <p>해당 상태의 주문이 없습니다.</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>주문번호</th>
              <th>상품명</th>
              <th>구매자</th>
              <th>결제금액</th>
              <th>상태</th>
              <th>처리</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o) => (
              <tr key={o.orderId}>
                <td>#{o.orderId}</td>
                <td className="col-strong">{o.title}</td>
                <td>{o.buyer}</td>
                <td>{o.orderPrice.toLocaleString()}원</td>
                <td>
                  <span
                    className={`admin-status ${
                      o.status === "PAID" ? "status-awaiting" : "status-progress"
                    }`}
                  >
                    {ORDER_STATUS_LABEL[o.status]}
                  </span>
                  {/* 배송지가 없으면 출고 자체가 불가능하다 (서버도 409로 막는다) */}
                  {o.status === "PAID" && !o.shippingReady && (
                    <span className="admin-status status-danger" style={{ marginLeft: 6 }}>
                      배송지 미입력
                    </span>
                  )}
                </td>
                <td>
                  {o.status === "PAID" ? (
                    <button
                      className="admin-btn"
                      disabled={running || !o.shippingReady}
                      onClick={() => void handleShip(o.orderId)}
                    >
                      출고 처리
                    </button>
                  ) : o.status === "SHIPPED" ? (
                    <button
                      className="admin-btn"
                      disabled={running}
                      onClick={() => void handleDeliver(o.orderId)}
                    >
                      배송 완료
                    </button>
                  ) : (
                    <button className="admin-btn" disabled>처리됨</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </AdminLayout>
  );
}

export default AdminOrdersPage;
