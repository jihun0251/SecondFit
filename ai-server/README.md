# SecondFit AI 추론 서버

상품 대표 이미지를 받아 **카테고리 / 색상 / 스타일 / 성별**을 예측해 제안값으로 돌려준다.
백엔드(`POST /api/v1/products/ai-tagging`)가 이 서버를 호출한다.

## 실행

```bash
cd ai-server
python -m venv .venv
.venv\Scripts\activate        # macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

API 문서: http://localhost:8000/docs

## 엔드포인트

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| GET | `/health` | 헬스체크 |
| POST | `/predict` | 이미지 1장(`image` 폼 필드) → 예측 결과 |

응답 예시:

```json
{
  "category": "아우터 > 데님 자켓",
  "color": "인디고 블루",
  "style": "캐주얼 / 빈티지",
  "gender": "공용",
  "confidence": 0.92
}
```

## 현재 상태

**모델 없이 동작하는 스텁입니다.** 파일 해시로 값을 고르기 때문에
같은 이미지를 넣으면 항상 같은 결과가 나옵니다(프론트 확인용).

실제 모델로 교체할 때는 `main.py`의 `predict_with_model()`만 구현하고
`predict()`에서 `predict_stub()` 대신 호출하면 됩니다.

## 백엔드 연동 시 알아둘 것

- 백엔드는 **이 서버가 꺼져 있어도 정상 동작**합니다.
  호출 실패 시 `{ "available": false, ... }`로 응답하고 상품 등록은 그대로 진행됩니다.
- 백엔드 타임아웃은 연결 1초 / 읽기 3초입니다 (`application.yaml`의 `ai.server.*`).
  모델 추론이 3초를 넘기면 fail-safe로 넘어가니, 느려지면 타임아웃을 늘리세요.
- 서버 주소는 환경변수 `AI_SERVER_URL`로 바꿀 수 있습니다 (기본 `http://localhost:8000`).
