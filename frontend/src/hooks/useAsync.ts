import { useCallback, useEffect, useState } from "react";
import { ApiError } from "../api/client";

/**
 * "불러오는 중 → 성공 / 실패" 3단 상태를 다루는 훅.
 *
 * 페이지마다 useState 3개(data, loading, error)와 useEffect를 반복해서 쓰게 되는데,
 * 그 반복을 여기 한 번만 적어둔다.
 *
 * 사용 예:
 *   const { data, loading, error, reload } = useAsync(() => productApi.getDetail(id), [id]);
 */
export function useAsync<T>(fetcher: () => Promise<T>, deps: unknown[] = []) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const memoizedFetcher = useCallback(fetcher, deps);

  const run = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setData(await memoizedFetcher());
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "데이터를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, [memoizedFetcher]);

  useEffect(() => {
    void run();
  }, [run]);

  return { data, loading, error, reload: run };
}

/** 버튼 클릭처럼 사용자가 직접 일으키는 동작용 (에러를 alert로 띄우고 중복 클릭을 막는다) */
export function useAction() {
  const [running, setRunning] = useState(false);

  const run = async (action: () => Promise<void>, successMessage?: string) => {
    if (running) return false;
    setRunning(true);
    try {
      await action();
      if (successMessage) alert(successMessage);
      return true;
    } catch (e) {
      alert(e instanceof ApiError ? e.message : "요청 처리에 실패했습니다.");
      return false;
    } finally {
      setRunning(false);
    }
  };

  return { running, run };
}
