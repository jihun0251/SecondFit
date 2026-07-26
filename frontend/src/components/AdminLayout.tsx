import { Link, useLocation, useNavigate } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuth } from "../contexts/AuthContext";
import "./AdminLayout.css";

const adminMenu = [
  { label: "입고 관리", path: "/admin/inbounds" },
  { label: "주문·출고 관리", path: "/admin/orders" },
  { label: "정산 관리", path: "/admin/settlements" },
  { label: "신고 처리", path: "/admin/reports" },
];

function AdminLayout({ children }: { children: ReactNode }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <div className="admin-layout">
      {/* 좌측 관리자 사이드바 */}
      <aside className="admin-sidebar">
        <Link to="/" className="admin-logo">
          Second<span className="admin-logo-accent">Fit</span>
          <span className="admin-badge">ADMIN</span>
        </Link>
        <nav className="admin-nav">
          {adminMenu.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`admin-nav-item ${location.pathname === item.path ? "active" : ""}`}
            >
              {item.label}
            </Link>
          ))}
        </nav>
      </aside>

      {/* 우측 콘텐츠 */}
      <div className="admin-content">
        <header className="admin-topbar">
          <span className="admin-topbar-title">본사 관리자</span>
          <span className="admin-topbar-user">
            {user?.nickname ?? "-"}
            <button className="admin-btn ghost" style={{ marginLeft: 12 }} onClick={handleLogout}>
              로그아웃
            </button>
          </span>
        </header>
        <main className="admin-main">{children}</main>
      </div>
    </div>
  );
}

export default AdminLayout;
