import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import Header from "../components/Header";
import MyPageSidebar from "../components/MyPageSidebar";
import { mockSales, mockProfile } from "../mocks/data";
import "./SalesPage.css";

const tabs = [
  { label: "전체", value: "ALL" },
  { label: "입고 대기", value: "PENDING_INBOUND" },
  { label: "판매중", value: "ON_SALE" },
  { label: "거래중", value: "PAID" },
  { label: "정산완료", value: "SETTLED" },
];

function SalesPage() {
  const [tab, setTab] = useState("ALL");
  const navigate = useNavigate();

  // "거래중"은 PAID/SHIPPED/DELIVERED 묶음
  const filtered = mockSales.filter((s) => {
    if (tab === "ALL") return true;
    if (tab === "PAID") return ["PAID", "SHIPPED", "DELIVERED"].includes(s.status);
    return s.status === tab;
  });

  const statusClass = (status: string) => {
    if (status === "ON_SALE") return "st-onsale";
    if (status === "PENDING_INBOUND") return "st-pending";
    if (status === "SETTLED") return "st-settled";
    return "st-progress";
  };

  return (
    <div className="sales-page">
      <Header loggedIn userName={mockProfile.nickname} />
      <div className="mypage-body">
        <MyPageSidebar />
        <main className="mypage-main">
          <div className="sales-header">
            <h2 className="mypage-title">판매 내역</h2>
            <Link to="/products/new" className="btn-register">
              + 상품 등록
            </Link>
          </div>

          <div className="sales-tabs">
            {tabs.map((t) => (
              <button
                key={t.value}
                className={`sales-tab ${tab === t.value ? "active" : ""}`}
                onClick={() => setTab(t.value)}
              >
                {t.label}
              </button>
            ))}
          </div>

          <div className="sales-list">
            {filtered.length === 0 ? (
              <div className="sales-empty">해당 상태의 상품이 없습니다.</div>
            ) : (
              filtered.map((s) => (
                <div className="sales-row" key={s.id}>
                  <div className="sales-thumb" />
                  <div className="sales-info">
                    <span className="sales-name">{s.title}</span>
                    <span className="sales-meta">
                      {s.price.toLocaleString()}원 · {s.date} 등록
                    </span>
                  </div>
                  <span className={`sales-status ${statusClass(s.status)}`}>
                    ● {s.statusLabel}
                  </span>
                  {s.status === "PENDING_INBOUND" ? (
                    <button
                      className="sales-action"
                      onClick={() => navigate(`/products/${s.id}/edit`)}
                    >
                      수정
                    </button>
                  ) : (
                    <Link to={`/products/${s.id}`} className="sales-action">
                      상세
                    </Link>
                  )}
                </div>
              ))
            )}
          </div>
        </main>
      </div>
    </div>
  );
}

export default SalesPage;