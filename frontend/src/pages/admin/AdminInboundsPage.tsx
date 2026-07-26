import { useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import { adminApi } from "../../api";
import type { InboundStatus } from "../../api/types";
import { useAction, useAsync } from "../../hooks/useAsync";

const tabs: { label: string; value?: InboundStatus }[] = [
  { label: "전체", value: undefined },
  { label: "입고 대기", value: "AWAITING" },
  { label: "입고 완료", value: "CONFIRMED" },
];

function AdminInboundsPage() {
  const [status, setStatus] = useState<InboundStatus | undefined>("AWAITING");

  const { data, loading, error, reload } = useAsync(
    () => adminApi.getInbounds({ status, page: 0, size: 50 }),
    [status]
  );
  const { run, running } = useAction();

  const handleConfirm = async (inboundId: number) => {
    if (!window.confirm("실물 입고를 확인했나요?\n확인하면 상품이 즉시 판매중으로 전환됩니다.")) return;
    await run(async () => {
      await adminApi.confirmInbound(inboundId);
      await reload();
    }, "입고 확인 완료. 상품이 판매중으로 전환되었습니다.");
  };

  const items = data?.content ?? [];

  return (
    <AdminLayout>
      <h2 className="admin-page-title">입고 관리</h2>
      <p className="admin-page-desc">
        판매자가 발송한 상품을 입고 확인하면 즉시 판매중으로 전환됩니다.
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
      ) : items.length === 0 ? (
        <p>해당 상태의 입고 건이 없습니다.</p>
      ) : (
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
                      disabled={running}
                      onClick={() => void handleConfirm(it.inboundId)}
                    >
                      입고 확인
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

export default AdminInboundsPage;
