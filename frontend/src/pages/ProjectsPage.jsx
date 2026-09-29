import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { createProject, deleteProject, getProjects, updateProject } from '../api/projectApi.js';
import { messageFrom } from '../api/client.js';
import { useAuthStore } from '../store/authStore.js';
import Modal from '../components/Modal.jsx';
import StatusPill from '../components/StatusPill.jsx';

const empty = { name: '', description: '', status: 'ACTIVE', color: '#2563eb' };

export default function ProjectsPage() {
  const user = useAuthStore(s => s.user);
  const canManage = ['ADMIN', 'MANAGER'].includes(user.role);
  const [items, setItems] = useState([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [error, setError] = useState('');
  const [form, setForm] = useState(empty);

  const load = () => getProjects().then(setItems).catch(e => setError(messageFrom(e)));
  useEffect(() => {
    load();
    const handler = () => load();
    window.addEventListener('taskflow:event', handler);
    return () => window.removeEventListener('taskflow:event', handler);
  }, []);

  const openCreate = () => { setEditing(null); setForm(empty); setOpen(true); };
  const openEdit = project => { setEditing(project); setForm({ name: project.name, description: project.description || '', status: project.status, color: project.color }); setOpen(true); };

  const submit = async e => {
    e.preventDefault();
    try {
      setError('');
      if (editing) await updateProject(editing.id, form);
      else await createProject(form);
      setOpen(false);
      setEditing(null);
      setForm(empty);
      load();
    } catch (e) { setError(messageFrom(e)); }
  };

  const remove = async project => {
    if (!window.confirm(`Delete "${project.name}" and all of its tasks?`)) return;
    try { await deleteProject(project.id); load(); }
    catch (e) { setError(messageFrom(e)); }
  };

  return (
    <>
      <div className="page-head">
        <div><span className="eyebrow">PROJECT PORTFOLIO</span><h1>Projects</h1><p>Organize related tasks into visible delivery spaces.</p></div>
        {canManage && <button className="primary-btn" onClick={openCreate}>New project</button>}
      </div>
      {error && <div className="form-error">{error}</div>}
      <div className="project-grid">
        {items.map(project => (
          <article className="project-card" key={project.id}>
            <div className="project-accent" style={{ background: project.color }} />
            <div className="project-top"><StatusPill value={project.status} /><span>{project.taskCount} tasks</span></div>
            <h3>{project.name}</h3>
            <p>{project.description || 'No description'}</p>
            <div className="project-footer">
              <small>Owner: {project.createdByName}</small>
              <div>
                <Link className="link-btn" to={`/app/projects/${project.id}`}>Open board</Link>
                {canManage && <button className="link-btn" onClick={() => openEdit(project)}>Edit</button>}
                {canManage && <button className="danger-link" onClick={() => remove(project)}>Delete</button>}
              </div>
            </div>
          </article>
        ))}
        {!items.length && <div className="empty-state"><b>No projects yet</b><span>{canManage ? 'Create the first project to start the workflow.' : 'No projects are available yet.'}</span></div>}
      </div>
      {open && (
        <Modal title={editing ? 'Edit project' : 'Create project'} onClose={() => setOpen(false)}>
          <form className="modal-form" onSubmit={submit}>
            <label>Project name<input maxLength="160" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required /></label>
            <label>Description<textarea rows="4" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} /></label>
            <div className="form-row">
              <label>Status<select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}><option>PLANNING</option><option>ACTIVE</option><option>ON_HOLD</option><option>COMPLETED</option></select></label>
              <label>Accent color<input type="color" value={form.color} onChange={e => setForm({ ...form, color: e.target.value })} /></label>
            </div>
            <button className="primary-btn">{editing ? 'Save changes' : 'Create project'}</button>
          </form>
        </Modal>
      )}
    </>
  );
}
