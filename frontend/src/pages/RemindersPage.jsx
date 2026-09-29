import { useEffect, useState } from 'react';
import { createReminder, deleteReminder, getReminders, updateReminder, updateReminderStatus } from '../api/reminderApi.js';
import { getPlans } from '../api/planningApi.js';
import { getMyTasks, getTasks } from '../api/taskApi.js';
import { messageFrom } from '../api/client.js';
import { useAuthStore } from '../store/authStore.js';
import Modal from '../components/Modal.jsx';
import StatusPill from '../components/StatusPill.jsx';

const empty = { title: '', message: '', remindAt: '', taskId: '', planId: '', repeatType: 'NONE' };
const toLocalInput = value => value ? String(value).slice(0, 16) : '';

export default function RemindersPage() {
  const user = useAuthStore(s => s.user);
  const [items, setItems] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [plans, setPlans] = useState([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(empty);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      setError('');
      const [reminderData, planData, taskData] = await Promise.all([
        getReminders(), getPlans(), user.role === 'USER' ? getMyTasks() : getTasks({})
      ]);
      setItems(reminderData);
      setPlans(planData);
      setTasks(taskData);
    } catch (e) { setError(messageFrom(e)); }
  };

  useEffect(() => {
    load();
    const handler = () => load();
    window.addEventListener('taskflow:event', handler);
    return () => window.removeEventListener('taskflow:event', handler);
  }, [user.role]);

  const openCreate = () => { setEditing(null); setForm(empty); setOpen(true); };
  const openEdit = reminder => {
    setEditing(reminder);
    setForm({
      title: reminder.title || '', message: reminder.message || '', remindAt: toLocalInput(reminder.remindAt),
      taskId: reminder.taskId || '', planId: reminder.planId || '', repeatType: reminder.repeatType || 'NONE'
    });
    setOpen(true);
  };

  const submit = async e => {
    e.preventDefault();
    try {
      const payload = { ...form, taskId: form.taskId ? Number(form.taskId) : null, planId: form.planId ? Number(form.planId) : null };
      if (editing) await updateReminder(editing.id, payload);
      else await createReminder(payload);
      setOpen(false);
      setEditing(null);
      setForm(empty);
      await load();
    } catch (e) { setError(messageFrom(e)); }
  };

  const changeStatus = async (reminder, status) => {
    try { await updateReminderStatus(reminder.id, status, status !== 'PENDING'); await load(); }
    catch (e) { setError(messageFrom(e)); }
  };

  const remove = async reminder => {
    if (!window.confirm(`Delete reminder "${reminder.title}"?`)) return;
    try { await deleteReminder(reminder.id); await load(); }
    catch (e) { setError(messageFrom(e)); }
  };

  return (
    <>
      <div className="page-head">
        <div><span className="eyebrow">REMINDERS</span><h1>Reminder center</h1><p>Schedule personal, plan and task reminders with repeat settings.</p></div>
        <button className="primary-btn" onClick={openCreate}>New reminder</button>
      </div>
      {error && <div className="form-error">{error}</div>}

      <div className="reminder-list">
        {items.map(reminder => (
          <article className={`reminder-card ${reminder.status.toLowerCase()}`} key={reminder.id}>
            <div>
              <div className="reminder-badges"><StatusPill value={reminder.status} /><span className="repeat-chip">{reminder.repeatType}</span></div>
              <h3>{reminder.title}</h3>
              <p>{reminder.message || 'No message'}</p>
              <small>{reminder.taskTitle ? `Task: ${reminder.taskTitle}` : reminder.planTitle ? `Plan: ${reminder.planTitle}` : 'General reminder'}</small>
            </div>
            <div className="reminder-time">
              <strong>{new Date(reminder.remindAt).toLocaleString()}</strong>
              <div>
                <button className="secondary-btn compact" onClick={() => openEdit(reminder)}>Edit</button>
                {reminder.status === 'PENDING' && <button className="secondary-btn compact" onClick={() => changeStatus(reminder, 'DISMISSED')}>Dismiss</button>}
                {reminder.status === 'DISMISSED' && <button className="secondary-btn compact" onClick={() => changeStatus(reminder, 'PENDING')}>Reopen</button>}
                <button className="danger-link" onClick={() => remove(reminder)}>Delete</button>
              </div>
            </div>
          </article>
        ))}
        {!items.length && <div className="empty-state"><b>No reminders</b><span>Create a reminder for a task, plan or personal follow-up.</span></div>}
      </div>

      {open && (
        <Modal title={editing ? 'Edit reminder' : 'Create reminder'} onClose={() => setOpen(false)}>
          <form className="modal-form" onSubmit={submit}>
            <label>Reminder title<input required maxLength="180" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} /></label>
            <label>Message<textarea rows="3" value={form.message} onChange={e => setForm({ ...form, message: e.target.value })} /></label>
            <label>Reminder date & time<input required type="datetime-local" value={form.remindAt} onChange={e => setForm({ ...form, remindAt: e.target.value })} /></label>
            <div className="form-row">
              <label>Task<select value={form.taskId} onChange={e => setForm({ ...form, taskId: e.target.value })}><option value="">No task</option>{tasks.map(t => <option key={t.id} value={t.id}>{t.title}</option>)}</select></label>
              <label>Plan<select value={form.planId} onChange={e => setForm({ ...form, planId: e.target.value })}><option value="">No plan</option>{plans.map(p => <option key={p.id} value={p.id}>{p.title}</option>)}</select></label>
            </div>
            <label>Repeat<select value={form.repeatType} onChange={e => setForm({ ...form, repeatType: e.target.value })}><option>NONE</option><option>DAILY</option><option>WEEKLY</option><option>MONTHLY</option></select></label>
            <button className="primary-btn">{editing ? 'Save changes' : 'Create reminder'}</button>
          </form>
        </Modal>
      )}
    </>
  );
}
