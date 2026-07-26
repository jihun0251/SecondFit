import { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/Header";
import { categoryApi, productApi } from "../api";
import { CONDITION_LABEL } from "../api/types";
import type { AiTagging, ConditionGrade } from "../api/types";
import { useAction, useAsync } from "../hooks/useAsync";
import "./ProductCreatePage.css";

const sizes = ["S", "M", "L", "XL"];

const grades: ConditionGrade[] = ["NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"];

/** 화면에서 다루는 이미지 1장 (아직 서버에 안 올라간 상태) */
interface PickedImage {
  file: File;
  previewUrl: string;
}

function ProductCreatePage() {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [images, setImages] = useState<PickedImage[]>([]);
  const [title, setTitle] = useState("");
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [size, setSize] = useState("");
  const [color, setColor] = useState("");
  const [grade, setGrade] = useState<ConditionGrade | "">("");
  const [price, setPrice] = useState("");
  const [description, setDescription] = useState("");

  const [aiLoading, setAiLoading] = useState(false);
  const [aiResult, setAiResult] = useState<AiTagging | null>(null);

  const { data: categories } = useAsync(() => categoryApi.getTree(), []);
  const { run, running } = useAction();

  // 대분류를 고르면 그 아래 소분류를 보여준다
  const [parentId, setParentId] = useState<number | null>(null);
  const children = categories?.find((c) => c.id === parentId)?.children ?? [];

  const handlePickFiles = (event: React.ChangeEvent<HTMLInputElement>) => {
    const picked = Array.from(event.target.files ?? []);
    if (picked.length === 0) return;

    if (images.length + picked.length > 8) {
      alert("이미지는 최대 8장까지 등록할 수 있습니다.");
      return;
    }

    const next = picked.map((file) => ({
      file,
      previewUrl: URL.createObjectURL(file), // 업로드 전 미리보기용
    }));
    const merged = [...images, ...next];
    setImages(merged);

    // 첫 이미지가 들어오면 대표 이미지로 AI 태깅을 돌린다
    if (images.length === 0) {
      void runAiTagging(next[0].file);
    }

    event.target.value = ""; // 같은 파일 다시 선택해도 onChange가 뜨도록
  };

  const runAiTagging = async (file: File) => {
    setAiLoading(true);
    setAiResult(null);
    try {
      // 추론 서버가 꺼져 있어도 available:false가 돌아올 뿐 실패하지 않는다
      setAiResult(await productApi.aiTagging(file));
    } catch {
      setAiResult({ available: false, category: null, color: null, style: null, gender: null, confidence: null });
    } finally {
      setAiLoading(false);
    }
  };

  /** AI 제안값을 폼에 채워 넣는다 (판매자가 고칠 수 있는 "제안"일 뿐) */
  const applyAi = () => {
    if (!aiResult?.available) return;

    if (aiResult.color) setColor(aiResult.color);

    // "아우터 > 데님 자켓" 형태를 실제 카테고리 ID로 맞춰본다
    if (aiResult.category && categories) {
      const [mainName, subName] = aiResult.category.split(">").map((s) => s.trim());
      const parent = categories.find((c) => c.name === mainName);
      if (parent) {
        setParentId(parent.id);
        const child = parent.children.find((c) => c.name === subName);
        setCategoryId(child ? child.id : parent.id);
      }
    }
  };

  const removeImage = (index: number) => {
    URL.revokeObjectURL(images[index].previewUrl);
    setImages(images.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    if (images.length === 0) {
      alert("이미지를 최소 1장 등록해주세요.");
      return;
    }
    if (!title || !categoryId || !grade || !price) {
      alert("필수 항목(상품명, 카테고리, 상태 등급, 가격)을 입력해주세요.");
      return;
    }

    await run(async () => {
      // 1) 파일을 먼저 올려 URL을 확보한다 (등록 API가 URL 배열을 요구하므로)
      const uploaded = await Promise.all(
        images.map((image) => productApi.uploadImageFile(image.file))
      );

      // 2) 확보한 URL로 상품을 등록한다 → PENDING_INBOUND(입고 대기)로 생성됨
      await productApi.create({
        categoryId,
        title,
        description: description || undefined,
        price: Number(price),
        size: size || undefined,
        color: color || undefined,
        conditionGrade: grade,
        images: uploaded.map((u) => u.url),
        thumbnailIndex: 0,
        // AI 원본 예측값도 함께 보관한다 (판매자가 값을 고쳐도 원본은 남는다)
        aiSuggestedCategory: aiResult?.category ?? undefined,
        aiSuggestedColor: aiResult?.color ?? undefined,
        aiConfidence: aiResult?.confidence ?? undefined,
      });

      alert("상품이 등록되었습니다. 본사 입고 확인 후 판매가 시작됩니다.");
      navigate("/my/sales");
    });
  };

  return (
    <div className="create-page">
      <Header />
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
              {images.map((image, i) => (
                <div
                  className="image-box filled"
                  key={image.previewUrl}
                  style={{
                    backgroundImage: `url(${image.previewUrl})`,
                    backgroundSize: "cover",
                    backgroundPosition: "center",
                  }}
                  onClick={() => removeImage(i)}
                  title="클릭하면 제거됩니다"
                >
                  {i === 0 && <span className="thumb-badge">대표</span>}
                </div>
              ))}
              {images.length < 8 && (
                <button className="image-box add" onClick={() => fileInputRef.current?.click()}>
                  + 추가
                </button>
              )}
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              multiple
              style={{ display: "none" }}
              onChange={handlePickFiles}
            />

            {/* AI 태깅 결과 패널 */}
            <div className="ai-panel">
              <div className="ai-panel-head">
                <span className="ai-title">🤖 AI 자동 태깅</span>
                {aiResult?.available && aiResult.confidence != null && (
                  <span className="ai-confidence">
                    신뢰도 {Math.round(aiResult.confidence * 100)}%
                  </span>
                )}
              </div>

              {aiLoading && <div className="ai-loading">이미지 분석 중...</div>}

              {!aiLoading && !aiResult && (
                <div className="ai-empty">이미지를 등록하면 자동으로 분석됩니다.</div>
              )}

              {!aiLoading && aiResult?.available && (
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
                  AI 서버에 연결하지 못했습니다. 직접 입력해주세요.
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
                {(categories ?? []).map((cat) => (
                  <button
                    key={cat.id}
                    className={`form-chip ${parentId === cat.id ? "active" : ""}`}
                    onClick={() => {
                      setParentId(cat.id);
                      setCategoryId(cat.id);
                    }}
                  >
                    {cat.name}
                  </button>
                ))}
              </div>
              {children.length > 0 && (
                <div className="chip-row" style={{ marginTop: 8 }}>
                  {children.map((child) => (
                    <button
                      key={child.id}
                      className={`form-chip ${categoryId === child.id ? "active" : ""}`}
                      onClick={() => setCategoryId(child.id)}
                    >
                      {child.name}
                    </button>
                  ))}
                </div>
              )}
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
                    key={g}
                    className={`form-chip ${grade === g ? "active" : ""}`}
                    onClick={() => setGrade(g)}
                  >
                    {CONDITION_LABEL[g]}
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

            <button className="create-submit" onClick={handleSubmit} disabled={running}>
              {running ? "등록 중..." : "상품 등록"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductCreatePage;
