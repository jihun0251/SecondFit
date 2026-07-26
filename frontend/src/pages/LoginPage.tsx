import { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { ApiError } from "../api/client";
import { useAuth } from "../contexts/AuthContext";
import "./LoginPage.css";

interface LocationState {
  from?: string;
}

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  // ProtectedRoute가 넘겨준 "원래 가려던 경로"
  const from = (location.state as LocationState | null)?.from;

  const handleSubmit = async () => {
    if (!email || !password) {
      setError("이메일과 비밀번호를 입력해 주세요.");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const user = await login(email, password);

      // 관리자는 관리자 화면으로, 일반 사용자는 원래 가려던 곳(없으면 홈)으로
      if (from) navigate(from, { replace: true });
      else if (user.role === "ADMIN") navigate("/admin/inbounds", { replace: true });
      else navigate("/", { replace: true });
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "로그인에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1 className="login-title">SecondFit</h1>
        <p className="login-subtitle">중고 의류, 믿고 사고 파는 위탁 마켓</p>

        <div className="login-field">
          <label>이메일</label>
          <input
            type="email"
            placeholder="이메일을 입력하세요"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
          />
        </div>

        <div className="login-field">
          <label>비밀번호</label>
          <input
            type="password"
            placeholder="비밀번호를 입력하세요"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
          />
        </div>

        {error && <p className="login-error">{error}</p>}

        <button className="login-button" onClick={handleSubmit} disabled={loading}>
          {loading ? "로그인 중..." : "로그인"}
        </button>

        <div className="login-links">
          <Link to="/signup">회원가입</Link>
          <span className="divider">·</span>
          <Link to="/reset">비밀번호 찾기</Link>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
