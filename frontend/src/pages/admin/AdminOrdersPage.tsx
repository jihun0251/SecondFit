import { useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import { mockAdminOrders } from "../../mocks/data";

function AdminOrdersPage() {
  const [orders, setOrders] = useState(mockAdminOrders);

  const handleShip = (orderId: number) => {
    // POST /admin/orders/{orderId}/ship (송장 등록)
    const trackingNo = prompt("출고 송장번호를 입력하세요:", "6234-1188-0092");
    if (!trackingNo) return;
    setOrders((prev) =>
      prev.map((o) =>
        o.orderId === orderId
          ? { ...o, status: "SHIPPED", statusLabel: "출고완료" }
          : o
      )
    );
  };

  return (
    <AdminLayout>
      <h2 className="admin-page-title">주문 · 출고 관리</h2>
      <p className="admin-page-desc">
        결제완료된 주문에 송장을 등록하여 출고 처리합니다.
      </p>

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
                  {o.statusLabel}
                </span>
              </td>
              <td>
                {o.status === "PAID" ? (
                  <button
                    className="admin-btn"
                    onClick={() => handleShip(o.orderId)}
                  >
                    출고 처리
                  </button>
                ) : (
                  <button className="admin-btn" disabled>
                    출고됨
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </AdminLayout>
  );
}

export default AdminOrdersPage;