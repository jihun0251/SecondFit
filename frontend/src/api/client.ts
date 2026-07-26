import type { ApiResponse } from "./types";

/**
 * 백엔드 통신 공통 모듈.
 *
 * 페이지마다 fetch를 직접 쓰면 토큰 헤더, 에러 처리, 응답 봉투 벗기기를
 * 21번 반복하게 된다. 여기 한 곳에 모아두고 페이지는 결과만 받아 쓴다.
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const API_PREFIX = "/api/v1";

const TOKEN_KEY = "secondfit_access_token";

// ===== 토큰 저장소 =====

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

// ===== 에러 =====

/**
 * 백엔드가 내려준 에러를 그대로 담는다.
 * code가 있으면 "EMAIL_DUPLICATED" 같은 값이라 화면에서 분기할 수 있다.
 */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
  }

  /** 인증 만료/누락 여부 */
  get isUnauthorized() {
    return this.status === 401;
  }
}

// ===== 요청 =====

interface RequestOptions {
  method?: "GET" | "POST" | "PATCH" | "PUT" | "DELETE";
  body?: unknown;
  /** FormData 전송 시 사용 (Content-Type을 브라우저가 정하도록 둬야 한다) */
  formData?: FormData;
  /** 인증 헤더를 붙일지. 기본 true (토큰이 없으면 어차피 안 붙는다) */
  auth?: boolean;
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, formData, auth = true } = options;

  const headers: Record<string, string> = {};
  const token = tokenStorage.get();

  if (auth && token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  // ⚠️ FormData일 때는 Content-Type을 직접 넣으면 안 된다.
  //    boundary 문자열이 빠져서 서버가 파싱에 실패한다.
  if (body !== undefined && !formData) {
    headers["Content-Type"] = "application/json";
  }

  let response: Response;
  try {
    response = await fetch(`${BASE_URL}${API_PREFIX}${path}`, {
      method,
      headers,
      body: formData ?? (body !== undefined ? JSON.stringify(body) : undefined),
    });
  } catch {
    // 네트워크 자체가 안 될 때 (서버 꺼짐 등)
    throw new ApiError(0, "NETWORK_ERROR", "서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.");
  }

  // 204 No Content — 삭제 API들이 본문 없이 응답한다
  if (response.status === 204) {
    return undefined as T;
  }

  let payload: ApiResponse<T>;
  try {
    payload = await response.json();
  } catch {
    throw new ApiError(response.status, "INVALID_RESPONSE", "서버 응답을 해석할 수 없습니다.");
  }

  if (!response.ok || payload.success === false) {
    throw new ApiError(
      response.status,
      payload.error?.code ?? "UNKNOWN",
      payload.error?.message ?? "요청 처리 중 오류가 발생했습니다."
    );
  }

  return payload.data as T;
}

// ===== 쿼리스트링 =====

/** undefined / 빈 문자열인 값은 빼고 쿼리스트링을 만든다 */
export function toQuery(params: Record<string, unknown>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;
    search.append(key, String(value));
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}

export const http = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: "POST", body }),
  patch: <T>(path: string, body?: unknown) => request<T>(path, { method: "PATCH", body }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
  postForm: <T>(path: string, formData: FormData) =>
    request<T>(path, { method: "POST", formData }),
  /** 로그인/회원가입처럼 토큰 없이 부르는 요청 */
  postPublic: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "POST", body, auth: false }),
};

/** 업로드 이미지 URL은 "/uploads/xxx.jpg" 형태라 호스트를 붙여줘야 한다 */
export function resolveImageUrl(url: string | null | undefined): string | undefined {
  if (!url) return undefined;
  if (url.startsWith("http")) return url;
  return `${BASE_URL}${url}`;
}
