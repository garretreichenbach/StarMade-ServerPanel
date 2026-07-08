import type { PanelUser, PanelSshKey, Role } from '@ssp/shared';
import { api } from './client';

export interface SshChallenge {
  ok: true;
  challengeId: string;
  challenge: string;
  namespace: string;
  signCommand: string;
}

export const authApi = {
  me: () => api.get<{ ok: true; user: PanelUser }>('/api/auth/me'),
  login: (username: string, password: string) =>
    api.post<{ ok: true; user: PanelUser }>('/api/auth/login', { username, password }),
  logout: () => api.post<{ ok: true }>('/api/auth/logout'),
  changePassword: (currentPassword: string, newPassword: string) =>
    api.post<{ ok: true }>('/api/auth/change-password', { currentPassword, newPassword }),

  listMyKeys: () => api.get<{ ok: true; keys: PanelSshKey[] }>('/api/auth/ssh-keys'),
  addMyKey: (name: string, publicKey: string) =>
    api.post<{ ok: true; key: PanelSshKey }>('/api/auth/ssh-keys', { name, publicKey }),
  deleteMyKey: (id: string) => api.del<{ ok: true }>(`/api/auth/ssh-keys/${id}`),

  sshChallenge: (username: string) =>
    api.post<SshChallenge>('/api/auth/ssh/challenge', { username }),
  sshVerify: (challengeId: string, signature: string) =>
    api.post<{ ok: true; user: PanelUser }>('/api/auth/ssh/verify', { challengeId, signature }),
};

export interface CreateUserInput {
  username: string;
  email?: string | null;
  role: Role;
  password?: string;
}

export const usersApi = {
  list: () => api.get<{ ok: true; users: PanelUser[] }>('/api/users'),
  create: (input: CreateUserInput) => api.post<{ ok: true; user: PanelUser }>('/api/users', input),
  update: (id: string, patch: { role?: Role; disabled?: boolean; email?: string | null }) =>
    api.patch<{ ok: true; user: PanelUser }>(`/api/users/${id}`, patch),
  resetPassword: (id: string, newPassword: string) =>
    api.post<{ ok: true }>(`/api/users/${id}/reset-password`, { newPassword }),
  remove: (id: string) => api.del<{ ok: true }>(`/api/users/${id}`),
};
