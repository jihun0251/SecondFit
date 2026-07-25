import { useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import { mockInbounds } from "../../mocks/data";

function AdminInboundsPage() {
  const [items, setItems] = useState(mockInbounds);

  const handleConfirm = (inboundId: number) => {
    // POST /admin/inbounds/{inboundId}/confirm
    setItems((prev) =>
      prev.map((it) =>
        it.inboundId === inboundId ? { ...it, status: "CONFIRMED" } : it
      )
    );
  };

  return (
    <AdminLayout>
      <h2 className="admin-page-title">입고 관리</h2>
      <p className="admin-page-desc">
        판매자가 발송한 상품을 입고 확인하면 즉시 판매중으로 전환됩니다.
      </p>

      <table className="admin-table">
        <thead>
          <tr>
            <th>입고번호</th>
            <th>상품명</th>
            <th>판매자</th>
            <th>가격</th>
            <th>상태</th>
            <th>처리</th>
          </tr>
        </thead>
        <tbody>
          {items.map((it) => (
            <tr key={it.inboundId}>
              <td>#{it.inboundId}</td>
              <td className="col-strong">{it.title}</td>
              <td>{it.seller}</td>
              <td>{it.price.toLocaleString()}원</td>
              <td>
                {it.status === "AWAITING" ? (
                  <span className="admin-status status-awaiting">입고 대기</span>
                ) : (
                  <span className="admin-status status-done">입고 완료 · 판매중</span>
                )}
              </td>
              <td>
                {it.status === "AWAITING" ? (
                  <button
                    className="admin-btn"
                    onClick={() => handleConfirm(it.inboundId)}
                  >
                    입고 확인
                  </button>
                ) : (
                  <button className="admin-btn" disabled>
                    완료됨
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

export default AdminInboundsPage;