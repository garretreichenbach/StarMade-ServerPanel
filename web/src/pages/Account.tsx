import { useEffect, useState } from 'react';
import { Trash2, Plus } from 'lucide-react';
import type { PanelSshKey } from '@ssp/shared';
import { authApi } from '../api/auth';
import { useAuth } from '../auth/AuthContext';
import { Alert, Badge, Button, Card, Field, TextInput } from '../components/ui';

export function Account() {
  const { user } = useAuth();
  if (!user) return null;
  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <div>
        <h2 className="text-xl font-semibold">Account</h2>
        <p className="mt-1 flex items-center gap-2 text-sm text-white/50">
          {user.username} <Badge tone={user.role}>{user.role}</Badge>
        </p>
      </div>
      <ChangePasswordCard />
      <SshKeysCard />
    </div>
  );
}

function ChangePasswordCard() {
  const [current, setCurrent] = useState('');
  const [next, setNext] = useState('');
  const [msg, setMsg] = useState<{ kind: 'success' | 'error'; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsg(null);
    setBusy(true);
    try {
      await authApi.changePassword(current, next);
      setMsg({ kind: 'success', text: 'Password changed.' });
      setCurrent('');
      setNext('');
    } catch (err) {
      setMsg({ kind: 'error', text: err instanceof Error ? err.message : 'Failed' });
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card>
      <h3 className="mb-4 text-sm font-medium uppercase tracking-wide text-white/50">Change password</h3>
      <form onSubmit={submit} className="flex flex-col gap-4">
        {msg && <Alert kind={msg.kind}>{msg.text}</Alert>}
        <Field label="Current password">
          <TextInput type="password" value={current} onChange={(e) => setCurrent(e.target.value)} autoComplete="current-password" />
        </Field>
        <Field label="New password" hint="At least 8 characters">
          <TextInput type="password" value={next} onChange={(e) => setNext(e.target.value)} autoComplete="new-password" />
        </Field>
        <Button type="submit" loading={busy} disabled={!current || next.length < 8} className="self-start">
          Update password
        </Button>
      </form>
    </Card>
  );
}

function SshKeysCard() {
  const [keys, setKeys] = useState<PanelSshKey[]>([]);
  const [name, setName] = useState('');
  const [publicKey, setPublicKey] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const load = async () => {
    try {
      setKeys((await authApi.listMyKeys()).keys);
    } catch {
      /* ignore */
    }
  };
  useEffect(() => { void load(); }, []);

  const add = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await authApi.addMyKey(name, publicKey);
      setName('');
      setPublicKey('');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to add key');
    } finally {
      setBusy(false);
    }
  };

  const remove = async (id: string) => {
    await authApi.deleteMyKey(id);
    await load();
  };

  return (
    <Card>
      <h3 className="mb-1 text-sm font-medium uppercase tracking-wide text-white/50">SSH keys</h3>
      <p className="mb-4 text-xs text-white/40">Registered public keys can be used to sign in without a password.</p>

      <ul className="mb-4 flex flex-col gap-2">
        {keys.length === 0 && <li className="text-sm text-white/40">No keys registered.</li>}
        {keys.map((k) => (
          <li key={k.id} className="flex items-center justify-between rounded-lg border border-white/10 bg-black/20 px-3 py-2">
            <div className="min-w-0">
              <div className="truncate text-sm">{k.name}</div>
              <div className="truncate font-mono text-xs text-white/40">{k.fingerprint}</div>
            </div>
            <button onClick={() => remove(k.id)} className="text-white/40 hover:text-red-400" title="Remove">
              <Trash2 className="h-4 w-4" />
            </button>
          </li>
        ))}
      </ul>

      <form onSubmit={add} className="flex flex-col gap-3 border-t border-white/10 pt-4">
        {error && <Alert>{error}</Alert>}
        <Field label="Key name">
          <TextInput value={name} onChange={(e) => setName(e.target.value)} placeholder="laptop" />
        </Field>
        <Field label="Public key" hint="Contents of e.g. ~/.ssh/id_ed25519.pub">
          <textarea
            value={publicKey}
            onChange={(e) => setPublicKey(e.target.value)}
            rows={3}
            placeholder="ssh-ed25519 AAAA…"
            className="w-full rounded-lg border border-white/15 bg-black/30 px-3 py-2 font-mono text-xs text-white outline-none placeholder:text-white/30 focus:border-sky-400"
          />
        </Field>
        <Button type="submit" loading={busy} disabled={!name || !publicKey} className="self-start">
          <Plus className="h-4 w-4" /> Add key
        </Button>
      </form>
    </Card>
  );
}
