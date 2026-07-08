import { useState } from "react";
import "./SignupPage.css";

function SignupPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [phone, setPhone] = useState("");

  const handleSubmit = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/v1/users/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: email,
          password: password,
          nickname: nickname,
          phone: phone,
        }),
      });

      const data = await response.json();

      if (response.ok) {
        console.log("회원가입 성공:", data);
        alert("회원가입 성공!");
      } else {
        console.log("회원가입 실패:", data);
        alert("실패: " + data.error.message);
      }
    } catch (error) {
      console.error("요청 중 오류:", error);
      alert("서버에 연결할 수 없습니다.");
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

        <button className="signup-button" onClick={handleSubmit}>
          가입하기
        </button>
      </div>
    </div>
  );
}

export default SignupPage;