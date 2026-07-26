import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import { categoryApi, productApi } from "../api";
import { resolveImageUrl } from "../api/client";
import { CONDITION_LABEL } from "../api/types";
import type { ConditionGrade } from "../api/types";
import { useAction, useAsync } from "../hooks/useAsync";
import "./ProductCreatePage.css";

const sizes = ["S", "M", "L", "XL"];
const grades: ConditionGrade[] = ["NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"];

function ProductEditPage() {
  const { productId } = useParams();
  const id = Number(productId);
  const navigate = useNavigate();

  const { data: product, loading, error } = useAsync(() => productApi.getDetail(id), [id]);
  const { data: categories } = useAsync(() => categoryApi.getTree(), []);
  const { run, running } = useAction();

  const [title, setTitle] = useState("");
  const [categoryId, setCategoryId] = useState<number | null>(null);
  const [size, setSize] = useState("");
  const [color, setColor] = useState("");
  const [grade, setGrade] = useState<ConditionGrade>("GOOD");
  const [price, setPrice] = useState("");
  const [description, setDescription] = useState("");

  // 상품을 불러오면 폼 초기값으로 채운다
  useEffect(() => {
    if (!product) return;
    setTitle(product.title);
    setCategoryId(product.category?.id ?? null);
    setSize(product.size ?? "");
    setColor(product.color ?? "");
    setGrade(product.conditionGrade);
    setPrice(String(product.price));
    setDescription(product.description ?? "");
  }, [product]);

  const handleSubmit = async () => {
    await run(async () => {
      await productApi.update(id, {
        categoryId: categoryId ?? undefined,
        title,
        description,
        price: Number(price),
        size: size || undefined,
        color: color || undefined,
        conditionGrade: grade,
      });
      alert("상품 정보가 수정되었습니다.");
      navigate("/my/sales");
    });
  };

  const handleDelete = async () => {
    if (!window.confirm("상품을 삭제할까요? 되돌릴 수 없습니다.")) return;
    await run(async () => {
      await productApi.remove(id);
      alert("상품이 삭제되었습니다.");
      navigate("/my/sales");
    });
  };

  if (loading) {
    return (
      <div className="create-page">
        <Header />
        <div className="create-body">불러오는 중...</div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="create-page">
        <Header />
        <div className="create-body">{error ?? "상품을 찾을 수 없습니다."}</div>
      </div>
    );
  }

  // 입고 확인이 끝나면 서버가 409로 막는다. 화면에서도 미리 알려준다.
  const editable = product.status === "PENDING_INBOUND";

  return (
    <div className="create-page">
      <Header />
      <div className="create-body">
        <h2 className="create-title">상품 수정</h2>
        <p className="create-desc">
          {editable
            ? "입고 확인 전 상품만 수정할 수 있습니다."
            : "⚠️ 이미 입고 확인된 상품이라 수정할 수 없습니다."}
        </p>

        <div className="create-content">
          {/* 좌측 이미지 (수정 화면은 AI 패널 없음) */}
          <div className="create-left">
            <h4 className="create-section-title">상품 이미지</h4>
            <div className="image-grid">
              {product.images.map((img, i) => (
                <div
                  className="image-box filled"
                  key={img.imageId}
                  style={{
                    backgroundImage: `url(${resolveImageUrl(img.url)})`,
                    backgroundSize: "cover",
                    backgroundPosition: "center",
                  }}
                >
                  {img.isThumbnail && <span className="thumb-badge">대표</span>}
                  {i === -1 && null}
                </div>
              ))}
            </div>
          </div>

          {/* 우측 폼 */}
          <div className="create-right">
            <div className="create-field">
              <label>상품명 <span className="required">*</span></label>
              <input value={title} onChange={(e) => setTitle(e.target.value)} disabled={!editable} />
            </div>

            <div className="create-field">
              <label>카테고리 <span className="required">*</span></label>
              <div className="chip-row">
                {(categories ?? []).flatMap((cat) => [cat, ...cat.children]).map((cat) => (
                  <button
                    key={cat.id}
                    className={`form-chip ${categoryId === cat.id ? "active" : ""}`}
                    onClick={() => setCategoryId(cat.id)}
                    disabled={!editable}
                  >
                    {cat.name}
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
                      disabled={!editable}
                    >
                      {s}
                    </button>
                  ))}
                </div>
              </div>

              <div className="create-field">
                <label>색상</label>
                <input value={color} onChange={(e) => setColor(e.target.value)} disabled={!editable} />
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
                    disabled={!editable}
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
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                disabled={!editable}
              />
            </div>

            <div className="create-field">
              <label>상품 설명</label>
              <textarea
                rows={4}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                disabled={!editable}
              />
            </div>

            <div className="edit-actions">
              <button className="btn-cancel" onClick={() => navigate(-1)}>취소</button>
              {editable && (
                <button className="btn-cancel" onClick={handleDelete} disabled={running}>
                  삭제
                </button>
              )}
              <button
                className="create-submit edit-submit"
                onClick={handleSubmit}
                disabled={!editable || running}
              >
                {running ? "처리 중..." : "수정 완료"}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductEditPage;
