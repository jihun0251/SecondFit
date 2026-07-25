export interface Product {
  id: number;
  name: string;
  price: number;
  size: string;
  grade: string;
  category: string;
  color: string;
  seller: string;
  sellerRating: number;
  subCategory: string;
  description: string;
  status: "ON_SALE" | "SOLD";
}

export const mockProducts: Product[] = [
  { id: 1042, name: "빈티지 워싱 데님 트러커 자켓", price: 89000, size: "M", grade: "A급", category: "아우터", color: "인디고 블루", seller: "seller_min", sellerRating: 4.9, subCategory: "데님 자켓", description: "90년대 빈티지 데님 트러커 자켓입니다. 자연스러운 워싱과 페이드가 매력적이며, 어깨선과 소매 상태가 매우 양호합니다.", status: "ON_SALE" },
  { id: 1043, name: "울 블렌드 오버코트", price: 124000, size: "L", grade: "S급", category: "아우터", color: "차콜 그레이", seller: "seller_kim", sellerRating: 4.7, subCategory: "코트", description: "울 70% 혼방 오버코트입니다. 착용 3회 미만의 새상품급 컨디션이며 보풀이나 오염이 없습니다.", status: "ON_SALE" },
  { id: 1044, name: "코듀로이 셔츠 자켓", price: 56000, size: "M", grade: "A급", category: "아우터", color: "베이지", seller: "seller_park", sellerRating: 4.8, subCategory: "셔츠 자켓", description: "부드러운 코듀로이 소재의 셔츠 자켓입니다. 가볍게 걸치기 좋은 간절기 아이템입니다.", status: "ON_SALE" },
  { id: 1045, name: "경량 패딩 베스트", price: 31000, size: "S", grade: "B급", category: "아우터", color: "블랙", seller: "seller_lee", sellerRating: 4.5, subCategory: "패딩", description: "경량 패딩 베스트입니다. 지퍼 부근에 약간의 사용감이 있으나 기능상 문제 없습니다.", status: "ON_SALE" },
  { id: 1046, name: "플리스 후드집업", price: 45000, size: "L", grade: "A급", category: "아우터", color: "아이보리", seller: "seller_min", sellerRating: 4.9, subCategory: "후드집업", description: "도톰한 플리스 소재 후드집업입니다. 보온성이 좋고 데일리로 착용하기 좋습니다.", status: "ON_SALE" },
  { id: 1047, name: "레더 라이더 자켓", price: 156000, size: "M", grade: "S급", category: "아우터", color: "블랙", seller: "seller_jung", sellerRating: 5.0, subCategory: "가죽 자켓", description: "소가죽 라이더 자켓입니다. 거의 착용하지 않아 가죽 컨디션이 매우 좋습니다.", status: "ON_SALE" },
  { id: 1048, name: "치노 팬츠", price: 33000, size: "M", grade: "B급", category: "하의", color: "카키", seller: "seller_park", sellerRating: 4.8, subCategory: "면바지", description: "베이직한 치노 팬츠입니다. 밑단에 약간의 사용감이 있습니다.", status: "ON_SALE" },
  { id: 1049, name: "캐시미어 니트", price: 67000, size: "L", grade: "A급", category: "상의", color: "오트밀", seller: "seller_kim", sellerRating: 4.7, subCategory: "니트", description: "캐시미어 혼방 니트입니다. 보풀이 거의 없고 촉감이 부드럽습니다.", status: "ON_SALE" },
  { id: 1050, name: "옥스포드 셔츠", price: 24000, size: "S", grade: "A급", category: "상의", color: "화이트", seller: "seller_lee", sellerRating: 4.5, subCategory: "셔츠", description: "베이직 옥스포드 셔츠입니다. 깔끔한 상태로 오피스룩에 적합합니다.", status: "ON_SALE" },
  { id: 1051, name: "와이드 데님 팬츠", price: 48000, size: "XL", grade: "S급", category: "하의", color: "라이트 블루", seller: "seller_jung", sellerRating: 5.0, subCategory: "청바지", description: "와이드 핏 데님 팬츠입니다. 새상품에 가까운 컨디션입니다.", status: "ON_SALE" },
  { id: 1052, name: "스웨이드 첼시 부츠", price: 72000, size: "L", grade: "A급", category: "신발", color: "브라운", seller: "seller_min", sellerRating: 4.9, subCategory: "부츠", description: "스웨이드 첼시 부츠입니다. 밑창 마모가 적고 상태가 좋습니다.", status: "ON_SALE" },
  { id: 1053, name: "캔버스 토트백", price: 29000, size: "M", grade: "B급", category: "가방", color: "네이비", seller: "seller_park", sellerRating: 4.8, subCategory: "토트백", description: "데일리 캔버스 토트백입니다. 내부에 약간의 얼룩이 있습니다.", status: "ON_SALE" },
];

export const mockLikes = [
  { id: 1042, price: 89000, status: "ON_SALE", priceDrop: 6000 },
  { id: 1050, price: 42000, status: "SOLD", priceDrop: 0 },
  { id: 1043, price: 124000, status: "ON_SALE", priceDrop: 0 },
  { id: 1048, price: 33000, status: "ON_SALE", priceDrop: 0 },
];

