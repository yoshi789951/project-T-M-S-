import { useEffect, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore.js';
import { useUiStore } from '../store/uiStore.js';
import { connectRealtime, disconnectRealtime } from '../realtime.js';
import Logo from './Logo.jsx';

export default function AppShell(){
  const {user,token,logout}=useAuthStore(); const {theme,toggleTheme}=useUiStore(); const navigate=useNavigate(); const [notice,setNotice]=useState(null);
  useEffect(()=>{ document.documentElement.dataset.theme=theme; },[theme]);
  useEffect(()=>{ const s=connectRealtime(token); if(!s)return; const handler=e=>{setNotice(e);window.dispatchEvent(new CustomEvent('taskflow:event',{detail:e}));setTimeout(()=>setNotice(null),3500)};s.on('task-event',handler);return()=>{s.off('task-event',handler);disconnectRealtime();};},[token]);
  useEffect(()=>{ const unauthorized=()=>{logout();navigate('/signin',{replace:true});}; window.addEventListener('taskflow:unauthorized',unauthorized); return()=>window.removeEventListener('taskflow:unauthorized',unauthorized); },[logout,navigate]);
  const signout=()=>{logout();navigate('/signin');};
  return <div className="app-shell">
    <aside className="sidebar">
      <Logo/>
      <nav>
        <NavLink to="/app" end>Overview</NavLink>
        <NavLink to="/app/projects">Projects</NavLink>
        <NavLink to="/app/my-tasks">My Tasks</NavLink>
        <NavLink to="/app/planning">Planning</NavLink>
        <NavLink to="/app/reminders">Reminders</NavLink>
        {user?.role==='ADMIN' && <NavLink to="/app/admin/users">Users</NavLink>}
      </nav>
      <div className="sidebar-bottom"><span className="role-chip">{user?.role}</span><small>{user?.email}</small></div>
    </aside>
    <main className="workspace">
      <header className="topbar"><div><span className="eyebrow">TASK MANAGEMENT</span><strong>Welcome, {user?.name?.split(' ')[0]}</strong></div><div className="top-actions"><button className="icon-btn" onClick={toggleTheme}>{theme==='dark'?'Light':'Dark'} mode</button><button className="secondary-btn" onClick={signout}>Sign out</button></div></header>
      {notice && <div className="event-toast"><b>{notice.type.replaceAll('_',' ')}</b><span>Live update received</span></div>}
      <section className="content"><Outlet/></section>
    </main>
  </div>;
}
