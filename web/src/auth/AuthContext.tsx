import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { PanelUser } from '@ssp/shared';
import { authApi } from '../api/auth';
import { ApiError } from '../api/client';

interface AuthState {
  user: PanelUser | null;
  loading: boolean;
  refresh: () => Promise<void>;
  setUser: (user: PanelUser) => void;
  logout: () => Promise<void>;
}

const AuthCtx = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUserState] = useState<PanelUser | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      const res = await authApi.me();
      setUserState(res.user);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) setUserState(null);
      else setUserState(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } finally {
      setUserState(null);
    }
  }, []);

  const value = useMemo<AuthState>(
    () => ({ user, loading, refresh, setUser: setUserState, logout }),
    [user, loading, refresh, logout],
  );

  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthCtx);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
