import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import Header from "../components/Header";
import MyPageSidebar from "../components/MyPageSidebar";
import { productApi, settlementApi } from "../api";
import { resolveImageUrl } from "../api/client";
import { PRODUCT_STATUS_LABEL } from "../api/types";
import type { ProductStatus } from "../api/types";
import { useAsync } from "../hooks/useAsync";
import "./SalesPage.css";

const tabs: { label: string; value?: ProductStatus }[] = [
  { label: "전체", value: undefined },
  { label: "입고 대기", value: "PENDING_INBOUND" },
  { label: "판매중", value: "ON_SALE" },
  { label: "결제완료", value: "PAID" },
  { label: "정산완료", value: "SETTLED" },
];

function SalesPage() {
  const [status, setStatus] = useState<ProductStatus | undefined>();
  const navigate = useNavigate();

  const { data, loading, error } = useAsync(
    () => productApi.getMine({ status, page: 0, size: 50 }),
    [status]
  );

  // 정산 요약도 함께 보여준다 (판매자가 가장 궁금해하는 숫자)
  const { data: settlements } = useAsync(() => settlementApi.getMine({ page: 0, size: 100 }), []);

  const statusClass = (s: ProductStatus) => {
    if (s === "ON_SALE") return "st-onsale";
    if (s === "PENDING_INBOUND") return "st-pending";
    if (s === "SETTLED") return "st-settled";
    return "st-progress";
  };

  const products = data?.content ?? [];

  const pendingAmount = (settlements?.content ?? [])
    .filter((s) => s.status === "PENDING")
    .reduce((sum, s) => sum + s.netAmount, 0);

  const completedAmount = (settlements?.content ?? [])
    .filter((s) => s.status === "COMPLETED")
    .reduce((sum, s) => sum + s.netAmount, 0);

  return (
    <div className="sales-page">
      <Header />
      <div className="mypage-body">
        <MyPageSidebar />
        <main className="mypage-main">
          <div className="sales-header">
            <h2 className="mypage-title">판매 내역</h2>
            <Link to="/products/new" className="btn-register">+ 상품 등록</Link>
          </div>

          {/* 정산 요약 */}
          <div className="settlement-summary">
            <div className="settlement-box">
              <span className="settlement-label">정산 대기</span>
              <span className="settlement-value">{pendingAmount.toLocaleString()}원</span>
            </div>
            <div className="settlement-box">
              <span className="settlement-label">정산 완료</span>
              <span className="settlement-value">{completedAmount.toLocaleString()}원</span>
            </div>
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

          <div className="sales-list">
            {loading ? (
              <div className="sales-empty">불러오는 중...</div>
            ) : error ? (
              <div className="sales-empty">{error}</div>
            ) : products.length === 0 ? (
              <div className="sales-empty">해당 상태의 상품이 없습니다.</div>
            ) : (
              products.map((s) => (
                <div className="sales-row" key={s.productId}>
                  <div
                    className="sales-thumb"
                    style={
                      s.thumbnail
                        ? {
                            backgroundImage: `url(${resolveImageUrl(s.thumbnail)})`,
                            backgroundSize: "cover",
                            backgroundPosition: "center",
                          }
                        : undefined
                    }
                  />
                  <div className="sales-info">
                    <span className="sales-name">{s.title}</span>
                    <span className="sales-meta">
                      {s.price.toLocaleString()}원 ·{" "}
                      {new Date(s.createdAt).toLocaleDateString("ko-KR")} 등록
                    </span>
                  </div>
                  <span className={`sales-status ${statusClass(s.status)}`}>
                    ● {PRODUCT_STATUS_LABEL[s.status]}
                  </span>
                  {s.status === "PENDING_INBOUND" ? (
                    <button
                      className="sales-action"
                      onClick={() => navigate(`/products/${s.productId}/edit`)}
                    >
                      수정
                    </button>
                  ) : (
                    <Link to={`/products/${s.productId}`} className="sales-action">
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
