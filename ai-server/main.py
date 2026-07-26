"""
SecondFit AI 자동 태깅 추론 서버 (FastAPI)

현재는 실제 모델 없이 규칙 기반으로 제안값을 돌려주는 스텁이다.
백엔드 연동을 먼저 끝내두고, 모델 학습이 되는 대로 predict_with_model()만 갈아끼우면 된다.

실행:
    pip install -r requirements.txt
    uvicorn main:app --reload --port 8000

확인:
    http://localhost:8000/docs
"""

import hashlib
import io
from typing import Optional

from fastapi import FastAPI, File, HTTPException, UploadFile
from PIL import Image
from pydantic import BaseModel

app = FastAPI(title="SecondFit AI Tagging Server", version="0.1.0")

# 백엔드가 기대하는 응답 스키마와 1:1로 맞춰야 한다.
# (backend: AiTaggingClient.Prediction)
CATEGORIES = [
    "아우터 > 데님 자켓",
    "아우터 > 코트",
    "상의 > 니트",
    "상의 > 셔츠",
    "하의 > 데님 팬츠",
    "하의 > 슬랙스",
]
COLORS = ["인디고 블루", "블랙", "카멜", "아이보리", "차콜", "올리브"]
STYLES = ["캐주얼 / 빈티지", "미니멀", "스트릿", "클래식"]
GENDERS = ["공용", "남성", "여성"]

ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png", "image/webp", "image/gif"}


class Prediction(BaseModel):
    category: str
    color: str
    style: str
    gender: str
    confidence: float


@app.get("/health")
def health():
    """백엔드/모니터링이 살아있는지 확인하는 용도"""
    return {"status": "ok"}


@app.post("/predict", response_model=Prediction)
async def predict(image: UploadFile = File(...)):
    """
    대표 이미지 1장을 받아 카테고리/색상/스타일/성별을 예측한다.

    ⚠️ 백엔드는 이 엔드포인트가 실패해도 상품 등록을 막지 않는다(fail-safe).
       따라서 여기서 굳이 500을 감추려 애쓸 필요는 없다.
    """
    if image.content_type not in ALLOWED_CONTENT_TYPES:
        raise HTTPException(status_code=400, detail=f"지원하지 않는 형식: {image.content_type}")

    raw = await image.read()
    if not raw:
        raise HTTPException(status_code=400, detail="빈 파일입니다.")

    # 이미지로 열리는지 검증 (확장자만 바꿔 올린 파일 걸러내기)
    try:
        img = Image.open(io.BytesIO(raw))
        img.verify()
    except Exception:
        raise HTTPException(status_code=400, detail="이미지 파일이 아닙니다.")

    return predict_stub(raw)


def predict_stub(raw: bytes) -> Prediction:
    """
    모델 대신 쓰는 임시 구현.

    파일 내용의 해시로 값을 고르기 때문에 "같은 이미지 → 같은 결과"가 보장된다.
    랜덤으로 하면 테스트할 때마다 값이 바뀌어서 프론트 확인이 어렵다.
    """
    digest = hashlib.sha256(raw).digest()

    return Prediction(
        category=CATEGORIES[digest[0] % len(CATEGORIES)],
        color=COLORS[digest[1] % len(COLORS)],
        style=STYLES[digest[2] % len(STYLES)],
        gender=GENDERS[digest[3] % len(GENDERS)],
        # 0.70 ~ 0.99 사이
        confidence=round(0.70 + (digest[4] % 30) / 100, 2),
    )


def predict_with_model(raw: bytes) -> Optional[Prediction]:
    """
    TODO: 실제 모델 추론으로 교체할 자리.

    계획:
      1. 학습: Fashion 데이터셋(예: DeepFashion, K-Fashion)으로 ResNet/ViT 파인튜닝
      2. 카테고리는 분류 헤드, 색상은 대표색 추출(k-means) 또는 별도 분류 헤드
      3. 저장: torch.save → model.pt 를 이 디렉터리에 두고 서버 기동 시 1회만 로드
      4. confidence = softmax 최대값
    """
    raise NotImplementedError
