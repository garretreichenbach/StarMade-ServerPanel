import { describe, it, expect, beforeAll } from 'vitest';
import { encryptSecret, decryptSecret, secretsConfigured } from './secrets';

describe('secret encryption (AES-256-GCM)', () => {
  beforeAll(() => {
    process.env.SSP_SECRET_KEY = 'test-secret-key-for-vitest';
  });

  it('reports configured when a key is set', () => {
    expect(secretsConfigured()).toBe(true);
  });

  it('round-trips a secret (e.g. an SSH key passphrase)', () => {
    const passphrase = 'correct horse battery staple 🐴';
    const blob = encryptSecret(passphrase);
    expect(Buffer.isBuffer(blob)).toBe(true);
    expect(decryptSecret(blob)).toBe(passphrase);
  });

  it('produces different ciphertext each time (random IV)', () => {
    const a = encryptSecret('same');
    const b = encryptSecret('same');
    expect(Buffer.compare(a, b)).not.toBe(0);
    expect(decryptSecret(a)).toBe('same');
    expect(decryptSecret(b)).toBe('same');
  });

  it('rejects tampered ciphertext (GCM auth tag)', () => {
    const blob = encryptSecret('secret');
    const last = blob.length - 1;
    blob[last] = (blob[last] ?? 0) ^ 0xff; // flip a ciphertext byte
    expect(() => decryptSecret(blob)).toThrow();
  });

  it('rejects a malformed blob', () => {
    expect(() => decryptSecret(Buffer.from([0, 1, 2]))).toThrow('Malformed');
  });
});
