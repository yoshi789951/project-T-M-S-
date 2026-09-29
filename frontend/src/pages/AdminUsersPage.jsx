import { useEffect, useState } from 'react';
import { getUsers, setActive, setRole } from '../api/adminApi.js';
import { messageFrom } from '../api/client.js';
import StatusPill from '../components/StatusPill.jsx';

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');

  const load = async () => {
    try { setError(''); setUsers(await getUsers()); }
    catch (e) { setError(messageFrom(e)); }
  };

  useEffect(() => { load(); }, []);

  const changeRole = async (id, role) => {
    try { await setRole(id, role); await load(); }
    catch (e) { setError(messageFrom(e)); }
  };

  const toggleActive = async user => {
    try { await setActive(user.id, !user.active); await load(); }
    catch (e) { setError(messageFrom(e)); }
  };

  return (
    <>
      <div className="page-head"><div><span className="eyebrow">ADMINISTRATION</span><h1>User access</h1><p>Assign roles and activate or suspend accounts.</p></div></div>
      {error && <div className="form-error">{error}</div>}
      <div className="table-wrap"><table><thead><tr><th>User</th><th>Role</th><th>Status</th><th>Last login</th><th>Created</th><th>Actions</th></tr></thead><tbody>
        {users.map(user => <tr key={user.id}>
          <td><b>{user.name}</b><small>{user.email}</small></td>
          <td><StatusPill value={user.role} /></td>
          <td>{user.active ? 'Active' : 'Suspended'}</td>
          <td>{user.lastLogin ? new Date(user.lastLogin).toLocaleString() : 'Never'}</td>
          <td>{new Date(user.createdAt).toLocaleDateString()}</td>
          <td><div className="row-actions"><select value={user.role} onChange={e => changeRole(user.id, e.target.value)}><option>USER</option><option>MANAGER</option><option>ADMIN</option></select><button className="secondary-btn" onClick={() => toggleActive(user)}>{user.active ? 'Suspend' : 'Activate'}</button></div></td>
        </tr>)}
      </tbody></table></div>
    </>
  );
}
