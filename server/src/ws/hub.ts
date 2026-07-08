import type { WebSocket } from 'ws';

/**
 * Minimal topic-based pub/sub hub for live streams (logs, chat, metrics,
 * lifecycle status, StarMote runtime events). Clients send
 * `{ type: 'subscribe' | 'unsubscribe', topic }`; the server pushes
 * `{ topic, event, data }` frames to subscribers.
 *
 * Topics are namespaced, e.g. `log:<serverId>`, `metrics:<serverId>`,
 * `status:<serverId>`, `chat:<serverId>`.
 */
export class WsHub {
  private readonly subscriptions = new Map<WebSocket, Set<string>>();

  add(socket: WebSocket): void {
    this.subscriptions.set(socket, new Set());

    socket.on('message', (raw: Buffer) => {
      let msg: unknown;
      try {
        msg = JSON.parse(raw.toString());
      } catch {
        return;
      }
      if (!msg || typeof msg !== 'object') return;
      const { type, topic } = msg as { type?: string; topic?: string };
      if (typeof topic !== 'string') return;
      const topics = this.subscriptions.get(socket);
      if (!topics) return;
      if (type === 'subscribe') topics.add(topic);
      else if (type === 'unsubscribe') topics.delete(topic);
    });

    socket.on('close', () => this.subscriptions.delete(socket));
    socket.on('error', () => this.subscriptions.delete(socket));
  }

  /** Push an event to every socket subscribed to `topic`. */
  broadcast(topic: string, event: string, data: unknown): void {
    const frame = JSON.stringify({ topic, event, data });
    for (const [socket, topics] of this.subscriptions) {
      if (topics.has(topic) && socket.readyState === socket.OPEN) {
        socket.send(frame);
      }
    }
  }

  get connectionCount(): number {
    return this.subscriptions.size;
  }
}

export const wsHub = new WsHub();
