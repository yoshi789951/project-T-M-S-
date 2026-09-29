import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api, messageFrom } from '../api/client.js';
import { getProject } from '../api/projectApi.js';
import { getTags } from '../api/tagsApi.js';
import {
  addComment,
  createTask,
  deleteTask,
  getComments,
  getTaskActivity,
  getTasks,
  updateTask,
  updateTaskStatus
} from '../api/taskApi.js';
import { useAuthStore } from '../store/authStore.js';
import Modal from '../components/Modal.jsx';
import StatusPill from '../components/StatusPill.jsx';
import TaskCard from '../components/TaskCard.jsx';

const columns = ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE'];
const emptyForm = {
  title: '', description: '', assigneeId: '', priority: 'MEDIUM', status: 'TODO', dueDate: '',
  estimatedHours: '', plannedStartAt: '', plannedEndAt: '', content: '', tagIds: []
};
const toLocalInput = value => value ? String(value).slice(0, 16) : '';

export default function ProjectBoardPage() {
  const { id } = useParams();
  const user = useAuthStore(s => s.user);
  const canManage = ['ADMIN', 'MANAGER'].includes(user.role);

  const [project, setProject] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [users, setUsers] = useState([]);
  const [tags, setTags] = useState([]);
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [detail, setDetail] = useState(null);
  const [comments, setComments] = useState([]);
  const [activity, setActivity] = useState([]);
  const [comment, setComment] = useState('');
  const [error, setError] = useState('');
  const [form, setForm] = useState(emptyForm);

  const load = async () => {
    try {
      setError('');
      const [projectData, taskData, tagData] = await Promise.all([
        getProject(id), getTasks({ projectId: id }), getTags()
      ]);
      setProject(projectData);
      setTasks(taskData);
      setTags(tagData);
      if (canManage) {
        try { setUsers((await api.get('/users')).data); } catch { setUsers([]); }
      }
    } catch (e) {
      setError(messageFrom(e));
    }
  };

  useEffect(() => {
    load();
    const handler = () => load();
    window.addEventListener('taskflow:event', handler);
    return () => window.removeEventListener('taskflow:event', handler);
  }, [id, canManage]);

  const groups = useMemo(
    () => Object.fromEntries(columns.map(column => [column, tasks.filter(task => task.status === column)])),
    [tasks]
  );

  const changeStatus = async (task, status) => {
    try {
      await updateTaskStatus(task.id, status);
      await load();
    } catch (e) { setError(messageFrom(e)); }
  };

  const openCreate = () => {
    setEditing(null);
    setForm(emptyForm);
    setFormOpen(true);
  };

  const openEdit = task => {
    setEditing(task);
    setForm({
      title: task.title || '',
      description: task.description || '',
      assigneeId: task.assigneeId || '',
      priority: task.priority || 'MEDIUM',
      status: task.status || 'TODO',
      dueDate: task.dueDate || '',
      estimatedHours: task.estimatedHours ?? '',
      plannedStartAt: toLocalInput(task.plannedStartAt),
      plannedEndAt: toLocalInput(task.plannedEndAt),
      content: task.content || '',
      tagIds: task.tags?.map(tag => tag.id) || []
    });
    setDetail(null);
    setFormOpen(true);
  };

  const submit = async e => {
    e.preventDefault();
    try {
      setError('');
      const payload = {
        ...form,
        projectId: Number(id),
        assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
        dueDate: form.dueDate || null,
        estimatedHours: form.estimatedHours === '' ? 0 : Number(form.estimatedHours),
        actualHours: editing?.actualHours ?? 0,
        plannedStartAt: form.plannedStartAt || null,
        plannedEndAt: form.plannedEndAt || null,
        actualStartAt: editing?.actualStartAt || null,
        actualEndAt: editing?.actualEndAt || null,
        tagIds: form.tagIds.map(Number)
      };
      if (editing) await updateTask(editing.id, payload);
      else await createTask(payload);
      setFormOpen(false);
      setEditing(null);
      setForm(emptyForm);
      await load();
    } catch (e) { setError(messageFrom(e)); }
  };

  const openTask = async task => {
    try {
      setDetail(task);
      const [commentData, activityData] = await Promise.all([getComments(task.id), getTaskActivity(task.id)]);
      setComments(commentData);
      setActivity(activityData);
    } catch (e) { setError(messageFrom(e)); }
  };

  const sendComment = async e => {
    e.preventDefault();
    if (!comment.trim()) return;
    try {
      await addComment(detail.id, comment.trim());
      setComment('');
      const [commentData, activityData] = await Promise.all([getComments(detail.id), getTaskActivity(detail.id)]);
      setComments(commentData);
      setActivity(activityData);
    } catch (e) { setError(messageFrom(e)); }
  };

  const remove = async task => {
    if (!window.confirm(`Delete task "${task.title}"? This also removes its comments and activity history.`)) return;
    try {
      await deleteTask(task.id);
      setDetail(null);
      await load();
    } catch (e) { setError(messageFrom(e)); }
  };

  const toggleTag = tagId => {
    setForm(current => ({
      ...current,
      tagIds: current.tagIds.includes(tagId)
        ? current.tagIds.filter(id => id !== tagId)
        : [...current.tagIds, tagId]
    }));
  };

  return (
    <>
      <div className="page-head">
        <div>
          <span className="eyebrow">KANBAN BOARD</span>
          <h1>{project?.name || 'Project'}</h1>
          <p>{project?.description || 'Track delivery through the complete task workflow.'}</p>
        </div>
        {canManage && <button className="primary-btn" onClick={openCreate}>Add task</button>}
      </div>

      {error && <div className="form-error">{error}</div>}

      <div className="kanban">
        {columns.map(column => (
          <section className="kanban-col" key={column}>
            <header><span>{column.replaceAll('_', ' ')}</span><b>{groups[column]?.length || 0}</b></header>
            <div>
              {groups[column]?.map(task => (
                <TaskCard
                  key={task.id}
                  task={task}
                  onStatus={changeStatus}
                  onOpen={openTask}
                  canUpdate={canManage || task.assigneeId === user.userId}
                />
              ))}
              {!groups[column]?.length && <div className="column-empty">No tasks</div>}
            </div>
          </section>
        ))}
      </div>

      {formOpen && (
        <Modal title={editing ? 'Edit task' : 'Create task'} wide onClose={() => setFormOpen(false)}>
          <form className="modal-form" onSubmit={submit}>
            <label>Title<input maxLength="180" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} required /></label>
            <label>Description<textarea rows="3" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} /></label>
            <div className="form-row">
              <label>Priority<select value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>URGENT</option></select></label>
              <label>Status<select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}><option>TODO</option><option>IN_PROGRESS</option><option>REVIEW</option><option>DONE</option></select></label>
            </div>
            <div className="form-row">
              <label>Assignee<select value={form.assigneeId} onChange={e => setForm({ ...form, assigneeId: e.target.value })}><option value="">Unassigned</option>{users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}</select></label>
              <label>Due date<input type="date" value={form.dueDate} onChange={e => setForm({ ...form, dueDate: e.target.value })} /></label>
            </div>
            <div className="form-row">
              <label>Planned start<input type="datetime-local" value={form.plannedStartAt} onChange={e => setForm({ ...form, plannedStartAt: e.target.value })} /></label>
              <label>Planned end<input type="datetime-local" value={form.plannedEndAt} onChange={e => setForm({ ...form, plannedEndAt: e.target.value })} /></label>
            </div>
            <label>Estimated hours<input type="number" min="0" step="0.25" value={form.estimatedHours} onChange={e => setForm({ ...form, estimatedHours: e.target.value })} /></label>
            <label>Work notes<textarea rows="3" value={form.content} onChange={e => setForm({ ...form, content: e.target.value })} placeholder="Technical notes, acceptance criteria, links..." /></label>
            <div className="field-group"><span>Tags</span><div className="tag-picker">{tags.map(tag => <button type="button" key={tag.id} className={`tag-chip ${form.tagIds.includes(tag.id) ? 'selected' : ''}`} style={{ '--tag-color': tag.color }} onClick={() => toggleTag(tag.id)}>{tag.title}</button>)}</div></div>
            <button className="primary-btn">{editing ? 'Save changes' : 'Create task'}</button>
          </form>
        </Modal>
      )}

      {detail && (
        <Modal title={detail.title} wide onClose={() => setDetail(null)}>
          <div className="detail-actions">
            <div className="tag-row">{detail.tags?.map(tag => <span className="tag-chip" key={tag.id} style={{ '--tag-color': tag.color }}>{tag.title}</span>)}</div>
            {canManage && <div><button className="secondary-btn" onClick={() => openEdit(detail)}>Edit</button><button className="danger-link" onClick={() => remove(detail)}>Delete</button></div>}
          </div>
          <div className="detail-grid three">
            <div>
              <span className="eyebrow">DETAILS</span>
              <p>{detail.description || 'No description.'}</p>
              {detail.content && <div className="work-notes"><b>Work notes</b><p>{detail.content}</p></div>}
              <dl>
                <div><dt>Project</dt><dd>{detail.projectName}</dd></div>
                <div><dt>Assignee</dt><dd>{detail.assigneeName || 'Unassigned'}</dd></div>
                <div><dt>Priority</dt><dd><StatusPill value={detail.priority} /></dd></div>
                <div><dt>Status</dt><dd><StatusPill value={detail.status} /></dd></div>
                <div><dt>Due date</dt><dd>{detail.dueDate || 'None'}</dd></div>
                <div><dt>Estimated</dt><dd>{detail.estimatedHours || 0} h</dd></div>
                <div><dt>Actual</dt><dd>{detail.actualHours || 0} h</dd></div>
              </dl>
            </div>
            <div className="comments">
              <span className="eyebrow">COMMENTS</span>
              <div className="comment-list">
                {comments.map(c => <article key={c.id}><b>{c.authorName}</b><p>{c.body}</p><small>{new Date(c.createdAt).toLocaleString()}</small></article>)}
                {!comments.length && <small className="empty-note">No comments yet.</small>}
              </div>
              <form onSubmit={sendComment}><textarea rows="3" value={comment} onChange={e => setComment(e.target.value)} placeholder="Write a comment..." /><button className="primary-btn">Add comment</button></form>
            </div>
            <div className="activity-panel">
              <span className="eyebrow">ACTIVITY</span>
              <div className="activity-list">
                {activity.map(entry => <article key={entry.id}><span className="activity-dot" /><div><b>{entry.title}</b><p>{entry.description}</p><small>{entry.createdByName} • {new Date(entry.createdAt).toLocaleString()}</small></div></article>)}
                {!activity.length && <small className="empty-note">No activity yet.</small>}
              </div>
            </div>
          </div>
        </Modal>
      )}
    </>
  );
}
