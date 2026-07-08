import type { FastifyInstance } from 'fastify';
import { getDb } from '../db/index';
import { wsHub } from '../ws/hub';

export async function healthRoutes(app: FastifyInstance): Promise<void> {
  app.get('/api/health', async () => {
    let dbOk = false;
    try {
      getDb().prepare('SELECT 1').get();
      dbOk = true;
    } catch {
      dbOk = false;
    }

    return {
      ok: true,
      name: 'starmade-server-panel',
      version: '0.1.0',
      uptimeSec: Math.round(process.uptime()),
      db: dbOk ? 'ok' : 'error',
      wsConnections: wsHub.connectionCount,
    };
  });
}
