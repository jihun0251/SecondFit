import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "./LoginPage.css";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async () => {
    // 백엔드 로그인 API(JWT)는 아직 없음 → 목업으로 홈 이동
    console.log("로그인 시도:", { email, password });
    alert("로그인 성공! (목업)");
    navigate("/");
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
          />
        </div>

        <div className="login-field">
          <label>비밀번호</label>
          <input
            type="password"
            placeholder="비밀번호를 입력하세요"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <button className="login-button" onClick={handleSubmit}>
          로그인
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