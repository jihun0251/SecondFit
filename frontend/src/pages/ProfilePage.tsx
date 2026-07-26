import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import MyPageSidebar from "../components/MyPageSidebar";
import { userApi } from "../api";
import { useAction, useAsync } from "../hooks/useAsync";
import { useAuth } from "../contexts/AuthContext";
import "./ProfilePage.css";

function ProfilePage() {
  const { data: profile, loading, error, reload } = useAsync(() => userApi.getMe(), []);
  const { run, running } = useAction();
  const { logout } = useAuth();
  const navigate = useNavigate();

  const [nickname, setNickname] = useState("");
  const [phone, setPhone] = useState("");
  const [account, setAccount] = useState("");

  useEffect(() => {
    if (!profile) return;
    setNickname(profile.nickname);
    // 서버는 전화번호를 마스킹해서 준다(010-****-5678).
    // 그대로 저장하면 마스킹된 값이 진짜 값이 되어버리므로 비워둔다.
    setPhone("");
    setAccount(profile.settlementAccount ?? "");
  }, [profile]);

  const handleSave = async () => {
    await run(async () => {
      await userApi.updateMe({
        nickname: nickname || undefined,
        phone: phone || undefined, // 입력했을 때만 전송
        settlementAccount: account || undefined,
      });
      await reload();
      alert("프로필이 저장되었습니다.");
    });
  };

  const handleWithdraw = async () => {
    if (!window.confirm("정말 탈퇴하시겠습니까? 되돌릴 수 없습니다.")) return;
    await run(async () => {
      await userApi.withdraw();
      logout();
      alert("탈퇴 처리되었습니다.");
      navigate("/");
    });
  };

  if (loading) {
    return (
      <div className="profile-page">
        <Header />
        <div className="mypage-body">
          <MyPageSidebar />
          <main className="mypage-main">불러오는 중...</main>
        </div>
      </div>
    );
  }

  if (error || !profile) {
    return (
      <div className="profile-page">
        <Header />
        <div className="mypage-body">
          <MyPageSidebar />
          <main className="mypage-main">{error ?? "프로필을 불러올 수 없습니다."}</main>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-page">
      <Header />
      <div className="mypage-body">
        <MyPageSidebar />
        <main className="mypage-main">
          <h2 className="mypage-title">프로필 관리</h2>

          {/* 프로필 요약 카드 */}
          <div className="profile-summary">
            <div className="profile-avatar" />
            <div className="profile-summary-info">
              <span className="profile-nickname">{profile.nickname}</span>
              <span className="profile-sub">{profile.email}</span>
              <div className="profile-badges">
                <span className="profile-badge badge-green">
                  ● 판매자 ★ {profile.rating}
                </span>
                <span className="profile-badge badge-gray">
                  ● 거래 {profile.tradeCount}건
                </span>
              </div>
            </div>
          </div>

          {/* 수정 폼 */}
          <div className="profile-form">
            <div className="profile-field">
              <label>닉네임</label>
              <input value={nickname} onChange={(e) => setNickname(e.target.value)} />
            </div>

            <div className="profile-field">
              <label>휴대폰</label>
              <input
                value={phone}
                placeholder={profile.phone ?? "010-0000-0000"}
                onChange={(e) => setPhone(e.target.value)}
              />
              <span className="profile-hint">
                현재 등록된 번호: {profile.phone ?? "없음"} (변경할 때만 입력하세요)
              </span>
            </div>

            <div className="profile-field">
              <label>정산 계좌</label>
              <input
                value={account}
                placeholder="예: 카카오뱅크 3333-01-1234567"
                onChange={(e) => setAccount(e.target.value)}
              />
            </div>

            <div className="profile-actions">
              <button className="btn-save" onClick={handleSave} disabled={running}>
                {running ? "저장 중..." : "저장"}
              </button>
              <button className="btn-change" onClick={handleWithdraw} disabled={running}>
                회원 탈퇴
              </button>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

export default ProfilePage;
