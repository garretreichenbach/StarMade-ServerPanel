import { createCipheriv, createDecipheriv, randomBytes, createHash } from 'node:crypto';

/**
 * Encryption-at-rest for stored secrets — remote-server passwords, SSH private
 * keys, and the passphrases that unlock them. AES-256-GCM (authenticated) with a
 * key derived from SSP_SECRET_KEY.
 *
 * Blob layout: [1-byte version=1][12-byte IV][16-byte GCM tag][ciphertext].
 * Stored as a SQLite BLOB (`server_credentials.secret_encrypted`) and only ever
 * decrypted in-process at the moment of use — never sent to the browser.
 */

const VERSION = 1;
const IV_LEN = 12;
const TAG_LEN = 16;

/**
 * The configured secret, read at call time so it picks up whatever dotenv/env
 * has populated (and so tests can set it before use).
 */
function rawSecret(): string | undefined {
  return process.env.SSP_SECRET_KEY;
}

/** Whether a secret key is configured (credential storage is available). */
export function secretsConfigured(): boolean {
  return !!rawSecret();
}

function getKey(): Buffer {
  const secret = rawSecret();
  if (!secret) {
    throw new Error(
      'SSP_SECRET_KEY is not set — cannot encrypt/decrypt stored credentials. ' +
        'Generate one with `openssl rand -base64 32` and set it in the environment.',
    );
  }
  // SHA-256 normalizes any provided secret (base64, hex, or passphrase) to a
  // 32-byte key.
  return createHash('sha256').update(secret, 'utf8').digest();
}

export function encryptSecret(plaintext: string): Buffer {
  const iv = randomBytes(IV_LEN);
  const cipher = createCipheriv('aes-256-gcm', getKey(), iv);
  const enc = Buffer.concat([cipher.update(plaintext, 'utf8'), cipher.final()]);
  const tag = cipher.getAuthTag();
  return Buffer.concat([Buffer.from([VERSION]), iv, tag, enc]);
}

export function decryptSecret(blob: Buffer | Uint8Array): string {
  const buf = Buffer.isBuffer(blob) ? blob : Buffer.from(blob);
  if (buf.length < 1 + IV_LEN + TAG_LEN || buf[0] !== VERSION) {
    throw new Error('Malformed encrypted secret');
  }
  const iv = buf.subarray(1, 1 + IV_LEN);
  const tag = buf.subarray(1 + IV_LEN, 1 + IV_LEN + TAG_LEN);
  const data = buf.subarray(1 + IV_LEN + TAG_LEN);
  const decipher = createDecipheriv('aes-256-gcm', getKey(), iv);
  decipher.setAuthTag(tag);
  return Buffer.concat([decipher.update(data), decipher.final()]).toString('utf8');
}
