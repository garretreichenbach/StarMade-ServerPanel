# StarMade Server Panel

A web-based control panel for StarMade dedicated servers — the feature set of the
StarMade Launcher's built-in server panel, hosted on your own box and reachable from a
browser, plus multi-user accounts, roles, SSH-key login, host user/key management, and
stored credentials for remote servers.

## Architecture

An npm-workspaces monorepo:

| Package | What it is |
| --- | --- |
| `shared/` (`@ssp/shared`) | Framework-agnostic types, config schema, and pure helpers ported from the launcher. |
| `server/` (`@ssp/server`) | Fastify + WebSocket backend over SQLite (`better-sqlite3`). Spawns/controls servers, edits configs, tails logs, exposes the REST + WS API, handles auth. |
| `web/` (`@ssp/web`) | React 19 + Vite + Tailwind SPA (the panel UI). |

In dev the SPA runs on Vite (`:3000`) and proxies `/api` + `/ws` to the backend
(`:8080`). In production the backend serves the built SPA.

## Prerequisites

- Node.js ≥ 22 (the backend uses the built-in `node:sqlite` module — no native
  modules to compile, no C toolchain required)

## Getting started

```bash
npm install            # installs all workspaces, compiles native deps
cp server/.env.example server/.env
npm run dev            # backend on :8080, web on :3000
```

Open http://localhost:3000 — the shell shows live backend/database health.

## Scripts (root)

- `npm run dev` — run backend + web together (watch mode)
- `npm run build` — build shared, web, then bundle the server
- `npm start` — run the built backend (serves the built SPA)
- `npm test` — run workspace test suites
- `npm run typecheck` — typecheck all workspaces

## Roadmap

See `docs`/the plan for the milestone breakdown: **M1 foundation** (this),
M2 auth & accounts, M3 server profiles & local lifecycle, M4 config/files/logs/chat,
M5 metrics & world database, M6 remote parity (StarMote/Docker/SSH), M7 host
administration, M8 hardening & deploy.

## Security note

This panel spawns processes, edits files, manages OS users/SSH keys, and stores remote
credentials — run it behind a TLS-terminating reverse proxy, as a dedicated
non-root service account, with the privileged host operations confined to a narrow
`sudoers` allowlist. Details land in the hardening milestone.
