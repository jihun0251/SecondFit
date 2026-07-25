import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import { mockAiTagging, mockProfile } from "../mocks/data";
import "./ProductCreatePage.css";

const categories = ["아우터", "상의", "하의", "신발", "가방", "악세서리"];
const sizes = ["S", "M", "L", "XL"];
const grades = [
  { label: "S (새상품급)", value: "NEW" },
  { label: "A (사용감 적음)", value: "LIKE_NEW" },
  { label: "B (사용감 있음)", value: "GOOD" },
];

function ProductCreatePage() {
  const navigate = useNavigate();

  const [images, setImages] = useState<string[]>([]);
  const [title, setTitle] = useState("");
  const [category, setCategory] = useState("");
  const [size, setSize] = useState("");
  const [color, setColor] = useState("");
  const [grade, setGrade] = useState("");
  const [price, setPrice] = useState("");
  const [description, setDescription] = useState("");

  // AI 태깅 상태
  const [aiLoading, setAiLoading] = useState(false);
  const [aiResult, setAiResult] = useState<typeof mockAiTagging | null>(null);

  // 이미지 추가 (실제 파일 대신 자리표시자)
  const handleAddImage = () => {
    if (images.length >= 8) {
      alert("이미지는 최대 8장까지 등록할 수 있습니다.");
      return;
    }
    const next = [...images, `img-${images.length + 1}`];
    setImages(next);

    // 첫 이미지가 올라오면 AI 태깅 자동 실행
    if (next.length === 1) {
      runAiTagging();
    }
  };

  // AI 자동 태깅 (2초 후 결과 반환하는 목업)
  const runAiTagging = () => {
    setAiLoading(true);
    setAiResult(null);
    setTimeout(() => {
      // 실제로는 POST /products/ai-tagging 호출
      setAiResult(mockAiTagging);
      setAiLoading(false);
    }, 2000);
  };

  // AI 제안값을 폼에 적용
  const applyAi = () => {
    if (!aiResult) return;
    // "아우터 > 데님 자켓" → 대분류 "아우터"만 추출
    const mainCat = aiResult.category.split(">")[0].trim();
    setCategory(mainCat);
    setColor(aiResult.color);
  };

  const handleSubmit = () => {
    if (images.length === 0) {
      alert("이미지를 최소 1장 등록해주세요.");
      return;
    }
    if (!title || !category || !grade || !price) {
      alert("필수 항목(상품명, 카테고리, 상태 등급, 가격)을 입력해주세요.");
      return;
    }
    // POST /products (백엔드 연동 지점)
    console.log("상품 등록:", {
      title, category, size, color, grade, price, description,
      aiSuggestedCategory: aiResult?.category,
      aiSuggestedColor: aiResult?.color,
      aiConfidence: aiResult?.confidence,
    });
    alert("상품이 등록되었습니다. (입고 대기 상태)");
    navigate("/my/sales");
  };

  return (
    <div className="create-page">
      <Header loggedIn userName={mockProfile.nickname} />
      <div className="create-body">
        <h2 className="create-title">상품 등록</h2>
        <p className="create-desc">
          대표 이미지를 올리면 AI가 카테고리·색상을 자동으로 추천해드립니다.
        </p>

        <div className="create-content">
          {/* 좌측: 이미지 + AI */}
          <div className="create-left">
            <h4 className="create-section-title">
              상품 이미지 <span className="required">*</span>
            </h4>
            <div className="image-grid">
              {images.map((img, i) => (
                <div className="image-box filled" key={img}>
                  이미지 {i + 1}
                  {i === 0 && <span className="thumb-badge">대표</span>}
                </div>
              ))}
              {images.length < 8 && (
                <button className="image-box add" onClick={handleAddImage}>
                  + 추가
                </button>
              )}
            </div>

            {/* AI 태깅 결과 패널 */}
            <div className="ai-panel">
              <div className="ai-panel-head">
                <span className="ai-title">🤖 AI 자동 태깅</span>
                {aiResult && (
                  <span className="ai-confidence">
                    신뢰도 {Math.round(aiResult.confidence * 100)}%
                  </span>
                )}
              </div>

              {aiLoading && (
                <div className="ai-loading">이미지 분석 중...</div>
              )}

              {!aiLoading && !aiResult && (
                <div className="ai-empty">
                  이미지를 등록하면 자동으로 분석됩니다.
                </div>
              )}

              {!aiLoading && aiResult && aiResult.available && (
                <>
                  <div className="ai-result">
                    <div className="ai-row">
                      <span className="ai-label">카테고리</span>
                      <span className="ai-value">{aiResult.category}</span>
                    </div>
                    <div className="ai-row">
                      <span className="ai-label">색상</span>
                      <span className="ai-value">{aiResult.color}</span>
                    </div>
                    <div className="ai-row">
                      <span className="ai-label">스타일</span>
                      <span className="ai-value">{aiResult.style}</span>
                    </div>
                    <div className="ai-row">
                      <span className="ai-label">성별</span>
                      <span className="ai-value">{aiResult.gender}</span>
                    </div>
                  </div>
                  <button className="ai-apply-btn" onClick={applyAi}>
                    제안값 적용하기
                  </button>
                </>
              )}

              {!aiLoading && aiResult && !aiResult.available && (
                <div className="ai-empty">
                  분석에 실패했어요. 직접 입력해주세요.
                </div>
              )}
            </div>
          </div>

          {/* 우측: 폼 */}
          <div className="create-right">
            <div className="create-field">
              <label>상품명 <span className="required">*</span></label>
              <input
                placeholder="예: 빈티지 워싱 데님 트러커 자켓"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
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
                  placeholder="예: 인디고 블루"
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
                placeholder="예: 89000"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
              />
            </div>

            <div className="create-field">
              <label>상품 설명</label>
              <textarea
                rows={4}
                placeholder="실측 사이즈, 상태, 착용 횟수 등을 적어주세요."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            <button className="create-submit" onClick={handleSubmit}>
              상품 등록
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductCreatePage;