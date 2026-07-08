import { spawn } from 'node:child_process';
import { mkdtemp, writeFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';

/**
 * SSH public-key login uses the standard SSHSIG flow so the private key never
 * leaves the user's machine:
 *
 *   printf '%s' '<challenge>' | ssh-keygen -Y sign -n starmade-panel -f ~/.ssh/id_ed25519
 *
 * The user pastes the armored signature; we verify it against their registered
 * public keys with `ssh-keygen -Y verify`.
 */
export const SSHSIG_NAMESPACE = 'starmade-panel';

const SIGNER_PRINCIPAL = 'panel-user';

export async function verifySshSignature(opts: {
  challenge: string;
  signatureArmored: string;
  /** Registered public keys for the user, in OpenSSH single-line form. */
  publicKeys: string[];
}): Promise<boolean> {
  if (opts.publicKeys.length === 0) return false;
  if (!opts.signatureArmored.includes('BEGIN SSH SIGNATURE')) return false;

  const dir = await mkdtemp(path.join(tmpdir(), 'ssp-sshsig-'));
  try {
    const allowed =
      opts.publicKeys
        .map((k) => `${SIGNER_PRINCIPAL} namespaces="${SSHSIG_NAMESPACE}" ${k.trim()}`)
        .join('\n') + '\n';
    const allowedPath = path.join(dir, 'allowed_signers');
    const sigPath = path.join(dir, 'sig');
    await writeFile(allowedPath, allowed, { mode: 0o600 });
    await writeFile(sigPath, opts.signatureArmored.trim() + '\n', { mode: 0o600 });

    return await new Promise<boolean>((resolve) => {
      const child = spawn('ssh-keygen', [
        '-Y',
        'verify',
        '-f',
        allowedPath,
        '-I',
        SIGNER_PRINCIPAL,
        '-n',
        SSHSIG_NAMESPACE,
        '-s',
        sigPath,
      ]);
      let settled = false;
      const done = (v: boolean) => {
        if (!settled) {
          settled = true;
          resolve(v);
        }
      };
      child.on('error', () => done(false));
      child.on('close', (code) => done(code === 0));
      child.stdin.on('error', () => {
        /* ignore EPIPE if ssh-keygen exits early */
      });
      // The signed payload is the exact challenge bytes (no trailing newline),
      // matching `printf '%s'` on the signing side.
      child.stdin.write(opts.challenge);
      child.stdin.end();
    });
  } finally {
    await rm(dir, { recursive: true, force: true });
  }
}
