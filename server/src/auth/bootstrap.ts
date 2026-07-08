import { randomBytes } from 'node:crypto';
import type { FastifyBaseLogger } from 'fastify';
import { config } from '../config';
import { countUsers, createUser } from './users';
import { hashPassword } from './passwords';
import { audit } from '../audit';

/**
 * On an empty database, create the initial admin account so there is a way to
 * log in. Uses SSP_ADMIN_USERNAME / SSP_ADMIN_PASSWORD when provided; otherwise
 * generates a random password and logs it once.
 */
export async function bootstrapAdmin(log: FastifyBaseLogger): Promise<void> {
  if (countUsers() > 0) return;

  const username = config.adminUsername;
  let password = config.adminPassword;
  let generated = false;
  if (!password) {
    password = randomBytes(12).toString('base64url');
    generated = true;
  }

  const passwordHash = await hashPassword(password);
  const user = createUser({ username, role: 'admin', passwordHash });
  audit({ userId: user.id, action: 'user.bootstrap', target: username });

  if (generated) {
    log.warn('════════════════════════════════════════════════════════════');
    log.warn(`Created initial admin account "${username}"`);
    log.warn(`Generated password: ${password}`);
    log.warn('Log in and change it. Set SSP_ADMIN_PASSWORD to control this.');
    log.warn('════════════════════════════════════════════════════════════');
  } else {
    log.info(`Created initial admin account "${username}".`);
  }
}
