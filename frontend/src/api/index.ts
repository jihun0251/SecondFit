import { http, toQuery } from "./client";
import type {
  AdminOrderItem,
  AdminSettlementItem,
  AiTagging,
  Category,
  ImageUploaded,
  InboundItem,
  InboundStatus,
  LoginResponse,
  MyProfile,
  OrderDetail,
  OrderStatus,
  OrderSummary,
  Page,
  Payment,
  PaymentMethod,
  ProductCreateRequest,
  ProductDetail,
  ProductSearchParams,
  ProductStatus,
  ProductSummary,
  ProductUpdateRequest,
  PublicProfile,
  ReportItem,
  ReportReason,
  ReportStatus,
  SellerReviews,
  SettlementStatus,
  SettlementSummary,
  ShippingRequest,
  WishlistItem,
} from "./types";

/**
 * 도메인별 API 함수 모음.
 * 페이지에서는 여기 있는 함수만 호출하고, URL 문자열은 다루지 않는다.
 */

// ===== 인증 =====

export const authApi = {
  login: (email: string, password: string) =>
    http.postPublic<LoginResponse>("/auth/login", { email, password }),

  logout: () => http.post<{ message: string }>("/auth/logout"),
};

// ===== 회원 =====

export const userApi = {
  signup: (body: { email: string; password: string; nickname: string; phone?: string }) =>
    http.postPublic<{ userId: number }>("/users/signup", body),

  getMe: () => http.get<MyProfile>("/users/me"),

  updateMe: (body: {
    nickname?: string;
    phone?: string;
    profileImage?: string;
    settlementAccount?: string;
  }) => http.patch<{ userId: number; nickname: string; phone: string }>("/users/me", body),

  withdraw: () => http.delete<void>("/users/me"),

  getPublicProfile: (userId: number) => http.get<PublicProfile>(`/users/${userId}`),

  getReviews: (userId: number, page = 0, size = 20) =>
    http.get<SellerReviews>(`/users/${userId}/reviews${toQuery({ page, size })}`),
};

// ===== 카테고리 =====

export const categoryApi = {
  getTree: () => http.get<Category[]>("/categories"),

  getProducts: (categoryId: number, params: { sort?: string; page?: number; size?: number } = {}) =>
    http.get<Page<ProductSummary>>(`/categories/${categoryId}/products${toQuery(params)}`),
};

// ===== 상품 =====

export const productApi = {
  search: (params: ProductSearchParams = {}) =>
    http.get<Page<ProductSummary>>(`/products${toQuery(params as Record<string, unknown>)}`),

  getDetail: (productId: number) => http.get<ProductDetail>(`/products/${productId}`),

  getMine: (params: { status?: ProductStatus; page?: number; size?: number } = {}) =>
    http.get<Page<ProductSummary>>(`/products/me${toQuery(params)}`),

  create: (body: ProductCreateRequest) =>
    http.post<{ productId: number; status: ProductStatus }>("/products", body),

  update: (productId: number, body: ProductUpdateRequest) =>
    http.patch<{ productId: number; price: number; status: ProductStatus }>(
      `/products/${productId}`,
      body
    ),

  remove: (productId: number) => http.delete<void>(`/products/${productId}`),

  /**
   * 상품 등록 전 이미지 업로드 → URL 반환.
   * 등록 API가 URL 배열을 요구하므로 파일을 먼저 올려 URL을 확보한 뒤 create를 호출한다.
   */
  uploadImageFile: (file: File) => {
    const form = new FormData();
    form.append("file", file);
    return http.postForm<{ url: string }>("/products/images/upload", form);
  },

  /** 이미 등록된 상품에 이미지 추가 */
  uploadImage: (productId: number, file: File, isThumbnail = false) => {
    const form = new FormData();
    form.append("file", file);
    return http.postForm<ImageUploaded>(
      `/products/${productId}/images${toQuery({ isThumbnail })}`,
      form
    );
  },

  deleteImage: (productId: number, imageId: number) =>
    http.delete<void>(`/products/${productId}/images/${imageId}`),

  /** AI 자동 태깅 — 서버가 죽어도 available:false로 응답한다 */
  aiTagging: (image: File) => {
    const form = new FormData();
    form.append("image", image);
    return http.postForm<AiTagging>("/products/ai-tagging", form);
  },
};

// ===== 주문 =====

