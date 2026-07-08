// Core shared types for the StarMade Server Panel.
//
// The server-profile shape and lifecycle/log enums are adapted from the
// StarMade Launcher (`types/index.ts`) so the ported helpers in
// `serverPanel.ts` and the ported UI keep working with minimal changes.

// ─── Server lifecycle / logs (from launcher) ────────────────────────────────

export type ServerLifecycleState = 'starting' | 'running' | 'stopping' | 'stopped' | 'error';
export type ServerLogLevel = 'INFO' | 'WARNING' | 'ERROR' | 'FATAL' | 'DEBUG' | 'stdout' | 'stderr';

export type RemoteBackendType = 'starmote' | 'azure-vm' | 'docker';
export type RemoteFileAccessProtocol = 'none' | 'ftp' | 'sftp';

// ─── Managed server profile ─────────────────────────────────────────────────
// Mirror of the launcher `ManagedItem` fields the reused helpers/UI touch.
// In the web panel this is persisted per row in `server_profiles`; secrets
// (passwords / key material) live separately in `server_credentials` and are
// never part of this object sent to the browser.

export interface ManagedItem {
  id: string;
  name: string;
  version?: string;
  icon?: string;
  /** Local install directory (empty for pure remote profiles). */
  path?: string;
  lastPlayed?: string;
  port?: string;
  /** Optional server address/hostname used for direct connections and panel display. */
  serverIp?: string;
  /** Optional default/max player cap for server installs. */
  maxPlayers?: number;
  /** True when this profile is a remote server (no local install/download path). */
  isRemote?: boolean;
  /** Which remote connection backend to use. Defaults to 'starmote' when omitted. */
  remoteBackend?: RemoteBackendType;
  // ── SSH backend fields ──────────────────────────────────────────────────
  azureVmSshPort?: string;
  azureVmSshKeyPath?: string;
  azureVmSshUsername?: string;
  azureVmScreenSession?: string;
  // ── Docker backend fields ────────────────────────────────────────────────
  dockerHost?: string;
  dockerContainer?: string;
  dockerScreenSession?: string;
  // ── Remote file access ─────────────────────────────────────────────────
  remoteFileAccessProtocol?: RemoteFileAccessProtocol;
  remoteFileAccessHost?: string;
  remoteFileAccessPort?: string;
  remoteFileAccessUsername?: string;
  remoteFileAccessRootPath?: string;
  // ── Local launch settings ──────────────────────────────────────────────
  installed?: boolean;
  requiredJavaVersion?: 8 | 21;
  /** Minimum JVM heap in MB (passed as -Xms). */
  minMemory?: number;
  /** Maximum JVM heap in MB (passed as -Xmx). */
  maxMemory?: number;
  /** Extra JVM arguments (must NOT include -Xms/-Xmx). */
  jvmArgs?: string;
  /** Override path to the java executable for this profile. */
  customJavaPath?: string;
}

// ─── Panel accounts / roles ─────────────────────────────────────────────────

/** Fixed role tiers for the first cut; per-server ACLs can refine later. */
export type Role = 'admin' | 'operator' | 'viewer';

export const ROLES: readonly Role[] = ['admin', 'operator', 'viewer'] as const;

/** Public-safe user shape (never includes password hash or secrets). */
export interface PanelUser {
  id: string;
  username: string;
  email: string | null;
  role: Role;
  disabled: boolean;
  createdAt: string;
  lastLoginAt: string | null;
}

export interface PanelSshKey {
  id: string;
  userId: string;
  name: string;
  fingerprint: string;
  createdAt: string;
}

// ─── Remote server credentials ──────────────────────────────────────────────
// Secrets the panel uses to connect OUT to remote servers. Stored encrypted at
// rest (AES-256-GCM) and never returned to the browser — the client only sees
// which secrets are set (RemoteCredentialStatus) and can write new ones.

export type RemoteCredentialKind =
  | 'starmote_password'
  | 'ssh_password'
  | 'ssh_key'
  | 'ftp_password';

/** Write-only payload when saving credentials for a server profile. */
export interface RemoteCredentialInput {
  kind: RemoteCredentialKind;
  /** Password for starmote_password / ssh_password / ftp_password. */
  password?: string;
  /** PEM / OpenSSH private key for ssh_key. */
  privateKey?: string;
  /**
   * Passphrase that unlocks a passphrase-protected private key. Optional — omit
   * for unencrypted keys. Stored encrypted alongside the key and supplied to the
   * SSH client at connect time.
   */
  passphrase?: string;
}

/** Public-safe view: what is configured, never the secret material itself. */
export interface RemoteCredentialStatus {
  kind: RemoteCredentialKind;
  hasPassword: boolean;
  hasPrivateKey: boolean;
  hasPassphrase: boolean;
}

// ─── API envelope ───────────────────────────────────────────────────────────

export type ApiOk<T> = { ok: true } & T;
export type ApiErr = { ok: false; error: string };
export type ApiResult<T> = ApiOk<T> | ApiErr;
