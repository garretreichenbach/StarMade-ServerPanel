import { useEffect, useState } from 'react';
import { CircleCheck, CircleAlert } from 'lucide-react';
import { api, type HealthResponse } from '../api/client';
import { Card, Spinner } from '../components/ui';
import { useAuth } from '../auth/AuthContext';

export function Dashboard() {
  const { user } = useAuth();
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .get<HealthResponse>('/api/health')
      .then(setHealth)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : String(err)));
  }, []);

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <div>
        <h2 className="text-xl font-semibold">Dashboard</h2>
        <p className="mt-1 text-sm text-white/50">Signed in as {user?.username}.</p>
      </div>

      <Card>
        <h3 className="mb-4 text-sm font-medium uppercase tracking-wide text-white/50">Backend status</h3>
        {error && (
          <div className="flex items-center gap-2 text-red-400">
            <CircleAlert className="h-5 w-5" /> {error}
          </div>
        )}
        {!health && !error && <Spinner label="Loading…" />}
        {health && (
          <dl className="grid grid-cols-2 gap-y-2 text-sm">
            <dt className="text-white/50">Service</dt>
            <dd className="text-right font-mono">{health.name}</dd>
            <dt className="text-white/50">Version</dt>
            <dd className="text-right font-mono">{health.version}</dd>
            <dt className="text-white/50">Database</dt>
            <dd className="flex items-center justify-end gap-1 text-right">
              {health.db === 'ok' ? (
                <CircleCheck className="h-4 w-4 text-emerald-400" />
              ) : (
                <CircleAlert className="h-4 w-4 text-red-400" />
              )}
              <span className="font-mono">{health.db}</span>
            </dd>
            <dt className="text-white/50">Uptime</dt>
            <dd className="text-right font-mono">{health.uptimeSec}s</dd>
          </dl>
        )}
      </Card>

      <Card>
        <p className="text-sm text-white/50">
          Server controls, configuration, logs, and the world database arrive in the next
          milestones. This is the account &amp; auth layer.
        </p>
      </Card>
    </div>
  );
}
