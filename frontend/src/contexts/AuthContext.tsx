import { createContext, useContext, useEffect, useState } from "react";
import type { ReactNode } from "react";
import { authApi } from "../api";
import { tokenStorage } from "../api/client";
import type { Role } from "../api/types";

/**
 * 로그인 상태 보관소.
 *
 * 토큰과 사용자 정보를 localStorage에 넣어두기 때문에
 * 새로고침해도 로그인이 풀리지 않는다.
 */

interface AuthUser {
  userId: number;
  nickname: string;
  role: Role;
}

interface AuthContextType {
  user: AuthUser | null;
  isLoggedIn: boolean;
  isAdmin: boolean;
  login: (email: string, password: string) => Promise<AuthUser>;
  logout: () => void;
}

const USER_KEY = "secondfit_user";

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function readStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(readStoredUser);

  // 토큰만 지워졌는데 사용자 정보가 남아 있는 어긋난 상태를 정리한다
  useEffect(() => {
    if (user && !tokenStorage.get()) {
      localStorage.removeItem(USER_KEY);
      setUser(null);
    }
  }, [user]);

  const login = async (email: string, password: string) => {
    const result = await authApi.login(email, password);

    tokenStorage.set(result.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify(result.user));
    setUser(result.user);

    return result.user;
  };

  const logout = () => {
    // 서버는 무상태(JWT)라 실제 무효화는 토큰을 지우는 것으로 이뤄진다.
    // 서버 호출은 실패해도 무시하고 진행한다.
    authApi.logout().catch(() => undefined);

    tokenStorage.clear();
    localStorage.removeItem(USER_KEY);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoggedIn: user !== null,
        isAdmin: user?.role === "ADMIN",
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth는 AuthProvider 안에서만 사용할 수 있습니다.");
  }
  return context;
}
