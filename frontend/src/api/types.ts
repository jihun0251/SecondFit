/**
 * 백엔드 DTO와 1:1로 대응하는 타입 정의.
 * 백엔드를 수정하면 여기도 같이 고쳐야 한다.
 */

// ===== 공통 =====

/** 모든 응답을 감싸는 봉투 */
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
}

/** 페이징 응답 */
export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ===== Enum =====

export type Role = "USER" | "ADMIN";

export type ConditionGrade = "NEW" | "LIKE_NEW" | "GOOD" | "FAIR" | "POOR";

export type ProductStatus =
  | "PENDING_INBOUND"
  | "ON_SALE"
  | "PAID"
  | "SHIPPED"
  | "DELIVERED"
  | "SETTLED";

export type OrderStatus = "PAID" | "SHIPPED" | "DELIVERED" | "CONFIRMED" | "CANCELLED";

export type InboundStatus = "AWAITING" | "CONFIRMED";

export type SettlementStatus = "PENDING" | "COMPLETED";

export type PaymentMethod = "CARD" | "BANK_TRANSFER" | "MOCK";

export type PaymentStatus = "PAID" | "REFUNDED" | "FAILED";

export type ReportReason = "FAKE" | "ABUSE" | "PROHIBITED" | "SPAM" | "ETC";

export type ReportStatus = "RECEIVED" | "REVIEWING" | "RESOLVED" | "REJECTED";

/** 화면 표시용 한글 라벨 (백엔드는 항상 영문 enum을 준다) */
export const CONDITION_LABEL: Record<ConditionGrade, string> = {
  NEW: "새상품",
  LIKE_NEW: "S급",
  GOOD: "A급",
  FAIR: "B급",
  POOR: "C급",
};

export const PRODUCT_STATUS_LABEL: Record<ProductStatus, string> = {
  PENDING_INBOUND: "입고 대기",
  ON_SALE: "판매중",
  PAID: "결제완료",
  SHIPPED: "출고",
  DELIVERED: "배송완료",
  SETTLED: "정산완료",
};

export const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  PAID: "결제완료",
  SHIPPED: "배송중",
  DELIVERED: "배송완료",
  CONFIRMED: "거래완료",
  CANCELLED: "취소됨",
};

export const REPORT_REASON_LABEL: Record<ReportReason, string> = {
  FAKE: "가품 의심",
  ABUSE: "욕설/비방",
  PROHIBITED: "금지 품목",
  SPAM: "스팸/광고",
  ETC: "기타",
};

export const SETTLEMENT_STATUS_LABEL: Record<SettlementStatus, string> = {
  PENDING: "정산 대기",
  COMPLETED: "정산 완료",
};

// ===== 인증 / 회원 =====

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: { userId: number; nickname: string; role: Role };
}

export interface MyProfile {
  userId: number;
  email: string;
  nickname: string;
  phone: string | null;
  profileImage: string | null;
  settlementAccount: string | null;
  rating: number;
  tradeCount: number;
  role: Role;
}

export interface PublicProfile {
  userId: number;
  nickname: string;
  profileImage: string | null;
  rating: number;
  tradeCount: number;
  onSaleProducts: { productId: number; title: string; price: number; thumbnail: string | null }[];
}

// ===== 카테고리 =====

export interface Category {
  id: number;
  name: string;
  children: Category[];
}

// ===== 상품 =====

export interface ProductSummary {
  productId: number;
  title: string;
  price: number;
  size: string | null;
  conditionGrade: ConditionGrade;
  thumbnail: string | null;
  status: ProductStatus;
  createdAt: string;
}

export interface ProductDetail {
  productId: number;
  title: string;
  description: string | null;
  price: number;
  size: string | null;
  color: string | null;
  conditionGrade: ConditionGrade;
  category: { id: number; name: string } | null;
  status: ProductStatus;
  viewCount: number;
  images: { imageId: number; url: string; isThumbnail: boolean }[];
  seller: { userId: number; nickname: string; rating: number | null };
}

