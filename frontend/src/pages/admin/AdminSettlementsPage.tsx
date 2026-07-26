import { useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import { adminApi } from "../../api";
import type { SettlementStatus } from "../../api/types";
import { useAction, useAsync } from "../../hooks/useAsync";

const tabs: { label: string; value?: SettlementStatus }[] = [
  { label: "전체", value: undefined },
  { label: "정산 대기", value: "PENDING" },
  { label: "정산 완료", value: "COMPLETED" },
];

function AdminSettlementsPage() {
  const [status, setStatus] = useState<SettlementStatus | undefined>("PENDING");

  const { data, loading, error, reload } = useAsync(
    () => adminApi.getSettlements({ status, page: 0, size: 50 }),
    [status]
  );
  const { run, running } = useAction();

  const handleComplete = async (settlementId: number, account: string | null) => {
    if (!account) {
      alert("판매자가 정산 계좌를 등록하지 않았습니다. 계좌 등록 후 처리해 주세요.");
      return;
    }
    if (!window.confirm(`${account} 로 송금을 완료했나요?`)) return;
    await run(async () => {
      await adminApi.completeSettlement(settlementId);
      await reload();
    }, "정산 완료 처리되었습니다.");
  };

  const items = data?.content ?? [];

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
        정산 대기 금액 합계 <strong>{pendingTotal.toLocaleString()}원</strong>
      </div>

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
      ) : items.length === 0 ? (
        <p>해당 상태의 정산 건이 없습니다.</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>정산번호</th>
              <th>판매자</th>
              <th>정산 계좌</th>
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
                <td>{it.settlementAccount ?? "미등록"}</td>
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
                      disabled={running}
                      onClick={() => void handleComplete(it.settlementId, it.settlementAccount)}
                    >
                      정산 처리
                    </button>
                  ) : (
                    <button className="admin-btn" disabled>완료됨</button>
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

export default AdminSettlementsPage;
