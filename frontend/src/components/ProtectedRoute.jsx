import { useEffect, useState } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { me } from '../api/authApi.js';
import { useAuthStore } from '../store/authStore.js';

export default function ProtectedRoute() {
  const token = useAuthStore(s => s.token);
  const login = useAuthStore(s => s.login);
  const logout = useAuthStore(s => s.logout);
  const [checking, setChecking] = useState(Boolean(token));
  const [valid, setValid] = useState(Boolean(token));

  useEffect(() => {
    let active = true;
    if (!token) {
      setChecking(false);
      setValid(false);
      return undefined;
    }

    setChecking(true);
    me()
      .then(session => {
        if (!active) return;
        login(session);
        setValid(true);
      })
      .catch(() => {
        if (!active) return;
        logout();
        setValid(false);
      })
      .finally(() => { if (active) setChecking(false); });

    return () => { active = false; };
  }, [token, login, logout]);

  if (checking) return <div className="route-loader"><span className="loader-ring" /><b>Verifying session…</b></div>;
  return valid && token ? <Outlet /> : <Navigate to="/signin" replace />;
}
