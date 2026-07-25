import { useState } from "react";
import { Link } from "react-router-dom";
import "./ResetPasswordPage.css";

function ResetPasswordPage() {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);

  const handleSubmit = () => {
    if (!email) {
      alert("이메일을 입력하세요.");
      return;
    }
    // 실제로는 재설정 링크 발송 API 호출
    console.log("재설정 링크 발송:", email);
    setSent(true);
  };

  return (
    <div className="reset-container">
      <div className="reset-card">
        <h1 className="reset-title">비밀번호 재설정</h1>
        <p className="reset-subtitle">
          가입한 이메일로 재설정 링크를 보내드립니다.
        </p>

        {sent ? (
          <div className="reset-done">
            <p className="reset-done-text">
              <strong>{email}</strong> 으로<br />
              재설정 링크를 보냈습니다.
            </p>
            <Link to="/login" className="reset-back-link">
              ← 로그인으로 돌아가기
            </Link>
          </div>
        ) : (
          <>
            <div className="reset-field">
              <label>이메일</label>
              <input
                type="email"
                placeholder="you@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <button className="reset-button" onClick={handleSubmit}>
              재설정 링크 보내기
            </button>

            <div className="reset-links">
              <Link to="/login">← 로그인으로 돌아가기</Link>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default ResetPasswordPage;