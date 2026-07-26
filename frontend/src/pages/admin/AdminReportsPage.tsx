import { useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import { adminApi } from "../../api";
import { REPORT_REASON_LABEL } from "../../api/types";
import type { ReportStatus } from "../../api/types";
import { useAction, useAsync } from "../../hooks/useAsync";

const tabs: { label: string; value?: ReportStatus }[] = [
  { label: "전체", value: undefined },
  { label: "미처리", value: "RECEIVED" },
  { label: "처리완료", value: "RESOLVED" },
  { label: "반려", value: "REJECTED" },
];

function AdminReportsPage() {
  const [status, setStatus] = useState<ReportStatus | undefined>("RECEIVED");

  const { data, loading, error, reload } = useAsync(
    () => adminApi.getReports({ status, page: 0, size: 50 }),
    [status]
  );
  const { run, running } = useAction();

  /**
   * 정지 = RESOLVED + SUSPEND_PRODUCT → 상품이 목록에서 사라진다.
   * 반려 = REJECTED → 상품은 그대로 유지된다.
   */
  const handleHandle = async (reportId: number, next: ReportStatus) => {
    const suspend = next === "RESOLVED";
    if (
      !window.confirm(
        suspend
          ? "신고를 인정하고 해당 상품을 노출 중지할까요?"
          : "신고를 반려할까요? 상품은 그대로 유지됩니다."
      )
    )
      return;

    await run(async () => {
      await adminApi.handleReport(reportId, next, suspend ? "SUSPEND_PRODUCT" : undefined);
      await reload();
    }, suspend ? "상품이 노출 중지되었습니다." : "신고를 반려했습니다.");
  };

  const statusView = (s: ReportStatus) => {
    if (s === "RECEIVED") return <span className="admin-status status-danger">미처리</span>;
    if (s === "REVIEWING") return <span className="admin-status status-awaiting">검토중</span>;
    if (s === "RESOLVED") return <span className="admin-status status-done">처리완료 · 정지</span>;
    return <span className="admin-status status-gray">반려</span>;
  };

  const reports = data?.content ?? [];

  return (
    <AdminLayout>
      <h2 className="admin-page-title">신고 처리</h2>
      <p className="admin-page-desc">
        접수된 신고를 검토하여 상품을 정지하거나 반려합니다. 같은 상품에 신고가 여러 건 쌓이면 우선 확인하세요.
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
      ) : reports.length === 0 ? (
        <p>해당 상태의 신고가 없습니다.</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>신고번호</th>
              <th>상품ID</th>
              <th>판매자</th>
              <th>사유</th>
              <th>상세</th>
              <th>신고수</th>
              <th>상태</th>
              <th>처리</th>
            </tr>
          </thead>
          <tbody>
            {reports.map((r) => (
              <tr key={r.reportId}>
                <td>#{r.reportId}</td>
                <td>{r.productId ?? "삭제됨"}</td>
                <td className="col-strong">{r.seller ?? "-"}</td>
                <td>{REPORT_REASON_LABEL[r.reason]}</td>
                <td>{r.detail ?? "-"}</td>
                <td>{r.count}건</td>
                <td>{statusView(r.status)}</td>
                <td>
                  {r.status === "RECEIVED" || r.status === "REVIEWING" ? (
                    <div className="admin-actions">
                      <button
                        className="admin-btn danger"
                        disabled={running}
                        onClick={() => void handleHandle(r.reportId, "RESOLVED")}
                      >
                        정지
                      </button>
                      <button
                        className="admin-btn ghost"
                        disabled={running}
                        onClick={() => void handleHandle(r.reportId, "REJECTED")}
                      >
                        반려
                      </button>
                    </div>
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

export default AdminReportsPage;
