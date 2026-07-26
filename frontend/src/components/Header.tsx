import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import "./Header.css";

function Header() {
  const { user, isLoggedIn, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState("");

  const handleSearch = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key !== "Enter") return;
    navigate(`/products?keyword=${encodeURIComponent(keyword.trim())}`);
  };

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <header className="header">
      <div className="header-inner">
        <Link to="/" className="header-logo">
          Second<span className="header-logo-accent">Fit</span>
        </Link>
        <nav className="header-nav">
          <Link to="/">홈</Link>
          <Link to="/products">상품</Link>
          <Link to="/products/new">판매하기</Link>
        </nav>
        <div className="header-right">
          <div className="header-search">
            <span className="header-search-icon">🔍</span>
            <input
              placeholder="브랜드, 상품, 카테고리 검색"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={handleSearch}
            />
          </div>
          <Link to="/my/likes" className="header-link">♡ 찜</Link>
          {/* 관리자 메뉴는 ADMIN 계정에게만 보인다 */}
          {isAdmin && (
            <Link to="/admin/inbounds" className="header-link admin-link">관리자</Link>
          )}
          {isLoggedIn ? (
            <>
              <Link to="/my/orders" className="header-link">마이</Link>
              <span className="header-user">{user?.nickname}</span>
              <button className="header-login-btn" onClick={handleLogout}>
                로그아웃
              </button>
            </>
          ) : (
            <Link to="/login" className="header-login-btn">로그인</Link>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;
