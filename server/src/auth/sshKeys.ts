import { randomUUID } from 'node:crypto';
import sshpk from 'sshpk';
import type { PanelSshKey } from '@ssp/shared';
import { getDb } from '../db/index';

interface SshKeyRow {
  id: string;
  user_id: string;
  name: string;
  public_key: string;
  fingerprint: string;
  created_at: string;
}

function toPanelSshKey(row: SshKeyRow): PanelSshKey {
  return {
    id: row.id,
    userId: row.user_id,
    name: row.name,
    fingerprint: row.fingerprint,
    createdAt: row.created_at,
  };
}

export interface ParsedPublicKey {
  /** Canonical OpenSSH single-line form. */
  openssh: string;
  /** SHA-256 fingerprint, e.g. `SHA256:abc…`. */
  fingerprint: string;
}

/** Parse & normalize an OpenSSH public key, or throw if malformed. */
export function parsePublicKey(input: string): ParsedPublicKey {
  const key = sshpk.parseKey(input.trim(), 'ssh');
  return {
    openssh: key.toString('ssh').trim(),
    fingerprint: key.fingerprint('sha256').toString(),
  };
}

export function listUserSshKeys(userId: string): PanelSshKey[] {
  const rows = getDb()
    .prepare('SELECT * FROM user_ssh_keys WHERE user_id = ? ORDER BY created_at')
    .all(userId) as unknown as SshKeyRow[];
  return rows.map(toPanelSshKey);
}

/** Full rows (incl. public_key) for a user — used by the verifier. */
export function getUserSshKeyMaterial(userId: string): Array<{ openssh: string }> {
  const rows = getDb()
    .prepare('SELECT public_key FROM user_ssh_keys WHERE user_id = ?')
    .all(userId) as Array<{ public_key: string }>;
  return rows.map((r) => ({ openssh: r.public_key }));
}

export function addUserSshKey(userId: string, name: string, publicKeyInput: string): PanelSshKey {
  const parsed = parsePublicKey(publicKeyInput);
  const existing = getDb()
    .prepare('SELECT id FROM user_ssh_keys WHERE fingerprint = ?')
    .get(parsed.fingerprint);
  if (existing) {
    throw new Error('This SSH key is already registered.');
  }
  const id = randomUUID();
  getDb()
    .prepare(
      'INSERT INTO user_ssh_keys (id, user_id, name, public_key, fingerprint, created_at) VALUES (?, ?, ?, ?, ?, ?)',
    )
    .run(id, userId, name, parsed.openssh, parsed.fingerprint, new Date().toISOString());
  const row = getDb().prepare('SELECT * FROM user_ssh_keys WHERE id = ?').get(id) as unknown as SshKeyRow;
  return toPanelSshKey(row);
}

export function deleteUserSshKey(userId: string, keyId: string): boolean {
  const res = getDb()
    .prepare('DELETE FROM user_ssh_keys WHERE id = ? AND user_id = ?')
    .run(keyId, userId);
  return res.changes > 0;
}
