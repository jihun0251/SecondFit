import { useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import { mockSettlements } from "../../mocks/data";

function AdminSettlementsPage() {
  const [items, setItems] = useState(mockSettlements);

  const handleComplete = (settlementId: number) => {
    // POST /admin/settlements/{settlementId}/complete
    setItems((prev) =>
      prev.map((it) =>
        it.settlementId === settlementId ? { ...it, status: "COMPLETED" } : it
      )
    );
  };

  const pendingTotal = items
    .filter((it) => it.status === "PENDING")
    .reduce((sum, it) => sum + it.netAmount, 0);

  return (
    <AdminLayout>
      <h2 className="admin-page-title">정산 관리</h2>
      <p className="admin-page-desc">
        거래 확정된 건에 대해 수수료(10%)를 차감하고 판매자에게 정산합니다.
      </p>

      <div className="settlement-summary">
        정산 대기 금액 합계{" "}
        <strong>{pendingTotal.toLocaleString()}원</strong>
      </div>

      <table className="admin-table">
        <thead>
          <tr>
            <th>정산번호</th>
            <th>판매자</th>
            <th>판매금액</th>
            <th>수수료</th>
            <th>정산액</th>
            <th>상태</th>
            <th>처리</th>
          </tr>
        </thead>
        <tbody>
          {items.map((it) => (
            <tr key={it.settlementId}>
              <td>#{it.settlementId}</td>
              <td className="col-strong">{it.seller}</td>
              <td>{it.grossAmount.toLocaleString()}원</td>
              <td className="fee">-{it.feeAmount.toLocaleString()}원</td>
              <td className="col-strong">{it.netAmount.toLocaleString()}원</td>
              <td>
                {it.status === "PENDING" ? (
                  <span className="admin-status status-awaiting">정산 대기</span>
                ) : (
                  <span className="admin-status status-done">정산 완료</span>
                )}
              </td>
              <td>
                {it.status === "PENDING" ? (
                  <button
                    className="admin-btn"
                    onClick={() => handleComplete(it.settlementId)}
                  >
                    정산 처리
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

export default AdminSettlementsPage;