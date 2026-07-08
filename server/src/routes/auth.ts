import type { FastifyInstance, FastifyReply } from 'fastify';
import { z } from 'zod';
import { config } from '../config';
import { audit } from '../audit';
import { requireAuth } from '../auth/middleware';
import { SESSION_COOKIE, createSession, destroySession, destroyUserSessions } from '../auth/sessions';
import { verifyPassword, hashPassword } from '../auth/passwords';
import {
  getUserRowByUsername,
  getUserRowById,
  toPanelUser,
  touchLastLogin,
  setPasswordHash,
} from '../auth/users';
import {
  listUserSshKeys,
  addUserSshKey,
  deleteUserSshKey,
  getUserSshKeyMaterial,
} from '../auth/sshKeys';
import { issueChallenge, consumeChallenge } from '../auth/challenges';
import { verifySshSignature, SSHSIG_NAMESPACE } from '../auth/sshVerify';

const SESSION_MAX_AGE_SEC = 7 * 24 * 60 * 60;

function setSessionCookie(reply: FastifyReply, token: string): void {
  reply.setCookie(SESSION_COOKIE, token, {
    httpOnly: true,
    sameSite: 'strict',
    secure: config.cookieSecure,
    path: '/',
    maxAge: SESSION_MAX_AGE_SEC,
  });
}

