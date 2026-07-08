import { randomBytes, randomUUID } from 'node:crypto';

// Short-lived, single-use SSH login challenges kept in memory. A restart clears
// them (acceptable — clients just request a fresh one).

interface Challenge {
  username: string;
  challenge: string;
  expiresAt: number;
}

const CHALLENGE_TTL_MS = 2 * 60 * 1000;
const store = new Map<string, Challenge>();

export interface IssuedChallenge {
  challengeId: string;
  challenge: string;
}

export function issueChallenge(username: string): IssuedChallenge {
  purge();
  const challengeId = randomUUID();
  const challenge = randomBytes(32).toString('base64url');
  store.set(challengeId, { username, challenge, expiresAt: Date.now() + CHALLENGE_TTL_MS });
  return { challengeId, challenge };
}

/** Consume a challenge (single use). Returns it if valid & unexpired. */
export function consumeChallenge(challengeId: string): { username: string; challenge: string } | null {
  const entry = store.get(challengeId);
  if (!entry) return null;
  store.delete(challengeId);
  if (entry.expiresAt < Date.now()) return null;
  return { username: entry.username, challenge: entry.challenge };
}

function purge(): void {
  const now = Date.now();
  for (const [id, entry] of store) {
    if (entry.expiresAt < now) store.delete(id);
  }
}
