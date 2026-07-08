// Ordered schema migrations. Each `up` is executed once, inside a transaction,
// and recorded in `schema_migrations`. Never edit an already-shipped migration;
// add a new one instead.

export interface Migration {
  id: number;
  name: string;
  up: string;
}

export const migrations: Migration[] = [
  {
    id: 1,
    name: 'init',
    up: /* sql */ `
      CREATE TABLE users (
        id            TEXT PRIMARY KEY,
        username      TEXT NOT NULL UNIQUE COLLATE NOCASE,
        email         TEXT,
        password_hash TEXT,                       -- argon2; NULL for key-only accounts
        role          TEXT NOT NULL DEFAULT 'viewer',
        disabled      INTEGER NOT NULL DEFAULT 0,
        created_at    TEXT NOT NULL,
        last_login_at TEXT
      );

      CREATE TABLE user_ssh_keys (
        id          TEXT PRIMARY KEY,
        user_id     TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        name        TEXT NOT NULL,
        public_key  TEXT NOT NULL,
        fingerprint TEXT NOT NULL UNIQUE,
        created_at  TEXT NOT NULL
      );
      CREATE INDEX idx_user_ssh_keys_user ON user_ssh_keys(user_id);

      CREATE TABLE sessions (
        id         TEXT PRIMARY KEY,              -- sha256 of the opaque cookie token
        user_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        created_at TEXT NOT NULL,
        expires_at TEXT NOT NULL,
        ip         TEXT,
        user_agent TEXT
      );
      CREATE INDEX idx_sessions_user ON sessions(user_id);
      CREATE INDEX idx_sessions_expires ON sessions(expires_at);

      CREATE TABLE server_profiles (
        id         TEXT PRIMARY KEY,
        name       TEXT NOT NULL,
        data       TEXT NOT NULL,                 -- JSON of the non-secret ManagedItem fields
        created_at TEXT NOT NULL,
        updated_at TEXT NOT NULL
      );

      CREATE TABLE server_credentials (
        id               TEXT PRIMARY KEY,
        server_id        TEXT NOT NULL REFERENCES server_profiles(id) ON DELETE CASCADE,
        kind             TEXT NOT NULL,           -- starmote_password | ssh_password | ssh_key | ftp_password
        secret_encrypted BLOB NOT NULL,           -- AES-256-GCM ciphertext; never leaves the server
        created_at       TEXT NOT NULL
      );
      CREATE INDEX idx_server_credentials_server ON server_credentials(server_id);

      CREATE TABLE server_acls (
        user_id    TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        server_id  TEXT NOT NULL REFERENCES server_profiles(id) ON DELETE CASCADE,
        permission TEXT NOT NULL,
        PRIMARY KEY (user_id, server_id, permission)
      );

      CREATE TABLE audit_log (
        id         INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id    TEXT,
        action     TEXT NOT NULL,
        target     TEXT,
        detail     TEXT,
        ip         TEXT,
        created_at TEXT NOT NULL
      );
      CREATE INDEX idx_audit_created ON audit_log(created_at);
    `,
  },
];