export async function authRoutes(app: FastifyInstance): Promise<void> {
  // ── Password login ────────────────────────────────────────────────────────
  app.post('/api/auth/login', async (request, reply) => {
    const body = z
      .object({ username: z.string().min(1), password: z.string().min(1) })
      .safeParse(request.body);
    if (!body.success) return reply.code(400).send({ ok: false, error: 'Invalid request' });

    const row = getUserRowByUsername(body.data.username);
    const genericError = { ok: false, error: 'Invalid username or password' } as const;
    // Always run a hash comparison to keep timing roughly constant.
    const hash = row?.password_hash ?? 'scrypt$16384$8$1$AAAAAAAAAAAAAAAAAAAAAA==$AAAA';
    const ok = await verifyPassword(body.data.password, hash);
    if (!row || row.disabled === 1 || !row.password_hash || !ok) {
      audit({ action: 'auth.login.fail', target: body.data.username, ip: request.ip });
      return reply.code(401).send(genericError);
    }

    const token = createSession(row.id, { ip: request.ip, userAgent: request.headers['user-agent'] });
    setSessionCookie(reply, token);
    touchLastLogin(row.id);
    audit({ userId: row.id, action: 'auth.login', ip: request.ip });
    return { ok: true, user: toPanelUser(row) };
  });

  // ── Logout ────────────────────────────────────────────────────────────────
  app.post('/api/auth/logout', async (request, reply) => {
    destroySession(request.sessionToken);
    reply.clearCookie(SESSION_COOKIE, { path: '/' });
    if (request.user) audit({ userId: request.user.id, action: 'auth.logout', ip: request.ip });
    return { ok: true };
  });

  // ── Current user ────────────────────────────────────────────────────────────
  app.get('/api/auth/me', async (request, reply) => {
    if (!request.user) return reply.code(401).send({ ok: false, error: 'Not authenticated' });
    return { ok: true, user: request.user };
  });

  // ── Change own password ─────────────────────────────────────────────────────
  app.post('/api/auth/change-password', { preHandler: requireAuth }, async (request, reply) => {
    const body = z
      .object({ currentPassword: z.string().min(1), newPassword: z.string().min(8) })
      .safeParse(request.body);
    if (!body.success) {
      return reply.code(400).send({ ok: false, error: 'New password must be at least 8 characters' });
    }
    const row = getUserRowById(request.user!.id)!;
    const ok = row.password_hash ? await verifyPassword(body.data.currentPassword, row.password_hash) : false;
    if (!ok) return reply.code(400).send({ ok: false, error: 'Current password is incorrect' });

    setPasswordHash(row.id, await hashPassword(body.data.newPassword));
    // Invalidate other sessions, keep the current one alive.
    destroyUserSessions(row.id);
    const token = createSession(row.id, { ip: request.ip, userAgent: request.headers['user-agent'] });
    setSessionCookie(reply, token);
    audit({ userId: row.id, action: 'auth.change-password', ip: request.ip });
    return { ok: true };
  });

  // ── Own SSH keys ─────────────────────────────────────────────────────────────
  app.get('/api/auth/ssh-keys', { preHandler: requireAuth }, async (request) => {
    return { ok: true, keys: listUserSshKeys(request.user!.id) };
  });

  app.post('/api/auth/ssh-keys', { preHandler: requireAuth }, async (request, reply) => {
    const body = z
      .object({ name: z.string().min(1).max(100), publicKey: z.string().min(1) })
      .safeParse(request.body);
    if (!body.success) return reply.code(400).send({ ok: false, error: 'Invalid request' });
    try {
      const key = addUserSshKey(request.user!.id, body.data.name, body.data.publicKey);
      audit({ userId: request.user!.id, action: 'auth.ssh-key.add', target: key.fingerprint, ip: request.ip });
      return { ok: true, key };
    } catch (err) {
      return reply.code(400).send({ ok: false, error: err instanceof Error ? err.message : 'Invalid key' });
    }
  });

  app.delete('/api/auth/ssh-keys/:id', { preHandler: requireAuth }, async (request, reply) => {
    const { id } = request.params as { id: string };
    const removed = deleteUserSshKey(request.user!.id, id);
    if (!removed) return reply.code(404).send({ ok: false, error: 'Key not found' });
    audit({ userId: request.user!.id, action: 'auth.ssh-key.remove', target: id, ip: request.ip });
    return { ok: true };
  });

  // ── SSH-key login: challenge / verify ───────────────────────────────────────
  app.post('/api/auth/ssh/challenge', async (request, reply) => {
    const body = z.object({ username: z.string().min(1) }).safeParse(request.body);
    if (!body.success) return reply.code(400).send({ ok: false, error: 'Invalid request' });
    // Always issue a challenge (don't reveal whether the user exists).
    const issued = issueChallenge(body.data.username);
    return {
      ok: true,
      challengeId: issued.challengeId,
      challenge: issued.challenge,
      namespace: SSHSIG_NAMESPACE,
      // Convenience hint for the UI to render a copyable command.
      signCommand: `printf '%s' '${issued.challenge}' | ssh-keygen -Y sign -n ${SSHSIG_NAMESPACE} -f ~/.ssh/id_ed25519`,
    };
  });

  app.post('/api/auth/ssh/verify', async (request, reply) => {
    const body = z
      .object({ challengeId: z.string().min(1), signature: z.string().min(1) })
      .safeParse(request.body);
    if (!body.success) return reply.code(400).send({ ok: false, error: 'Invalid request' });

    const consumed = consumeChallenge(body.data.challengeId);
    const genericError = { ok: false, error: 'Signature verification failed' } as const;
    if (!consumed) return reply.code(401).send(genericError);

    const row = getUserRowByUsername(consumed.username);
    if (!row || row.disabled === 1) {
      audit({ action: 'auth.ssh-login.fail', target: consumed.username, ip: request.ip });
      return reply.code(401).send(genericError);
    }

    const publicKeys = getUserSshKeyMaterial(row.id).map((k) => k.openssh);
    const valid = await verifySshSignature({
      challenge: consumed.challenge,
      signatureArmored: body.data.signature,
      publicKeys,
    });
    if (!valid) {
      audit({ userId: row.id, action: 'auth.ssh-login.fail', ip: request.ip });
      return reply.code(401).send(genericError);
    }

    const token = createSession(row.id, { ip: request.ip, userAgent: request.headers['user-agent'] });
    setSessionCookie(reply, token);
    touchLastLogin(row.id);
    audit({ userId: row.id, action: 'auth.ssh-login', ip: request.ip });
    return { ok: true, user: toPanelUser(row) };
  });
}
