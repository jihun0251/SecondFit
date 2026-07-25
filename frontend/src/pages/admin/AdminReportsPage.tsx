import { useState } from "react";
import AdminLayout from "../../components/AdminLayout";
import { mockReports } from "../../mocks/data";

function AdminReportsPage() {
  const [reports, setReports] = useState(mockReports);

  const handleResolve = (reportId: number, action: "RESOLVED" | "REJECTED") => {
    // PATCH /admin/reports/{reportId}
    setReports((prev) =>
      prev.map((r) => (r.reportId === reportId ? { ...r, status: action } : r))
    );
  };

  const statusView = (status: string) => {
    if (status === "RECEIVED")
      return <span className="admin-status status-danger">미처리</span>;
    if (status === "RESOLVED")
      return <span className="admin-status status-done">처리완료 · 정지</span>;
    return <span className="admin-status status-gray">반려</span>;
  };

  return (
    <AdminLayout>
      <h2 className="admin-page-title">신고 처리</h2>
      <p className="admin-page-desc">
        접수된 신고를 검토하여 상품을 정지하거나 반려합니다.
      </p>

      <table className="admin-table">
        <thead>
          <tr>
            <th>신고번호</th>
            <th>상품ID</th>
            <th>판매자</th>
            <th>사유</th>
            <th>신고수</th>
            <th>상태</th>
            <th>처리</th>
          </tr>
        </thead>
        <tbody>
          {reports.map((r) => (
            <tr key={r.reportId}>
              <td>#{r.reportId}</td>
              <td>{r.productId}</td>
              <td className="col-strong">{r.seller}</td>
              <td>{r.reasonLabel}</td>
              <td>{r.count}건</td>
              <td>{statusView(r.status)}</td>
              <td>
                {r.status === "RECEIVED" ? (
                  <div className="admin-actions">
                    <button
                      className="admin-btn danger"
                      onClick={() => handleResolve(r.reportId, "RESOLVED")}
                    >
                      정지
                    </button>
                    <button
                      className="admin-btn ghost"
                      onClick={() => handleResolve(r.reportId, "REJECTED")}
                    >
                      반려
                    </button>
                  </div>
                ) : (
                  <button className="admin-btn" disabled>
                    처리됨
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

export default AdminReportsPage;