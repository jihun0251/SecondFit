import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import Header from "../components/Header";
import StepIndicator from "../components/StepIndicator";
import { orderApi } from "../api";
import { useAction } from "../hooks/useAsync";
import "./AddressPage.css";

const memoOptions = [
  "부재 시 문 앞에 놓아주세요",
  "경비실에 맡겨주세요",
  "배송 전 연락주세요",
];

function AddressPage() {
  const navigate = useNavigate();
  const location = useLocation();

  // CheckoutPage가 결제 직후 넘겨준 주문 번호
  const orderId = (location.state as { orderId?: number } | null)?.orderId;

  const [receiverName, setReceiverName] = useState("");
  const [receiverPhone, setReceiverPhone] = useState("");
  const [zipcode, setZipcode] = useState("");
  const [address1, setAddress1] = useState("");
  const [address2, setAddress2] = useState("");
  const [memo, setMemo] = useState(memoOptions[0]);

  const { run, running } = useAction();

  const handleSave = async () => {
    if (!orderId) {
      alert("주문 정보를 찾을 수 없습니다. 결제를 다시 진행해 주세요.");
      return;
    }
    if (!receiverName || !receiverPhone || !zipcode || !address1) {
      alert("받는 사람, 연락처, 우편번호, 기본 주소는 필수입니다.");
      return;
    }

    await run(async () => {
      await orderApi.updateShipping(orderId, {
        receiverName,
        receiverPhone,
        zipcode,
        address1,
        address2: address2 || undefined,
        memo,
      });
      navigate(`/orders/${orderId}`, { replace: true });
    });
  };

  // 결제를 건너뛰고 직접 들어온 경우
  if (!orderId) {
    return (
      <div className="address-page">
        <Header />
        <div className="address-body">
          <p>주문 정보가 없습니다. 상품 페이지에서 결제를 먼저 진행해 주세요.</p>
          <button className="btn-prev" onClick={() => navigate("/products")}>
            상품 목록으로
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="address-page">
      <Header />
      <div className="address-body">
        <StepIndicator current={2} />

        <div className="address-header">
          <h3 className="address-title">배송지 정보</h3>
          <span className="badge-paid">● 결제완료</span>
        </div>

        <div className="address-card">
          <div className="addr-field">
            <label>받는 사람</label>
            <input
              placeholder="이름"
              value={receiverName}
              onChange={(e) => setReceiverName(e.target.value)}
            />
          </div>

          <div className="addr-field">
            <label>연락처</label>
            <input
              placeholder="010-0000-0000"
              value={receiverPhone}
              onChange={(e) => setReceiverPhone(e.target.value)}
            />
          </div>

          <div className="addr-field">
            <label>주소</label>
            <div className="addr-zip-row">
              <input
                placeholder="우편번호"
                value={zipcode}
                onChange={(e) => setZipcode(e.target.value)}
              />
              {/* 우편번호 검색 API(다음 주소 등)는 아직 미연동 — 직접 입력 */}
              <button className="btn-zip" disabled>주소 검색</button>
            </div>
            <input
              className="addr-mt"
              placeholder="기본 주소"
              value={address1}
              onChange={(e) => setAddress1(e.target.value)}
            />
            <input
              className="addr-mt"
              placeholder="상세 주소"
              value={address2}
              onChange={(e) => setAddress2(e.target.value)}
            />
          </div>

          <div className="addr-field">
            <label>배송 메모</label>
            <select value={memo} onChange={(e) => setMemo(e.target.value)}>
              {memoOptions.map((option) => (
                <option key={option}>{option}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="address-actions">
          <button className="btn-prev" onClick={() => navigate(-1)}>이전</button>
          <button className="btn-next" onClick={handleSave} disabled={running}>
            {running ? "저장 중..." : "배송지 저장 · 출고 요청"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default AddressPage;
