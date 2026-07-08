import { useState } from 'react';
import { Server, KeyRound, Terminal, Copy, Check } from 'lucide-react';
import { authApi, type SshChallenge } from '../api/auth';
import { useAuth } from '../auth/AuthContext';
import { Alert, Button, Card, Field, TextInput } from '../components/ui';

type Tab = 'password' | 'ssh';

export function Login() {
  const [tab, setTab] = useState<Tab>('password');
  return (
    <div className="flex min-h-full flex-col items-center justify-center gap-6 p-8">
      <header className="flex items-center gap-3">
        <Server className="h-8 w-8 text-sky-400" />
        <h1 className="text-2xl font-semibold tracking-tight">StarMade Server Panel</h1>
      </header>

      <Card className="w-full max-w-md">
        <div className="mb-5 flex gap-1 rounded-lg bg-black/30 p-1 text-sm">
          <TabButton active={tab === 'password'} onClick={() => setTab('password')} icon={<KeyRound className="h-4 w-4" />}>
            Password
          </TabButton>
          <TabButton active={tab === 'ssh'} onClick={() => setTab('ssh')} icon={<Terminal className="h-4 w-4" />}>
            SSH key
          </TabButton>
        </div>
        {tab === 'password' ? <PasswordForm /> : <SshForm />}
      </Card>
    </div>
  );
}

function TabButton({ active, onClick, icon, children }: { active: boolean; onClick: () => void; icon: React.ReactNode; children: React.ReactNode }) {
  return (
    <button
      onClick={onClick}
      className={`flex flex-1 items-center justify-center gap-2 rounded-md px-3 py-1.5 transition ${
        active ? 'bg-white/10 text-white' : 'text-white/50 hover:text-white/80'
      }`}
    >
      {icon}
      {children}
    </button>
  );
}

function PasswordForm() {
  const { setUser } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const res = await authApi.login(username, password);
      setUser(res.user);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setBusy(false);
    }
  };

  return (
    <form onSubmit={submit} className="flex flex-col gap-4">
      {error && <Alert>{error}</Alert>}
      <Field label="Username">
        <TextInput value={username} onChange={(e) => setUsername(e.target.value)} autoFocus autoComplete="username" />
      </Field>
      <Field label="Password">
        <TextInput type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />
      </Field>
      <Button type="submit" loading={busy} disabled={!username || !password}>
        Sign in
      </Button>
    </form>
  );
}

function SshForm() {
  const { setUser } = useAuth();
  const [username, setUsername] = useState('');
  const [challenge, setChallenge] = useState<SshChallenge | null>(null);
  const [signature, setSignature] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [copied, setCopied] = useState(false);

  const getChallenge = async () => {
    setError(null);
    setBusy(true);
    try {
      setChallenge(await authApi.sshChallenge(username));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get challenge');
    } finally {
      setBusy(false);
    }
  };

  const verify = async () => {
    if (!challenge) return;
    setError(null);
    setBusy(true);
    try {
      const res = await authApi.sshVerify(challenge.challengeId, signature);
      setUser(res.user);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Verification failed');
    } finally {
      setBusy(false);
    }
  };

  const copyCmd = async () => {
    if (!challenge) return;
    await navigator.clipboard.writeText(challenge.signCommand);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  return (
    <div className="flex flex-col gap-4">
      {error && <Alert>{error}</Alert>}
      <Field label="Username">
        <TextInput value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
      </Field>

      {!challenge ? (
        <Button onClick={getChallenge} loading={busy} disabled={!username}>
          Continue
        </Button>
      ) : (
        <>
          <div>
            <div className="mb-1 flex items-center justify-between">
              <span className="text-xs font-medium uppercase tracking-wide text-white/50">1. Sign this challenge</span>
              <button onClick={copyCmd} className="flex items-center gap-1 text-xs text-sky-300 hover:text-sky-200">
                {copied ? <Check className="h-3 w-3" /> : <Copy className="h-3 w-3" />}
                {copied ? 'Copied' : 'Copy'}
              </button>
            </div>
            <pre className="overflow-x-auto rounded-lg border border-white/10 bg-black/40 p-3 text-xs text-white/80">
              {challenge.signCommand}
            </pre>
            <p className="mt-1 text-xs text-white/40">
              Run it in a terminal (swap the key path if needed), then paste the output below.
              If your key has a passphrase, ssh-keygen prompts for it locally (or uses
              ssh-agent) — it never leaves your machine.
            </p>
          </div>
          <Field label="2. Paste the SSH signature">
            <textarea
              value={signature}
              onChange={(e) => setSignature(e.target.value)}
              rows={6}
              placeholder="-----BEGIN SSH SIGNATURE-----"
              className="w-full rounded-lg border border-white/15 bg-black/30 px-3 py-2 font-mono text-xs text-white outline-none placeholder:text-white/30 focus:border-sky-400"
            />
          </Field>
          <div className="flex gap-2">
            <Button variant="ghost" onClick={() => { setChallenge(null); setSignature(''); }}>
              Back
            </Button>
            <Button onClick={verify} loading={busy} disabled={!signature.includes('SSH SIGNATURE')} className="flex-1">
              Verify &amp; sign in
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
