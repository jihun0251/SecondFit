# SecondFit

> 이미지 기반 AI 자동 태깅을 적용한 중고 의류 위탁·중개 플랫폼

판매자가 상품 사진을 올리면 직접 학습시킨 이미지 분류 모델이 카테고리·색상 등을 자동으로 예측해 등록 폼을 채워주는 중고 의류 거래 서비스입니다. 크림(KREAM)형 위탁·중개 모델을 단순화하여, 판매자는 본사로 상품을 발송하고 판매글만 등록하며 구매자는 배송지를 입력해 본사로부터 상품을 수령합니다.

개인 학습 프로젝트로, 풀스택 웹 개발과 AI 모델 서빙을 함께 경험하는 데 중점을 둡니다.

<br>

## 주요 기능

- **회원/인증** — 이메일 회원가입·로그인(JWT), USER/ADMIN 역할 구분
- **상품 관리** — 이미지 업로드·등록, 목록/상세 조회, 카테고리·사이즈·가격·상태 필터
- **AI 자동 태깅** — 상품 이미지 기반 카테고리·색상 자동 예측 (프로젝트 핵심)
- **거래 흐름** — 위탁 등록 → 본사 입고 확인 → 판매 → 결제 → 출고 → 배송 → 정산
- **부가 기능** — 찜, 리뷰/평점, 신고, 관리자 대시보드

<br>

## 아키텍처

```
[React 프론트엔드] ── 이미지 업로드
        │
        ▼
[Spring Boot 백엔드] ── 내부 API 호출
        │
        ▼
[FastAPI 추론 서버] ── PyTorch (ResNet18 / ViT 전이학습)
        │
        ▼
  예측 결과(JSON) → 등록 폼 자동완성
```

이미지 추론 모델은 별도의 FastAPI 마이크로서비스로 분리하여 서빙하고, Spring Boot 백엔드가 내부 API로 호출하는 구조입니다.

<br>

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Frontend | React, Vite |
| Backend | Spring Boot, Spring Security, JWT, JPA |
| Database | MySQL |
| AI | PyTorch, torchvision (ResNet18 / ViT), FastAPI |
| 학습 환경 | Google Colab (GPU) |
| 형상관리 | GitHub (모노레포) |
| 도구 | ERDCloud, Figma, Notion |

<br>

## 프로젝트 구조

```
SecondFit/
├── backend/      # Spring Boot REST API
├── frontend/     # React (Vite)
└── ai-server/    # FastAPI + PyTorch 추론 서버
```

<br>

## 실행 방법

### 사전 요구 사항

- JDK 17
- Node.js (LTS 권장)
- MySQL 8.0
- Python 3.x (ai-server)

### 1. 데이터베이스 준비

```sql
CREATE DATABASE secondfit CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2. Backend

`backend/src/main/resources/application.yml`의 DB 접속 정보를 환경에 맞게 설정하고, DB 비밀번호는 환경변수(`DB_PASSWORD`)로 주입합니다.

```bash
cd backend
./gradlew bootRun
```

기본 포트: `http://localhost:8080`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

기본 포트: `http://localhost:5173`

### 4. AI Server

```bash
cd ai-server
# (세팅 예정)
```

<br>

## 개발 현황

- [x] 모노레포 초기 구성
- [x] Backend 프로젝트 세팅 및 MySQL 연동
- [x] Frontend (Vite + React) 세팅
- [ ] 도메인 엔티티 및 REST API 구현
- [ ] AI 모델 학습 및 FastAPI 서빙
- [ ] 프론트엔드 화면 및 API 연동
- [ ] 배포

<br>

## 문서

- ERD / API 명세: Notion
- 기술 블로그: [jjh00251.tistory.com](https://jjh00251.tistory.com)

<br>

## 개발자

**정지훈 (JI HOON JUNG)** — 한국공학대학교 컴퓨터공학부
