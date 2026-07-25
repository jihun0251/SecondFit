import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import { mockProducts, mockProfile } from "../mocks/data";
import "./ProductCreatePage.css";

const categories = ["아우터", "상의", "하의", "신발", "가방", "악세서리"];
const sizes = ["S", "M", "L", "XL"];
const grades = [
  { label: "S (새상품급)", value: "NEW" },
  { label: "A (사용감 적음)", value: "LIKE_NEW" },
  { label: "B (사용감 있음)", value: "GOOD" },
];

function ProductEditPage() {
  const { productId } = useParams();
  const navigate = useNavigate();

  // 기존 상품 정보 불러오기 (없으면 첫 상품으로 대체)
  const original =
    mockProducts.find((p) => p.id === Number(productId)) || mockProducts[0];

  const [title, setTitle] = useState(original.name);
  const [category, setCategory] = useState(original.category);
  const [size, setSize] = useState(original.size);
  const [color, setColor] = useState(original.color);
  const [grade, setGrade] = useState("LIKE_NEW");
  const [price, setPrice] = useState(String(original.price));
  const [description, setDescription] = useState(original.description);

  const handleSubmit = () => {
    // PATCH /products/{productId}
    console.log("상품 수정:", { productId, title, category, size, color, price });
    alert("상품 정보가 수정되었습니다.");
    navigate("/my/sales");
  };

  return (
    <div className="create-page">
      <Header loggedIn userName={mockProfile.nickname} />
      <div className="create-body">
        <h2 className="create-title">상품 수정</h2>
        <p className="create-desc">
          입고 확인 전 상품만 수정할 수 있습니다.
        </p>

        <div className="create-content">
          {/* 좌측 이미지 (수정 화면은 AI 패널 없음) */}
          <div className="create-left">
            <h4 className="create-section-title">상품 이미지</h4>
            <div className="image-grid">
              {[1, 2, 3].map((n) => (
                <div className="image-box filled" key={n}>
                  이미지 {n}
                  {n === 1 && <span className="thumb-badge">대표</span>}
                </div>
              ))}
              <button className="image-box add">+ 추가</button>
            </div>
          </div>

          {/* 우측 폼 */}
          <div className="create-right">
            <div className="create-field">
              <label>상품명 <span className="required">*</span></label>
              <input value={title} onChange={(e) => setTitle(e.target.value)} />
            </div>

            <div className="create-field">
              <label>카테고리 <span className="required">*</span></label>
              <div className="chip-row">
                {categories.map((cat) => (
                  <button
                    key={cat}
                    className={`form-chip ${category === cat ? "active" : ""}`}
                    onClick={() => setCategory(cat)}
                  >
                    {cat}
                  </button>
                ))}
              </div>
            </div>

            <div className="create-field-row">
              <div className="create-field">
                <label>사이즈</label>
                <div className="chip-row">
                  {sizes.map((s) => (
                    <button
                      key={s}
                      className={`form-chip ${size === s ? "active" : ""}`}
                      onClick={() => setSize(s)}
                    >
                      {s}
                    </button>
                  ))}
                </div>
              </div>

              <div className="create-field">
                <label>색상</label>
                <input
                  value={color}
                  onChange={(e) => setColor(e.target.value)}
                />
              </div>
            </div>

            <div className="create-field">
              <label>상태 등급 <span className="required">*</span></label>
              <div className="chip-row">
                {grades.map((g) => (
                  <button
                    key={g.value}
                    className={`form-chip ${grade === g.value ? "active" : ""}`}
                    onClick={() => setGrade(g.value)}
                  >
                    {g.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="create-field">
              <label>희망 가격 <span className="required">*</span></label>
              <input
                type="number"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
              />
            </div>

            <div className="create-field">
              <label>상품 설명</label>
              <textarea
                rows={4}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            <div className="edit-actions">
              <button className="btn-cancel" onClick={() => navigate(-1)}>
                취소
              </button>
              <button className="create-submit edit-submit" onClick={handleSubmit}>
                수정 완료
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductEditPage;