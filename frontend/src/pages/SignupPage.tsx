import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { userApi } from "../api";
import { ApiError } from "../api/client";
import "./SignupPage.css";

function SignupPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [phone, setPhone] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async () => {
    setError(null);
    setLoading(true);
    try {
      await userApi.signup({ email, password, nickname, phone: phone || undefined });
      alert("회원가입이 완료되었습니다. 로그인해 주세요.");
      navigate("/login", { replace: true });
    } catch (e) {
      // 백엔드가 이메일/닉네임 중복, 비밀번호 규칙 위반 등을 메시지로 내려준다
      setError(e instanceof ApiError ? e.message : "회원가입에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="signup-container">
      <div className="signup-card">
        <h1 className="signup-title">회원가입</h1>
        <p className="signup-subtitle">중고 의류, 믿고 사고 파는 위탁 마켓</p>

        <div className="signup-field">
          <label>이메일</label>
          <input
            type="email"
            placeholder="이메일을 입력하세요"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div className="signup-field">
          <label>비밀번호</label>
          <input
            type="password"
            placeholder="영문+숫자 8자 이상"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <div className="signup-field">
          <label>닉네임</label>
          <input
            type="text"
            placeholder="닉네임을 입력하세요"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
          />
        </div>

        <div className="signup-field">
          <label>전화번호 (선택)</label>
          <input
            type="text"
            placeholder="010-1234-5678"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
          />
        </div>

        {error && <p className="signup-error">{error}</p>}

        <button className="signup-button" onClick={handleSubmit} disabled={loading}>
          {loading ? "가입 중..." : "가입하기"}
        </button>

        <div className="signup-links">
          <span>이미 계정이 있으신가요? </span>
          <Link to="/login">로그인</Link>
        </div>
      </div>
    </div>
  );
}

export default SignupPage;
