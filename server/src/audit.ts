import { getDb } from './db/index';

/** Append an entry to the audit log. Best-effort; never throws to the caller. */
export function audit(entry: {
  userId?: string | null;
  action: string;
  target?: string | null;
  detail?: string | null;
  ip?: string | null;
}): void {
  try {
    getDb()
      .prepare(
        'INSERT INTO audit_log (user_id, action, target, detail, ip, created_at) VALUES (?, ?, ?, ?, ?, ?)',
      )
      .run(
        entry.userId ?? null,
        entry.action,
        entry.target ?? null,
        entry.detail ?? null,
        entry.ip ?? null,
        new Date().toISOString(),
      );
  } catch {
    /* auditing must not break the request path */
  }
}
