import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import MyPageSidebar from "../components/MyPageSidebar";
import { mockOrders } from "../mocks/data";
import "./OrdersPage.css";

const tabs = ["전체 6", "결제완료 1", "배송중 2", "배송완료 3"];

function OrdersPage() {
  const [tab, setTab] = useState(tabs[0]);
  const navigate = useNavigate();

  const statusClass = (status: string) => {
    if (status === "SHIPPING") return "st-shipping";
    if (status === "DELIVERED") return "st-delivered";
    return "st-confirmed";
  };

  return (
    <div className="orders-page">
      <Header loggedIn userName="buyer_lee" />
      <div className="mypage-body">
        <MyPageSidebar />
        <main className="mypage-main">
          <h2 className="mypage-title">구매 내역</h2>

          <div className="order-tabs">
            {tabs.map((t) => (
              <button
                key={t}
                className={`order-tab ${tab === t ? "active" : ""}`}
                onClick={() => setTab(t)}
              >
                {t}
              </button>
            ))}
          </div>

          <div className="order-list">
            {mockOrders.map((o) => (
              <div className="order-row" key={o.id}>
                <div className="order-row-thumb" />
                <div className="order-row-info">
                  <span className="order-row-name">{o.name}</span>
                  <span className="order-row-meta">
                    {o.price.toLocaleString()}원 · {o.date} 결제
                  </span>
                </div>
                <span className={`order-status ${statusClass(o.status)}`}>
                  ● {o.statusLabel}
                </span>
                <button
                  className={`order-action ${
                    o.action === "거래 확정" ? "primary" : ""
                  }`}
                  onClick={() => {
                    if (o.action === "배송 조회") navigate(`/orders/${o.id}`);
                  }}
                >
                  {o.action}
                </button>
              </div>
            ))}
          </div>
        </main>
      </div>
    </div>
  );
}

export default OrdersPage;