import { Navigate, useLocation } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuth } from "../contexts/AuthContext";

/**
 * 로그인(또는 관리자 권한)이 필요한 화면을 감싸는 가드.
 *
 * 백엔드가 401/403으로 막아주긴 하지만, 그건 "요청이 거절되는" 것이고
 * 화면 자체를 안 보여주는 건 프론트가 해야 한다.
 */
interface Props {
  children: ReactNode;
  /** true면 ADMIN만 통과 */
  adminOnly?: boolean;
}

function ProtectedRoute({ children, adminOnly = false }: Props) {
  const { isLoggedIn, isAdmin } = useAuth();
  const location = useLocation();

  if (!isLoggedIn) {
    // 로그인 후 원래 가려던 곳으로 돌려보내기 위해 현재 경로를 넘긴다
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  if (adminOnly && !isAdmin) {
    return (
      <div style={{ padding: "80px 24px", textAlign: "center" }}>
        <h2>접근 권한이 없습니다</h2>
        <p style={{ color: "#666", marginTop: 8 }}>관리자 계정으로 로그인해 주세요.</p>
      </div>
    );
  }

  return <>{children}</>;
}

export default ProtectedRoute;