export const mockOrders = [
  { id: 9931, name: "데님 트러커 자켓", price: 92000, date: "07/02", status: "SHIPPING", statusLabel: "배송중", action: "배송 조회" },
  { id: 9930, name: "플리스 후드집업", price: 45000, date: "06/29", status: "DELIVERED", statusLabel: "배송완료", action: "거래 확정" },
  { id: 9929, name: "치노 팬츠", price: 33000, date: "06/20", status: "CONFIRMED", statusLabel: "거래완료", action: "리뷰 작성" },
];

// 내 프로필 (GET /users/me 응답 구조 기반)
export const mockProfile = {
  userId: 12,
  email: "min@email.com",
  nickname: "seller_min",
  phone: "010-****-5678",
  settlementAccount: "○○은행 123-****-8890",
  rating: 4.9,
  tradeCount: 32,
  joinedAt: "2025.11",
};

// 받은 리뷰 (GET /users/{userId}/reviews 응답 구조 기반)
export const mockReviews = {
  averageRating: 4.9,
  totalCount: 28,
  content: [
    { reviewId: 501, reviewer: "buyer_lee", rating: 5, content: "상태 설명 그대로예요. 포장도 꼼꼼하고 좋은 거래 감사합니다.", productTitle: "데님 트러커 자켓" },
    { reviewId: 502, reviewer: "kim_j", rating: 4, content: "니트 상태 괜찮아요. 배송이 조금 늦었지만 만족합니다.", productTitle: "울 니트 스웨터" },
    { reviewId: 503, reviewer: "park_s", rating: 5, content: "빠른 발송 감사합니다. 실물이 사진보다 더 좋네요.", productTitle: "코듀로이 셔츠 자켓" },
  ],
};

// 내 판매 상품 (products.status 흐름 기반)
export const mockSales = [
  { id: 1042, title: "빈티지 워싱 데님 트러커 자켓", price: 89000, status: "ON_SALE", statusLabel: "판매중", date: "07/20" },
  { id: 2001, title: "울 니트 스웨터", price: 38000, status: "PENDING_INBOUND", statusLabel: "입고 대기", date: "07/22" },
  { id: 2002, title: "코듀로이 셔츠 자켓", price: 56000, status: "PAID", statusLabel: "결제완료", date: "07/15" },
  { id: 2003, title: "레더 라이더 자켓", price: 156000, status: "SHIPPED", statusLabel: "출고", date: "07/10" },
  { id: 2004, title: "치노 팬츠", price: 33000, status: "SETTLED", statusLabel: "정산완료", date: "06/28" },
];

// AI 태깅 목업 응답 (POST /products/ai-tagging 응답 구조)
export const mockAiTagging = {
  available: true,
  category: "아우터 > 데님 자켓",
  color: "인디고 블루",
  style: "캐주얼 / 빈티지",
  gender: "공용",
  confidence: 0.92,
};

// ===== 관리자(본사) 목업 =====

// 입고 대기 목록 (GET /admin/inbounds)
export const mockInbounds = [
  { inboundId: 55, productId: 1042, title: "빈티지 워싱 데님 트러커 자켓", seller: "seller_min", price: 89000, status: "AWAITING" },
  { inboundId: 56, productId: 2001, title: "울 니트 스웨터", seller: "kim_j", price: 38000, status: "AWAITING" },
  { inboundId: 57, productId: 2002, title: "레더 라이더 자켓", seller: "seller_jung", price: 156000, status: "AWAITING" },
  { inboundId: 58, productId: 2003, title: "치노 팬츠", seller: "park_s", price: 33000, status: "CONFIRMED" },
];

// 출고 대기 주문 (GET /admin/orders, status=PAID)
export const mockAdminOrders = [
  { orderId: 9931, title: "데님 트러커 자켓", buyer: "buyer_lee", orderPrice: 92000, status: "PAID", statusLabel: "결제완료" },
  { orderId: 9932, title: "코듀로이 셔츠 자켓", buyer: "buyer_han", orderPrice: 59000, status: "PAID", statusLabel: "결제완료" },
  { orderId: 9930, title: "플리스 후드집업", buyer: "buyer_seo", orderPrice: 48000, status: "SHIPPED", statusLabel: "출고완료" },
];

// 정산 대기 (GET /admin/settlements, status=PENDING)
export const mockSettlements = [
  { settlementId: 3301, orderId: 9931, seller: "seller_min", grossAmount: 89000, feeAmount: 8900, netAmount: 80100, status: "PENDING" },
  { settlementId: 3302, orderId: 9929, seller: "park_s", grossAmount: 33000, feeAmount: 3300, netAmount: 29700, status: "PENDING" },
  { settlementId: 3300, orderId: 9928, seller: "kim_j", grossAmount: 67000, feeAmount: 6700, netAmount: 60300, status: "COMPLETED" },
];

// 신고 목록 (GET /admin/reports)
export const mockReports = [
  { reportId: 201, productId: 1088, seller: "unknown_9", reason: "FAKE", reasonLabel: "가품 의심", count: 3, status: "RECEIVED" },
  { reportId: 202, productId: 1091, seller: "seller_x", reason: "PROHIBITED", reasonLabel: "금지 품목", count: 1, status: "RECEIVED" },
  { reportId: 203, productId: 1077, seller: "user_44", reason: "SPAM", reasonLabel: "스팸/광고", count: 2, status: "RESOLVED" },
];