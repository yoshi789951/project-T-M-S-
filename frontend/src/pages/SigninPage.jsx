import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signin } from '../api/authApi.js';
import { messageFrom } from '../api/client.js';
import { useAuthStore } from '../store/authStore.js';
import Logo from '../components/Logo.jsx';

export default function SigninPage() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const login = useAuthStore(s => s.login);
  const navigate = useNavigate();

  const submit = async e => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const auth = await signin(form);
      login(auth);
      navigate('/app');
    } catch (err) { setError(messageFrom(err)); }
    finally { setBusy(false); }
  };

  return (
    <div className="auth-page">
      <div className="auth-panel">
        <Logo />
        <span className="eyebrow">SECURE WORKSPACE</span>
        <h1>Welcome back</h1>
        <p>Sign in to manage projects, tasks, plans and reminders.</p>
        <form onSubmit={submit}>
          <label>Email<input autoComplete="email" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required /></label>
          <label>Password<input autoComplete="current-password" type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} required /></label>
          {error && <div className="form-error">{error}</div>}
          <button className="primary-btn" disabled={busy}>{busy ? 'Signing in...' : 'Sign in'}</button>
        </form>
        <div className="demo-credentials"><b>Development admin</b><span>admin@taskflow.local</span><span>Admin123!</span></div>
        <small>New user? <Link to="/signup">Create an account</Link></small>
      </div>
      <div className="auth-art"><div className="orb one" /><div className="orb two" /><h2>Focus on the work that matters.</h2><p>Role-based access, MySQL persistence, JWT security and realtime Redis/Socket.IO task events.</p></div>
    </div>
  );
}
