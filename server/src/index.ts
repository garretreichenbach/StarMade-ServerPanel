import { buildApp } from './app';
import { config } from './config';
import { getDb, closeDb } from './db';
import { bootstrapAdmin } from './auth/bootstrap';
import { purgeExpiredSessions } from './auth/sessions';

async function main(): Promise<void> {
  // Open + migrate the database up front so startup fails fast on a bad DB.
  getDb();
  purgeExpiredSessions();

  const app = await buildApp();

  // Ensure there is an admin account to log in with.
  await bootstrapAdmin(app.log);

  const shutdown = async (signal: string) => {
    app.log.info({ signal }, 'shutting down');
    await app.close();
    closeDb();
    process.exit(0);
  };
  process.on('SIGINT', () => void shutdown('SIGINT'));
  process.on('SIGTERM', () => void shutdown('SIGTERM'));

  try {
    await app.listen({ host: config.host, port: config.port });
  } catch (err) {
    app.log.error(err);
    process.exit(1);
  }
}

void main();
