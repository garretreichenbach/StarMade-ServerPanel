import { randomBytes, createHash } from 'node:crypto';
import { getDb } from '../db/index';

// Opaque session tokens: a random token goes in the cookie; only its SHA-256 is
// stored, so a leak of the DB does not reveal usable session cookies.

export const SESSION_COOKIE = 'ssp_session';
const SESSION_TTL_MS = 7 * 24 * 60 * 60 * 1000; // 7 days

function hashToken(token: string): string {
  return createHash('sha256').update(token).digest('hex');
}

export interface SessionContext {
  ip?: string | undefined;
  userAgent?: string | undefined;
}

/** Create a session for a user and return the opaque cookie token. */
export function createSession(userId: string, ctx: SessionContext = {}): string {
  const token = randomBytes(32).toString('base64url');
  const now = Date.now();
  getDb()
    .prepare(
      'INSERT INTO sessions (id, user_id, created_at, expires_at, ip, user_agent) VALUES (?, ?, ?, ?, ?, ?)',
    )
    .run(
      hashToken(token),
      userId,
      new Date(now).toISOString(),
      new Date(now + SESSION_TTL_MS).toISOString(),
      ctx.ip ?? null,
      ctx.userAgent ?? null,
    );
  return token;
}

/** Resolve a cookie token to a live user id, or null if missing/expired. */
export function resolveSession(token: string | undefined): string | null {
  if (!token) return null;
  const row = getDb()
    .prepare('SELECT user_id, expires_at FROM sessions WHERE id = ?')
    .get(hashToken(token)) as { user_id: string; expires_at: string } | undefined;
  if (!row) return null;
  if (new Date(row.expires_at).getTime() < Date.now()) {
    destroySession(token);
    return null;
  }
  return row.user_id;
}

export function destroySession(token: string | undefined): void {
  if (!token) return;
  getDb().prepare('DELETE FROM sessions WHERE id = ?').run(hashToken(token));
}

/** Remove all sessions for a user (e.g. on disable / password reset). */
export function destroyUserSessions(userId: string): void {
  getDb().prepare('DELETE FROM sessions WHERE user_id = ?').run(userId);
}

/** Best-effort purge of expired sessions. */
export function purgeExpiredSessions(): void {
  getDb().prepare('DELETE FROM sessions WHERE expires_at < ?').run(new Date().toISOString());
}
