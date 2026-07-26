import { createContext, useCallback, useContext, useEffect, useState } from "react";
import type { ReactNode } from "react";
import { wishlistApi } from "../api";
import type { WishlistItem } from "../api/types";
import { useAuth } from "./AuthContext";

/**
 * 찜 상태.
 *
 * 예전엔 화면 안에서만 도는 목업 배열이었지만 이제 서버가 진짜 소유자다.
 * 로그인 시 목록을 한 번 불러와 캐시해 두고, 토글할 때마다 서버에 반영한다.
 */

interface WishlistContextType {
  items: WishlistItem[];
  isWished: (productId: number) => boolean;
  toggleWish: (productId: number) => Promise<void>;
  reload: () => Promise<void>;
  loading: boolean;
}

const WishlistContext = createContext<WishlistContextType | undefined>(undefined);

export function WishlistProvider({ children }: { children: ReactNode }) {
  const { isLoggedIn } = useAuth();
  const [items, setItems] = useState<WishlistItem[]>([]);
  const [loading, setLoading] = useState(false);

  const reload = useCallback(async () => {
    if (!isLoggedIn) {
      setItems([]);
      return;
    }
    setLoading(true);
    try {
      setItems(await wishlistApi.getMine());
    } catch {
      // 찜 목록을 못 불러온다고 페이지 전체를 막을 필요는 없다
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [isLoggedIn]);

  useEffect(() => {
    void reload();
  }, [reload]);

  const isWished = (productId: number) => items.some((item) => item.productId === productId);

  const toggleWish = async (productId: number) => {
    if (!isLoggedIn) {
      alert("로그인이 필요합니다.");
      return;
    }

    const wished = isWished(productId);

    // 낙관적 업데이트: 서버 응답을 기다리지 않고 하트부터 바꾼다.
    // 실패하면 아래 catch에서 되돌린다.
    setItems((prev) =>
      wished
        ? prev.filter((item) => item.productId !== productId)
        : [...prev, { productId, title: "", price: 0, priceChange: 0, status: "ON_SALE", thumbnail: null }]
    );

    try {
      if (wished) {
        await wishlistApi.remove(productId);
      } else {
        await wishlistApi.add(productId);
      }
      await reload(); // 서버 기준으로 다시 맞춘다 (가격 변동 등)
    } catch (error) {
      await reload(); // 실패 시 원래 상태로 복구
      alert(error instanceof Error ? error.message : "찜 처리에 실패했습니다.");
    }
  };

  return (
    <WishlistContext.Provider value={{ items, isWished, toggleWish, reload, loading }}>
      {children}
    </WishlistContext.Provider>
  );
}

export function useWishlist() {
  const context = useContext(WishlistContext);
  if (!context) {
    throw new Error("useWishlist는 WishlistProvider 안에서만 사용할 수 있습니다.");
  }
  return context;
}
