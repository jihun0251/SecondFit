import { createContext, useContext, useState } from "react";
import type { ReactNode } from "react";

interface LikesContextType {
  likedIds: number[];
  toggleLike: (id: number) => void;
  isLiked: (id: number) => boolean;
}

const LikesContext = createContext<LikesContextType | undefined>(undefined);

// 초기 찜 목록 (기본으로 몇 개 찜해둔 상태)
const initialLikes = [1042, 1043, 1048];

export function LikesProvider({ children }: { children: ReactNode }) {
  const [likedIds, setLikedIds] = useState<number[]>(initialLikes);

  const toggleLike = (id: number) => {
    setLikedIds((prev) =>
      prev.includes(id) ? prev.filter((v) => v !== id) : [...prev, id]
    );
  };

  const isLiked = (id: number) => likedIds.includes(id);

  return (
    <LikesContext.Provider value={{ likedIds, toggleLike, isLiked }}>
      {children}
    </LikesContext.Provider>
  );
}

// 각 페이지에서 쓸 훅
export function useLikes() {
  const context = useContext(LikesContext);
  if (!context) {
    throw new Error("useLikes는 LikesProvider 안에서만 사용할 수 있습니다.");
  }
  return context;
}