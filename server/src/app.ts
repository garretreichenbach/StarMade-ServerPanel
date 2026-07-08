import Fastify, { type FastifyInstance } from 'fastify';
import fastifyCookie from '@fastify/cookie';
import fastifyWebsocket from '@fastify/websocket';
import fastifyStatic from '@fastify/static';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { config } from './config';
import { healthRoutes } from './routes/health';
import { authRoutes } from './routes/auth';
import { userAdminRoutes } from './routes/users';
import { registerAuthContext } from './auth/middleware';
import { wsHub } from './ws/hub';

/** Build and configure the Fastify app (without starting the listener). */
export async function buildApp(): Promise<FastifyInstance> {
  const app = Fastify({
    logger: config.isTest
      ? false
      : { level: config.isProduction ? 'info' : 'debug' },
    trustProxy: true,
  });

  await app.register(fastifyCookie);
  await app.register(fastifyWebsocket);

  // Populate request.user from the session cookie on every request.
  registerAuthContext(app);

  // REST routes.
  await app.register(healthRoutes);
  await app.register(authRoutes);
  await app.register(userAdminRoutes);

  // Live-stream WebSocket endpoint.
  app.register(async (scope) => {
    scope.get('/ws', { websocket: true }, (socket) => {
      wsHub.add(socket);
    });
  });

  // In production, serve the built SPA and fall back to index.html for client
  // routing. The web build is expected at ../../web/dist relative to this file.
  const webDist = resolveWebDist();
  if (webDist) {
    await app.register(fastifyStatic, { root: webDist, wildcard: false });
    app.setNotFoundHandler((req, reply) => {
      if (req.raw.url?.startsWith('/api') || req.raw.url?.startsWith('/ws')) {
        reply.code(404).send({ ok: false, error: 'Not found' });
        return;
      }
      reply.sendFile('index.html');
    });
  }

  return app;
}

function resolveWebDist(): string | null {
  const here = path.dirname(fileURLToPath(import.meta.url));
  // dev (src/app.ts): ../../web/dist ; prod (dist/index.js): ../../web/dist
  const candidates = [
    path.resolve(here, '../../web/dist'),
    path.resolve(here, '../web/dist'),
  ];
  return candidates.find((p) => fs.existsSync(path.join(p, 'index.html'))) ?? null;
}
