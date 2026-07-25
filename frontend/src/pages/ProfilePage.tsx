import { useState } from "react";
import Header from "../components/Header";
import MyPageSidebar from "../components/MyPageSidebar";
import { mockProfile } from "../mocks/data";
import "./ProfilePage.css";

function ProfilePage() {
  const [nickname, setNickname] = useState(mockProfile.nickname);
  const [phone, setPhone] = useState(mockProfile.phone);
  const [account, setAccount] = useState(mockProfile.settlementAccount);
  const [newPassword, setNewPassword] = useState("");

  const handleSave = () => {
    // PATCH /users/me (백엔드 연동 지점)
    console.log("프로필 저장:", { nickname, phone, account });
    alert("프로필이 저장되었습니다.");
  };

  const handleChangePassword = () => {
    if (!newPassword) {
      alert("변경할 비밀번호를 입력하세요.");
      return;
    }
    console.log("비밀번호 변경:", newPassword);
    alert("비밀번호가 변경되었습니다.");
    setNewPassword("");
  };

  return (
    <div className="profile-page">
      <Header loggedIn userName={mockProfile.nickname} />
      <div className="mypage-body">
        <MyPageSidebar />
        <main className="mypage-main">
          <h2 className="mypage-title">프로필 관리</h2>

          {/* 프로필 요약 카드 */}
          <div className="profile-summary">
            <div className="profile-avatar" />
            <div className="profile-summary-info">
              <span className="profile-nickname">{mockProfile.nickname}</span>
              <span className="profile-sub">
                {mockProfile.email} · 가입 {mockProfile.joinedAt}
              </span>
              <div className="profile-badges">
                <span className="profile-badge badge-green">
                  ● 판매자 ★ {mockProfile.rating}
                </span>
                <span className="profile-badge badge-gray">
                  ● 거래 {mockProfile.tradeCount}건
                </span>
              </div>
            </div>
            <button className="btn-image">이미지 변경</button>
          </div>

          {/* 수정 폼 */}
          <div className="profile-form">
            <div className="profile-field">
              <label>닉네임</label>
              <input
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
              />
            </div>

            <div className="profile-field">
              <label>휴대폰</label>
              <input value={phone} onChange={(e) => setPhone(e.target.value)} />
            </div>

            <div className="profile-field">
              <label>정산 계좌</label>
              <input
                value={account}
                onChange={(e) => setAccount(e.target.value)}
              />
            </div>

            <div className="profile-field">
              <label>비밀번호</label>
              <div className="password-row">
                <input
                  type="password"
                  placeholder="변경하려면 입력"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                />
                <button className="btn-change" onClick={handleChangePassword}>
                  변경
                </button>
              </div>
            </div>

            <div className="profile-actions">
              <button className="btn-save" onClick={handleSave}>
                저장
              </button>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

export default ProfilePage;