import type { FastifyInstance, FastifyReply, FastifyRequest } from 'fastify';
import type { PanelUser, Role } from '@ssp/shared';
import { SESSION_COOKIE, resolveSession } from './sessions';
import { getUserRowById, toPanelUser } from './users';

declare module 'fastify' {
  interface FastifyRequest {
    /** The authenticated panel user for this request, or null if anonymous. */
    user: PanelUser | null;
    /** The raw session cookie token, if present. */
    sessionToken: string | undefined;
  }
}

const ROLE_RANK: Record<Role, number> = { viewer: 0, operator: 1, admin: 2 };

/** Populate `request.user` from the session cookie on every request. */
export function registerAuthContext(app: FastifyInstance): void {
  app.decorateRequest('user', null);
  app.decorateRequest('sessionToken', undefined);

  app.addHook('onRequest', async (request) => {
    const token = request.cookies?.[SESSION_COOKIE];
    request.sessionToken = token;
    const userId = resolveSession(token);
    if (!userId) return;
    const row = getUserRowById(userId);
    if (!row || row.disabled === 1) return;
    request.user = toPanelUser(row);
  });
}

/** preHandler: reject anonymous requests. */
export async function requireAuth(request: FastifyRequest, reply: FastifyReply): Promise<void> {
  if (!request.user) {
    await reply.code(401).send({ ok: false, error: 'Authentication required' });
  }
}

/** preHandler factory: require at least the given role (admin > operator > viewer). */
export function requireRole(minRole: Role) {
  return async (request: FastifyRequest, reply: FastifyReply): Promise<void> => {
    if (!request.user) {
      await reply.code(401).send({ ok: false, error: 'Authentication required' });
      return;
    }
    if (ROLE_RANK[request.user.role] < ROLE_RANK[minRole]) {
      await reply.code(403).send({ ok: false, error: 'Insufficient permissions' });
    }
  };
}