export interface ProductCreateRequest {
  categoryId: number;
  title: string;
  description?: string;
  price: number;
  size?: string;
  color?: string;
  conditionGrade: ConditionGrade;
  images: string[];
  thumbnailIndex?: number;
  aiSuggestedCategory?: string;
  aiSuggestedColor?: string;
  aiConfidence?: number;
}

export interface ProductUpdateRequest {
  categoryId?: number;
  title?: string;
  description?: string;
  price?: number;
  size?: string;
  color?: string;
  conditionGrade?: ConditionGrade;
}

export interface ProductSearchParams {
  keyword?: string;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  size?: string;
  conditionGrade?: ConditionGrade;
  sort?: "latest" | "price_asc" | "price_desc";
  page?: number;
  pageSize?: number;
}

export interface AiTagging {
  available: boolean;
  category: string | null;
  color: string | null;
  style: string | null;
  gender: string | null;
  confidence: number | null;
}

export interface ImageUploaded {
  imageId: number;
  url: string;
  isThumbnail: boolean;
}

// ===== 주문 / 결제 =====

export interface OrderSummary {
  orderId: number;
  title: string;
  orderPrice: number;
  status: OrderStatus;
  paidAt: string;
}

export interface OrderDetail {
  orderId: number;
  product: { productId: number; title: string };
  orderPrice: number;
  status: OrderStatus;
  shipping: {
    receiverName: string | null;
    receiverPhone: string | null;
    zipcode: string | null;
    address1: string | null;
    address2: string | null;
    memo: string | null;
    trackingNo: string | null;
  };
  timeline: {
    paidAt: string | null;
    shippedAt: string | null;
    deliveredAt: string | null;
    confirmedAt: string | null;
  };
}

export interface ShippingRequest {
  receiverName: string;
  receiverPhone: string;
  zipcode: string;
  address1: string;
  address2?: string;
  memo?: string;
}

export interface Payment {
  paymentId: number;
  orderId: number;
  amount: number;
  method: PaymentMethod;
  status: PaymentStatus;
  paidAt: string;
}

// ===== 정산 =====

export interface SettlementSummary {
  settlementId: number;
  orderId: number;
  grossAmount: number;
  feeAmount: number;
  netAmount: number;
  status: SettlementStatus;
}

// ===== 리뷰 =====

export interface ReviewItem {
  reviewId: number;
  reviewer: string;
  rating: number;
  content: string | null;
  productTitle: string;
  createdAt: string;
}

export interface SellerReviews {
  averageRating: number;
  totalCount: number;
  content: ReviewItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// ===== 찜 =====

export interface WishlistItem {
  productId: number;
  title: string;
  price: number;
  priceChange: number;
  status: ProductStatus;
  thumbnail: string | null;
}

// ===== 신고 =====

export interface ReportItem {
  reportId: number;
  productId: number | null;
  seller: string | null;
  reason: ReportReason;
  detail: string | null;
  count: number;
  status: ReportStatus;
  createdAt: string;
}

// ===== 관리자 =====

export interface InboundItem {
  inboundId: number;
  productId: number;
  title: string;
  seller: string;
  price: number;
  trackingNo: string | null;
  status: InboundStatus;
}

/** 관리자 주문 목록 (명세 외 추가 엔드포인트 GET /admin/orders) */
export interface AdminOrderItem {
  orderId: number;
  productId: number;
  title: string;
  buyer: string;
  orderPrice: number;
  status: OrderStatus;
  trackingNo: string | null;
  /** 배송지가 입력되어 출고 가능한 상태인지 */
  shippingReady: boolean;
  paidAt: string;
}

/** 관리자 정산 목록 (명세 외 추가 엔드포인트 GET /admin/settlements) */
export interface AdminSettlementItem {
  settlementId: number;
  orderId: number;
  seller: string;
  grossAmount: number;
  feeAmount: number;
  netAmount: number;
  status: SettlementStatus;
  settlementAccount: string | null;
}