export const orderApi = {
  create: (productId: number) =>
    http.post<{ orderId: number; productId: number; orderPrice: number; status: OrderStatus }>(
      "/orders",
      { productId }
    ),

  getDetail: (orderId: number) => http.get<OrderDetail>(`/orders/${orderId}`),

  getMine: (params: { status?: OrderStatus; page?: number; size?: number } = {}) =>
    http.get<Page<OrderSummary>>(`/orders/me${toQuery(params)}`),

  updateShipping: (orderId: number, body: ShippingRequest) =>
    http.patch<{ orderId: number; shippingSaved: boolean; status: OrderStatus }>(
      `/orders/${orderId}/shipping`,
      body
    ),

  confirm: (orderId: number) =>
    http.post<{ orderId: number; status: OrderStatus; settlementId: number }>(
      `/orders/${orderId}/confirm`
    ),

  cancel: (orderId: number, reason?: string) =>
    http.post<{ orderId: number; status: OrderStatus }>(`/orders/${orderId}/cancel`, { reason }),
};

// ===== 결제 =====

export const paymentApi = {
  pay: (orderId: number, amount: number, method: PaymentMethod = "MOCK") =>
    http.post<{ paymentId: number; orderId: number; status: string; pgTid: string }>("/payments", {
      orderId,
      amount,
      method,
    }),

  getDetail: (paymentId: number) => http.get<Payment>(`/payments/${paymentId}`),
};

// ===== 정산 =====

export const settlementApi = {
  getMine: (params: { status?: SettlementStatus; page?: number; size?: number } = {}) =>
    http.get<Page<SettlementSummary>>(`/settlements/me${toQuery(params)}`),

  getDetail: (settlementId: number) => http.get<SettlementSummary>(`/settlements/${settlementId}`),
};

// ===== 리뷰 =====

export const reviewApi = {
  create: (orderId: number, rating: number, content?: string) =>
    http.post<{ reviewId: number; orderId: number; rating: number }>("/reviews", {
      orderId,
      rating,
      content,
    }),

  remove: (reviewId: number) => http.delete<void>(`/reviews/${reviewId}`),
};

// ===== 찜 =====

export const wishlistApi = {
  getMine: () => http.get<WishlistItem[]>("/wishlists"),

  add: (productId: number) =>
    http.post<{ wishlistId: number; productId: number }>("/wishlists", { productId }),

  remove: (productId: number) => http.delete<void>(`/wishlists/${productId}`),
};

// ===== 신고 =====

export const reportApi = {
  create: (productId: number, reason: ReportReason, detail?: string) =>
    http.post<{ reportId: number; status: ReportStatus }>("/reports", {
      productId,
      reason,
      detail,
    }),
};

// ===== 관리자 =====

export const adminApi = {
  getInbounds: (params: { status?: InboundStatus; page?: number; size?: number } = {}) =>
    http.get<Page<InboundItem>>(`/admin/inbounds${toQuery(params)}`),

  confirmInbound: (inboundId: number) =>
    http.post<{
      inboundId: number;
      productId: number;
      productStatus: ProductStatus;
      confirmedAt: string;
    }>(`/admin/inbounds/${inboundId}/confirm`),

  /** 관리자 주문 목록 (명세 외 추가 엔드포인트) */
  getOrders: (params: { status?: OrderStatus; page?: number; size?: number } = {}) =>
    http.get<Page<AdminOrderItem>>(`/admin/orders${toQuery(params)}`),

  /** 관리자 정산 목록 (명세 외 추가 엔드포인트) */
  getSettlements: (params: { status?: SettlementStatus; page?: number; size?: number } = {}) =>
    http.get<Page<AdminSettlementItem>>(`/admin/settlements${toQuery(params)}`),

  shipOrder: (orderId: number, trackingNo: string) =>
    http.post<{ orderId: number; status: OrderStatus; trackingNo: string }>(
      `/admin/orders/${orderId}/ship`,
      { trackingNo }
    ),

  deliverOrder: (orderId: number) =>
    http.post<{ orderId: number; status: OrderStatus; deliveredAt: string }>(
      `/admin/orders/${orderId}/deliver`
    ),

  completeSettlement: (settlementId: number) =>
    http.post<{
      settlementId: number;
      status: SettlementStatus;
      netAmount: number;
      settledAt: string;
    }>(`/admin/settlements/${settlementId}/complete`),

  getReports: (params: { status?: ReportStatus; page?: number; size?: number } = {}) =>
    http.get<Page<ReportItem>>(`/admin/reports${toQuery(params)}`),

  handleReport: (reportId: number, status: ReportStatus, action?: string) =>
    http.patch<{ reportId: number; status: ReportStatus; action: string; resolvedAt: string }>(
      `/admin/reports/${reportId}`,
      { status, action }
    ),
};
