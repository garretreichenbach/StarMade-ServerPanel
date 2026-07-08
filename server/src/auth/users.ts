import { randomUUID } from 'node:crypto';
import type { PanelUser, Role } from '@ssp/shared';
import { getDb } from '../db/index';

interface UserRow {
  id: string;
  username: string;
  email: string | null;
  password_hash: string | null;
  role: string;
  disabled: number;
  created_at: string;
  last_login_at: string | null;
}

/** Map a DB row to the public-safe user shape (never exposes password_hash). */
export function toPanelUser(row: UserRow): PanelUser {
  return {
    id: row.id,
    username: row.username,
    email: row.email,
    role: row.role as Role,
    disabled: row.disabled === 1,
    createdAt: row.created_at,
    lastLoginAt: row.last_login_at,
  };
}

export function getUserRowById(id: string): UserRow | undefined {
  return getDb().prepare('SELECT * FROM users WHERE id = ?').get(id) as UserRow | undefined;
}

export function getUserRowByUsername(username: string): UserRow | undefined {
  return getDb().prepare('SELECT * FROM users WHERE username = ?').get(username) as
    | UserRow
    | undefined;
}

export function getUserById(id: string): PanelUser | null {
  const row = getUserRowById(id);
  return row ? toPanelUser(row) : null;
}

export function listUsers(): PanelUser[] {
  const rows = getDb()
    .prepare('SELECT * FROM users ORDER BY username COLLATE NOCASE')
    .all() as unknown as UserRow[];
  return rows.map(toPanelUser);
}

export function countUsers(): number {
  const row = getDb().prepare('SELECT COUNT(*) AS n FROM users').get() as { n: number };
  return row.n;
}

/** Count enabled admins — used to prevent locking out the last one. */
export function countActiveAdmins(): number {
  const row = getDb()
    .prepare("SELECT COUNT(*) AS n FROM users WHERE role = 'admin' AND disabled = 0")
    .get() as { n: number };
  return row.n;
}

export interface CreateUserInput {
  username: string;
  email?: string | null;
  passwordHash?: string | null;
  role: Role;
}

export function createUser(input: CreateUserInput): PanelUser {
  const id = randomUUID();
  const now = new Date().toISOString();
  getDb()
    .prepare(
      'INSERT INTO users (id, username, email, password_hash, role, disabled, created_at) VALUES (?, ?, ?, ?, ?, 0, ?)',
    )
    .run(id, input.username, input.email ?? null, input.passwordHash ?? null, input.role, now);
  return getUserById(id)!;
}

export function setPasswordHash(id: string, passwordHash: string | null): void {
  getDb().prepare('UPDATE users SET password_hash = ? WHERE id = ?').run(passwordHash, id);
}

export function setRole(id: string, role: Role): void {
  getDb().prepare('UPDATE users SET role = ? WHERE id = ?').run(role, id);
}

export function setDisabled(id: string, disabled: boolean): void {
  getDb().prepare('UPDATE users SET disabled = ? WHERE id = ?').run(disabled ? 1 : 0, id);
}

export function setEmail(id: string, email: string | null): void {
  getDb().prepare('UPDATE users SET email = ? WHERE id = ?').run(email, id);
}

export function touchLastLogin(id: string): void {
  getDb().prepare('UPDATE users SET last_login_at = ? WHERE id = ?').run(new Date().toISOString(), id);
}

export function deleteUser(id: string): void {
  getDb().prepare('DELETE FROM users WHERE id = ?').run(id);
}
