import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore.js';
export default function RoleRoute({roles}){ const user=useAuthStore(s=>s.user); return user && roles.includes(user.role) ? <Outlet/> : <Navigate to="/app" replace/>; }
