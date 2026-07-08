import { useEffect, useState } from 'react';
import { Trash2, Plus, UserPlus } from 'lucide-react';
import type { PanelUser, Role } from '@ssp/shared';
import { ROLES } from '@ssp/shared';
import { usersApi } from '../api/auth';
import { useAuth } from '../auth/AuthContext';
import { Alert, Badge, Button, Card, Field, Select, TextInput } from '../components/ui';

export function AdminUsers() {
  const { user: me } = useAuth();
  const [users, setUsers] = useState<PanelUser[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [showCreate, setShowCreate] = useState(false);

  const load = async () => {
    try {
      setUsers((await usersApi.list()).users);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load users');
    }
  };
  useEffect(() => { void load(); }, []);

  const act = async (fn: () => Promise<unknown>) => {
    setError(null);
    try {
      await fn();
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Action failed');
    }
  };

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Users &amp; roles</h2>
        <Button onClick={() => setShowCreate((s) => !s)}>
          <UserPlus className="h-4 w-4" /> New user
        </Button>
      </div>

      {error && <Alert>{error}</Alert>}
      {showCreate && <CreateUserCard onCreated={async () => { setShowCreate(false); await load(); }} />}

      <Card className="p-0">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-white/10 text-left text-xs uppercase tracking-wide text-white/40">
              <th className="px-4 py-3">User</th>
              <th className="px-4 py-3">Role</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id} className="border-b border-white/5 last:border-0">
                <td className="px-4 py-3">
                  <div>{u.username}</div>
                  {u.email && <div className="text-xs text-white/40">{u.email}</div>}
                </td>
                <td className="px-4 py-3">
                  <Select
                    value={u.role}
                    disabled={u.id === me?.id}
                    onChange={(e) => act(() => usersApi.update(u.id, { role: e.target.value as Role }))}
                    className="w-32"
                  >
                    {ROLES.map((r) => (
                      <option key={r} value={r}>{r}</option>
                    ))}
                  </Select>
                </td>
                <td className="px-4 py-3">
                  {u.disabled ? (
                    <Badge>disabled</Badge>
                  ) : (
                    <Badge tone="operator">active</Badge>
                  )}
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center justify-end gap-2">
                    {u.id !== me?.id && (
                      <Button variant="ghost" onClick={() => act(() => usersApi.update(u.id, { disabled: !u.disabled }))}>
                        {u.disabled ? 'Enable' : 'Disable'}
                      </Button>
                    )}
                    {u.id !== me?.id && (
                      <button
                        onClick={() => act(() => usersApi.remove(u.id))}
                        className="text-white/40 hover:text-red-400"
                        title="Delete user"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}

function CreateUserCard({ onCreated }: { onCreated: () => void }) {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<Role>('viewer');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await usersApi.create({ username, email: email || null, role, password: password || undefined });
      onCreated();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create user');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card>
      <h3 className="mb-4 text-sm font-medium uppercase tracking-wide text-white/50">New user</h3>
      <form onSubmit={submit} className="grid grid-cols-2 gap-4">
        {error && <div className="col-span-2"><Alert>{error}</Alert></div>}
        <Field label="Username">
          <TextInput value={username} onChange={(e) => setUsername(e.target.value)} />
        </Field>
        <Field label="Email (optional)">
          <TextInput type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </Field>
        <Field label="Role">
          <Select value={role} onChange={(e) => setRole(e.target.value as Role)}>
            {ROLES.map((r) => (
              <option key={r} value={r}>{r}</option>
            ))}
          </Select>
        </Field>
        <Field label="Password" hint="Optional — leave blank for SSH-key-only">
          <TextInput type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="new-password" />
        </Field>
        <div className="col-span-2">
          <Button type="submit" loading={busy} disabled={!username}>
            <Plus className="h-4 w-4" /> Create user
          </Button>
        </div>
      </form>
    </Card>
  );
}
