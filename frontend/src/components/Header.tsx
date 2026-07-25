import { Link } from "react-router-dom";
import "./Header.css";

interface HeaderProps {
  loggedIn?: boolean;
  userName?: string;
}

function Header({ loggedIn = false, userName = "buyer_lee" }: HeaderProps) {
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
            <input placeholder="브랜드, 상품, 카테고리 검색" />
          </div>
          <Link to="/my/likes" className="header-link">♡ 찜</Link>
          <Link to="/admin/inbounds" className="header-link admin-link">관리자</Link>
          {loggedIn ? (
            <span className="header-user">{userName}</span>
          ) : (
            <>
              <Link to="/my/orders" className="header-link">마이</Link>
              <Link to="/login" className="header-login-btn">로그인</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;