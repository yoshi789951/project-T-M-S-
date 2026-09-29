import { useEffect, useMemo, useState } from 'react';
import { getMyTasks, updateTaskStatus } from '../api/taskApi.js';
import { messageFrom } from '../api/client.js';
import TaskCard from '../components/TaskCard.jsx';

export default function MyTasksPage() {
  const [tasks, setTasks] = useState([]);
  const [status, setStatus] = useState('ALL');
  const [error, setError] = useState('');

  const load = async () => {
    try { setError(''); setTasks(await getMyTasks()); }
    catch (e) { setError(messageFrom(e)); }
  };

  useEffect(() => {
    load();
    const handler = () => load();
    window.addEventListener('taskflow:event', handler);
    return () => window.removeEventListener('taskflow:event', handler);
  }, []);

  const filtered = useMemo(
    () => status === 'ALL' ? tasks : tasks.filter(task => task.status === status),
    [tasks, status]
  );

  const change = async (task, nextStatus) => {
    try { await updateTaskStatus(task.id, nextStatus); await load(); }
    catch (e) { setError(messageFrom(e)); }
  };

  return (
    <>
      <div className="page-head">
        <div><span className="eyebrow">PERSONAL WORKLIST</span><h1>My Tasks</h1><p>Your assignments ordered by due date.</p></div>
        <select className="filter-select" value={status} onChange={e => setStatus(e.target.value)}><option value="ALL">All statuses</option><option>TODO</option><option>IN_PROGRESS</option><option>REVIEW</option><option>DONE</option></select>
      </div>
      {error && <div className="form-error">{error}</div>}
      <div className="task-list-grid">
        {filtered.map(task => <TaskCard key={task.id} task={task} canUpdate onStatus={change} />)}
        {!filtered.length && <div className="empty-state"><b>No matching tasks</b><span>Assigned work will appear here.</span></div>}
      </div>
    </>
  );
}
