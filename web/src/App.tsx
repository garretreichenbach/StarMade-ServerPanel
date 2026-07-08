import { useState } from 'react';
import { Server, LayoutDashboard, UserCog, Users, LogOut } from 'lucide-react';
import { useAuth } from './auth/AuthContext';
import { Badge, Spinner } from './components/ui';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { Account } from './pages/Account';
import { AdminUsers } from './pages/AdminUsers';

type View = 'dashboard' | 'account' | 'admin-users';

export function App() {
  const { user, loading, logout } = useAuth();
  const [view, setView] = useState<View>('dashboard');

  if (loading) {
    return (
      <div className="flex min-h-full items-center justify-center">
        <Spinner label="Loading…" />
      </div>
    );
  }

  if (!user) return <Login />;

  const nav: Array<{ id: View; label: string; icon: React.ReactNode; adminOnly?: boolean }> = [
    { id: 'dashboard', label: 'Dashboard', icon: <LayoutDashboard className="h-4 w-4" /> },
    { id: 'account', label: 'Account', icon: <UserCog className="h-4 w-4" /> },
    { id: 'admin-users', label: 'Users & roles', icon: <Users className="h-4 w-4" />, adminOnly: true },
  ];

  return (
    <div className="flex min-h-full">
      <aside className="flex w-60 flex-col border-r border-white/10 bg-black/20 p-4">
        <div className="mb-8 flex items-center gap-2 px-2">
          <Server className="h-6 w-6 text-sky-400" />
          <span className="font-semibold">Server Panel</span>
        </div>
        <nav className="flex flex-1 flex-col gap-1">
          {nav
            .filter((item) => !item.adminOnly || user.role === 'admin')
            .map((item) => (
              <button
                key={item.id}
                onClick={() => setView(item.id)}
                className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition ${
                  view === item.id ? 'bg-sky-500/15 text-sky-200' : 'text-white/60 hover:bg-white/5 hover:text-white'
                }`}
              >
                {item.icon}
                {item.label}
              </button>
            ))}
        </nav>
        <div className="mt-4 border-t border-white/10 pt-4">
          <div className="mb-2 flex items-center gap-2 px-2 text-sm">
            <span className="truncate">{user.username}</span>
            <Badge tone={user.role}>{user.role}</Badge>
          </div>
          <button
            onClick={() => void logout()}
            className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm text-white/60 transition hover:bg-white/5 hover:text-white"
          >
            <LogOut className="h-4 w-4" /> Sign out
          </button>
        </div>
      </aside>

      <main className="flex-1 overflow-y-auto p-8">
        {view === 'dashboard' && <Dashboard />}
        {view === 'account' && <Account />}
        {view === 'admin-users' && user.role === 'admin' && <AdminUsers />}
      </main>
    </div>
  );
}
