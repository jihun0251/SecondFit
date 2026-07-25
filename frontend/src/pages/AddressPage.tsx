import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import StepIndicator from "../components/StepIndicator";
import "./AddressPage.css";

function AddressPage() {
  const { productId } = useParams();
  const navigate = useNavigate();

  return (
    <div className="address-page">
      <Header loggedIn userName="buyer_lee" />
      <div className="address-body">
        <StepIndicator current={2} />

        <div className="address-header">
          <h3 className="address-title">배송지 정보</h3>
          <span className="badge-paid">● 결제완료</span>
        </div>

        <div className="address-card">
          <div className="addr-field">
            <label>받는 사람</label>
            <input placeholder="이름" />
          </div>

          <div className="addr-field">
            <label>연락처</label>
            <input placeholder="010-0000-0000" />
          </div>

          <div className="addr-field">
            <label>주소</label>
            <div className="addr-zip-row">
              <input placeholder="우편번호" />
              <button className="btn-zip">주소 검색</button>
            </div>
            <input className="addr-mt" placeholder="기본 주소" />
            <input className="addr-mt" placeholder="상세 주소" />
          </div>

          <div className="addr-field">
            <label>배송 메모</label>
            <select>
              <option>부재 시 문 앞에 놓아주세요</option>
              <option>경비실에 맡겨주세요</option>
              <option>배송 전 연락주세요</option>
            </select>
          </div>
        </div>

        <div className="address-actions">
          <button className="btn-prev" onClick={() => navigate(-1)}>
            이전
          </button>
          <button
            className="btn-next"
            onClick={() => navigate("/orders/9931")}
          >
            배송지 저장 · 출고 요청
          </button>
        </div>
      </div>
    </div>
  );
}

export default AddressPage;