import type { FastifyInstance } from 'fastify';
import { z } from 'zod';
import { ROLES } from '@ssp/shared';
import { audit } from '../audit';
import { requireRole } from '../auth/middleware';
import { hashPassword } from '../auth/passwords';
import { destroyUserSessions } from '../auth/sessions';
import {
  listUsers,
  getUserById,
  getUserRowByUsername,
  createUser,
  setRole,
  setDisabled,
  setEmail,
  setPasswordHash,
  deleteUser,
  countActiveAdmins,
} from '../auth/users';
import { listUserSshKeys, addUserSshKey, deleteUserSshKey } from '../auth/sshKeys';

const roleSchema = z.enum(ROLES as unknown as [string, ...string[]]);

export async function userAdminRoutes(app: FastifyInstance): Promise<void> {
  const adminOnly = { preHandler: requireRole('admin') };

  app.get('/api/users', adminOnly, async () => ({ ok: true, users: listUsers() }));

  app.post('/api/users', adminOnly, async (request, reply) => {
    const body = z
      .object({
        username: z
          .string()
          .min(1)
          .max(64)
          .regex(/^[A-Za-z0-9._-]+$/, 'Letters, digits, dot, underscore, hyphen only'),
        email: z.string().email().optional().nullable(),
        role: roleSchema,
        password: z.string().min(8).optional(),
      })
      .safeParse(request.body);
    if (!body.success) {
      return reply.code(400).send({ ok: false, error: body.error.issues[0]?.message ?? 'Invalid request' });
    }
    if (getUserRowByUsername(body.data.username)) {
      return reply.code(409).send({ ok: false, error: 'Username already exists' });
    }
    const passwordHash = body.data.password ? await hashPassword(body.data.password) : null;
    const user = createUser({
      username: body.data.username,
      email: body.data.email ?? null,
      role: body.data.role as (typeof ROLES)[number],
      passwordHash,
    });
    audit({ userId: request.user!.id, action: 'user.create', target: user.username, ip: request.ip });
    return { ok: true, user };
  });

  app.patch('/api/users/:id', adminOnly, async (request, reply) => {
    const { id } = request.params as { id: string };
    const target = getUserById(id);
    if (!target) return reply.code(404).send({ ok: false, error: 'User not found' });

    const body = z
      .object({
        role: roleSchema.optional(),
        disabled: z.boolean().optional(),
        email: z.string().email().nullable().optional(),
      })
      .safeParse(request.body);
    if (!body.success) return reply.code(400).send({ ok: false, error: 'Invalid request' });

    // Guard against removing the last active admin (by demotion or disabling).
    const losingAdmin =
      (body.data.role !== undefined && target.role === 'admin' && body.data.role !== 'admin') ||
      (body.data.disabled === true && target.role === 'admin');
    if (losingAdmin && countActiveAdmins() <= 1) {
      return reply.code(400).send({ ok: false, error: 'Cannot remove the last active admin' });
    }

    if (body.data.role !== undefined) setRole(id, body.data.role as (typeof ROLES)[number]);
    if (body.data.disabled !== undefined) {
      setDisabled(id, body.data.disabled);
      if (body.data.disabled) destroyUserSessions(id);
    }
    if (body.data.email !== undefined) setEmail(id, body.data.email);
    audit({ userId: request.user!.id, action: 'user.update', target: target.username, ip: request.ip });
    return { ok: true, user: getUserById(id) };
  });

  app.post('/api/users/:id/reset-password', adminOnly, async (request, reply) => {
    const { id } = request.params as { id: string };
    if (!getUserById(id)) return reply.code(404).send({ ok: false, error: 'User not found' });
    const body = z.object({ newPassword: z.string().min(8) }).safeParse(request.body);
    if (!body.success) {
      return reply.code(400).send({ ok: false, error: 'Password must be at least 8 characters' });
    }
    setPasswordHash(id, await hashPassword(body.data.newPassword));
    destroyUserSessions(id);
    audit({ userId: request.user!.id, action: 'user.reset-password', target: id, ip: request.ip });
    return { ok: true };
  });

  app.delete('/api/users/:id', adminOnly, async (request, reply) => {
    const { id } = request.params as { id: string };
    const target = getUserById(id);
    if (!target) return reply.code(404).send({ ok: false, error: 'User not found' });
    if (id === request.user!.id) {
      return reply.code(400).send({ ok: false, error: 'You cannot delete your own account' });
    }
    if (target.role === 'admin' && countActiveAdmins() <= 1) {
      return reply.code(400).send({ ok: false, error: 'Cannot delete the last active admin' });
    }
    deleteUser(id);
    audit({ userId: request.user!.id, action: 'user.delete', target: target.username, ip: request.ip });
    return { ok: true };
  });

  // ── Manage another user's SSH keys ──────────────────────────────────────────
  app.get('/api/users/:id/ssh-keys', adminOnly, async (request, reply) => {
    const { id } = request.params as { id: string };
    if (!getUserById(id)) return reply.code(404).send({ ok: false, error: 'User not found' });
    return { ok: true, keys: listUserSshKeys(id) };
  });

  app.post('/api/users/:id/ssh-keys', adminOnly, async (request, reply) => {
    const { id } = request.params as { id: string };
    if (!getUserById(id)) return reply.code(404).send({ ok: false, error: 'User not found' });
    const body = z
      .object({ name: z.string().min(1).max(100), publicKey: z.string().min(1) })
      .safeParse(request.body);
    if (!body.success) return reply.code(400).send({ ok: false, error: 'Invalid request' });
    try {
      const key = addUserSshKey(id, body.data.name, body.data.publicKey);
      audit({ userId: request.user!.id, action: 'user.ssh-key.add', target: `${id}:${key.fingerprint}`, ip: request.ip });
      return { ok: true, key };
    } catch (err) {
      return reply.code(400).send({ ok: false, error: err instanceof Error ? err.message : 'Invalid key' });
    }
  });

  app.delete('/api/users/:id/ssh-keys/:keyId', adminOnly, async (request, reply) => {
    const { id, keyId } = request.params as { id: string; keyId: string };
    const removed = deleteUserSshKey(id, keyId);
    if (!removed) return reply.code(404).send({ ok: false, error: 'Key not found' });
    audit({ userId: request.user!.id, action: 'user.ssh-key.remove', target: `${id}:${keyId}`, ip: request.ip });
    return { ok: true };
  });
}
