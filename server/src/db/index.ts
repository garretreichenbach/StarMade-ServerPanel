import fs from 'node:fs';
import path from 'node:path';
import type { DatabaseSync as DatabaseSyncClass } from 'node:sqlite';
import { config } from '../config';
import { migrations } from './migrations/index';

// esbuild (via tsup) rewrites a static `import ... from 'node:sqlite'` down to a
// bare `sqlite` specifier — that builtin is newer than esbuild's known-builtins
// list. Loading it through a variable specifier is not statically analyzable, so
// esbuild leaves it verbatim and Node resolves `node:sqlite` correctly. The
// type-only import above (erased at build time) keeps full typing. `tsx` in dev
// handles the top-level await here without issue.
const sqliteSpecifier = 'node:sqlite';
const { DatabaseSync } = (await import(sqliteSpecifier)) as {
  DatabaseSync: typeof DatabaseSyncClass;
};

export type DB = DatabaseSyncClass;

let dbInstance: DB | null = null;

/**
 * Open (once) the panel SQLite database using Node's built-in `node:sqlite`,
 * apply pragmas, and run migrations. No native dependency to compile.
 */
export function getDb(): DB {
  if (dbInstance) return dbInstance;

  fs.mkdirSync(path.dirname(config.dbPath), { recursive: true });

  // Foreign-key constraints are enabled by default in DatabaseSync.
  const db = new DatabaseSync(config.dbPath);
  db.exec('PRAGMA journal_mode = WAL;');
  db.exec('PRAGMA busy_timeout = 5000;');

  runMigrations(db);

  dbInstance = db;
  return db;
}

function runMigrations(db: DB): void {
  db.exec(`
    CREATE TABLE IF NOT EXISTS schema_migrations (
      id         INTEGER PRIMARY KEY,
      name       TEXT NOT NULL,
      applied_at TEXT NOT NULL
    );
  `);

  const appliedRows = db.prepare('SELECT id FROM schema_migrations').all() as Array<{ id: number }>;
  const applied = new Set(appliedRows.map((r) => r.id));

  const record = db.prepare(
    'INSERT INTO schema_migrations (id, name, applied_at) VALUES (?, ?, ?)',
  );

  for (const migration of [...migrations].sort((a, b) => a.id - b.id)) {
    if (applied.has(migration.id)) continue;
    db.exec('BEGIN');
    try {
      db.exec(migration.up);
      record.run(migration.id, migration.name, new Date().toISOString());
      db.exec('COMMIT');
    } catch (err) {
      db.exec('ROLLBACK');
      throw err;
    }
  }
}

/** Close the database (used by tests / graceful shutdown). */
export function closeDb(): void {
  if (dbInstance) {
    dbInstance.close();
    dbInstance = null;
  }
}
