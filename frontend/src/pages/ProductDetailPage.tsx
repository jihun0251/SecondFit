import { useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import Header from "../components/Header";
import { productApi, reportApi } from "../api";
import { resolveImageUrl } from "../api/client";
import { CONDITION_LABEL, PRODUCT_STATUS_LABEL, REPORT_REASON_LABEL } from "../api/types";
import type { ReportReason } from "../api/types";
import { useAction, useAsync } from "../hooks/useAsync";
import { useAuth } from "../contexts/AuthContext";
import { useWishlist } from "../contexts/WishlistContext";
import "./ProductDetailPage.css";

function ProductDetailPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const id = Number(productId);

  const { user } = useAuth();
  const { isWished, toggleWish } = useWishlist();
  const { run, running } = useAction();

  const { data: product, loading, error } = useAsync(() => productApi.getDetail(id), [id]);

  // 이미지 여러 장 중 현재 보고 있는 것
  const [activeImage, setActiveImage] = useState(0);

  if (loading) {
    return (
      <div className="detail-page">
        <Header />
        <div className="detail-notfound">불러오는 중...</div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="detail-page">
        <Header />
        <div className="detail-notfound">
          <p>{error ?? "존재하지 않는 상품입니다."}</p>
          <Link to="/products" className="btn-back">상품 목록으로</Link>
        </div>
      </div>
    );
  }

  const liked = isWished(product.productId);
  const onSale = product.status === "ON_SALE";
  const isMine = user?.userId === product.seller.userId;
  const mainImage = product.images[activeImage] ?? product.images[0];

  const handleReport = () => {
    const reason = window.prompt(
      `신고 사유를 입력하세요.\n${Object.entries(REPORT_REASON_LABEL)
        .map(([key, label]) => `${key} = ${label}`)
        .join("\n")}`,
      "FAKE"
    );
    if (!reason) return;
    if (!(reason in REPORT_REASON_LABEL)) {
      alert("올바른 사유 코드가 아닙니다.");
      return;
    }
    const detail = window.prompt("상세 사유 (선택)") ?? undefined;
    void run(
      async () => {
        await reportApi.create(product.productId, reason as ReportReason, detail);
      },
      "신고가 접수되었습니다."
    );
  };

  return (
    <div className="detail-page">
      <Header />
      <div className="detail-body">
        {/* 좌측 이미지 */}
        <div className="detail-images">
          <div
            className="detail-main-image"
            style={
              mainImage
                ? {
                    backgroundImage: `url(${resolveImageUrl(mainImage.url)})`,
                    backgroundSize: "cover",
                    backgroundPosition: "center",
                  }
                : undefined
            }
          >
            {!mainImage && "이미지 없음"}
          </div>
          <div className="detail-thumbs">
            {product.images.map((img, index) => (
              <div
                className={`detail-thumb ${index === activeImage ? "active" : ""}`}
                key={img.imageId}
                onClick={() => setActiveImage(index)}
                style={{
                  backgroundImage: `url(${resolveImageUrl(img.url)})`,
                  backgroundSize: "cover",
                  backgroundPosition: "center",
                  cursor: "pointer",
                }}
              />
            ))}
          </div>
        </div>

        {/* 우측 정보 */}
        <div className="detail-info">
          <div className="detail-top">
            <span className={`badge-onsale ${onSale ? "" : "badge-sold"}`}>
              ● {PRODUCT_STATUS_LABEL[product.status]}
            </span>
            <span className="detail-category">{product.category?.name ?? "미분류"}</span>
          </div>

          <h1 className="detail-name">{product.title}</h1>
          <p className="detail-price">{product.price.toLocaleString()}원</p>

          <div className="detail-spec">
            <div className="spec-row">
              <span className="spec-label">사이즈</span>
              <span className="spec-value">{product.size ?? "-"}</span>
            </div>
            <div className="spec-row">
              <span className="spec-label">상태 등급</span>
              <span className="spec-value">{CONDITION_LABEL[product.conditionGrade]}</span>
            </div>
            <div className="spec-row">
              <span className="spec-label">색상</span>
              <span className="spec-value">{product.color ?? "-"}</span>
            </div>
            <div className="spec-row">
              <span className="spec-label">판매자</span>
              <span className="spec-value">
                {product.seller.nickname}
                {product.seller.rating ? ` · ★ ${product.seller.rating}` : ""}
              </span>
            </div>
            <div className="spec-row">
              <span className="spec-label">조회수</span>
              <span className="spec-value">{product.viewCount}</span>
            </div>
          </div>

          <h4 className="detail-desc-title">상품 설명</h4>
          <p className="detail-desc-text">{product.description ?? "등록된 설명이 없습니다."}</p>

          <div className="detail-actions">
            <button
              className={`btn-like ${liked ? "liked" : ""}`}
              onClick={() => void toggleWish(product.productId)}
            >
              {liked ? "♥ 찜한 상품" : "♡ 찜하기"}
            </button>

            {isMine ? (
              // 본인 상품은 구매할 수 없다 (서버도 403으로 막는다)
              <button
                className="btn-buy"
                onClick={() => navigate(`/products/${product.productId}/edit`)}
              >
                상품 수정
              </button>
            ) : (
              <button
                className="btn-buy"
                disabled={!onSale}
                onClick={() => navigate(`/checkout/${product.productId}`)}
              >
                {onSale ? "바로 구매" : "판매 종료"}
              </button>
            )}
          </div>

          {!isMine && (
            <button className="detail-report" onClick={handleReport} disabled={running}>
              🚩 이 상품 신고하기
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default ProductDetailPage;
