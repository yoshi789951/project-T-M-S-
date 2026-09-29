import { useEffect, useState } from 'react';
import {
  createPlan,
  createPlanItem,
  deletePlan,
  deletePlanItem,
  getPlanItems,
  getPlans,
  updatePlan,
  updatePlanItem
} from '../api/planningApi.js';
import { getProjects } from '../api/projectApi.js';
import { getMyTasks, getTasks } from '../api/taskApi.js';
import { messageFrom } from '../api/client.js';
import { useAuthStore } from '../store/authStore.js';
import Modal from '../components/Modal.jsx';
import StatusPill from '../components/StatusPill.jsx';

const emptyPlan = { title: '', description: '', projectId: '', status: 'ACTIVE', priority: 'MEDIUM', startDate: '', endDate: '', progressPercent: 0 };
const emptyItem = { title: '', description: '', taskId: '', status: 'TODO', plannedStartAt: '', plannedEndAt: '', sortOrder: 0 };
const localInput = value => value ? String(value).slice(0, 16) : '';

export default function PlanningPage() {
  const user = useAuthStore(s => s.user);
  const [plans, setPlans] = useState([]);
  const [projects, setProjects] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [planOpen, setPlanOpen] = useState(false);
  const [editingPlan, setEditingPlan] = useState(null);
  const [planForm, setPlanForm] = useState(emptyPlan);
  const [selectedPlan, setSelectedPlan] = useState(null);
  const [items, setItems] = useState([]);
  const [itemForm, setItemForm] = useState(emptyItem);
  const [editingItem, setEditingItem] = useState(null);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      setError('');
      const [planData, projectData, taskData] = await Promise.all([
        getPlans(),
        getProjects(),
        user.role === 'USER' ? getMyTasks() : getTasks({})
      ]);
      setPlans(planData);
      setProjects(projectData);
      setTasks(taskData);
    } catch (e) { setError(messageFrom(e)); }
  };

  useEffect(() => {
    load();
    const handler = () => load();
    window.addEventListener('taskflow:event', handler);
    return () => window.removeEventListener('taskflow:event', handler);
  }, [user.role]);

  const openCreate = () => {
    setEditingPlan(null);
    setPlanForm(emptyPlan);
    setPlanOpen(true);
  };

  const openEdit = plan => {
    setEditingPlan(plan);
    setPlanForm({
      title: plan.title || '', description: plan.description || '', projectId: plan.projectId || '',
      status: plan.status || 'ACTIVE', priority: plan.priority || 'MEDIUM',
      startDate: plan.startDate || '', endDate: plan.endDate || '', progressPercent: plan.progressPercent ?? 0
    });
    setPlanOpen(true);
  };

  const submitPlan = async e => {
    e.preventDefault();
    try {
      const payload = { ...planForm, projectId: planForm.projectId ? Number(planForm.projectId) : null, progressPercent: Number(planForm.progressPercent) };
      if (editingPlan) await updatePlan(editingPlan.id, payload);
      else await createPlan(payload);
      setPlanOpen(false);
      setEditingPlan(null);
      setPlanForm(emptyPlan);
      await load();
    } catch (e) { setError(messageFrom(e)); }
  };

  const removePlan = async plan => {
    if (!window.confirm(`Delete planning block "${plan.title}" and its checklist items?`)) return;
    try { await deletePlan(plan.id); if (selectedPlan?.id === plan.id) setSelectedPlan(null); await load(); }
    catch (e) { setError(messageFrom(e)); }
  };

  const openItems = async plan => {
    try {
      setSelectedPlan(plan);
      setItems(await getPlanItems(plan.id));
      setEditingItem(null);
      setItemForm(emptyItem);
    } catch (e) { setError(messageFrom(e)); }
  };

  const editItem = item => {
    setEditingItem(item);
    setItemForm({
      title: item.title || '', description: item.description || '', taskId: item.taskId || '', status: item.status || 'TODO',
      plannedStartAt: localInput(item.plannedStartAt), plannedEndAt: localInput(item.plannedEndAt), sortOrder: item.sortOrder ?? 0
    });
  };

  const submitItem = async e => {
    e.preventDefault();
    try {
      const payload = {
        ...itemForm,
        taskId: itemForm.taskId ? Number(itemForm.taskId) : null,
        plannedStartAt: itemForm.plannedStartAt || null,
        plannedEndAt: itemForm.plannedEndAt || null,
        sortOrder: Number(itemForm.sortOrder || 0)
      };
      if (editingItem) await updatePlanItem(selectedPlan.id, editingItem.id, payload);
      else await createPlanItem(selectedPlan.id, payload);
      setItems(await getPlanItems(selectedPlan.id));
      setEditingItem(null);
      setItemForm(emptyItem);
      await load();
    } catch (e) { setError(messageFrom(e)); }
  };

  const removeItem = async item => {
    try {
      await deletePlanItem(selectedPlan.id, item.id);
      setItems(await getPlanItems(selectedPlan.id));
      await load();
    } catch (e) { setError(messageFrom(e)); }
  };

  return (
    <>
      <div className="page-head">
        <div><span className="eyebrow">PLANNING</span><h1>Delivery plans</h1><p>Turn goals into dated plans and ordered checklist items.</p></div>
        <button className="primary-btn" onClick={openCreate}>New plan</button>
      </div>
      {error && <div className="form-error">{error}</div>}

      <div className="planning-grid">
        {plans.map(plan => (
          <article className="plan-card" key={plan.id}>
            <div className="plan-card-top"><StatusPill value={plan.status} /><StatusPill value={plan.priority} /></div>
            <h3>{plan.title}</h3>
            <p>{plan.description || 'No description'}</p>
            <div className="plan-dates"><span>{plan.startDate || 'No start'}</span><b>→</b><span>{plan.endDate || 'No end'}</span></div>
            <div className="progress-line"><div><i style={{ width: `${plan.progressPercent || 0}%` }} /></div><strong>{plan.progressPercent || 0}%</strong></div>
            <div className="plan-summary"><span>{plan.projectName || 'Personal plan'}</span><span>{plan.itemCount || 0} items</span></div>
            <footer>
              <button className="link-btn" onClick={() => openItems(plan)}>Checklist</button>
              <div><button className="secondary-btn compact" onClick={() => openEdit(plan)}>Edit</button><button className="danger-link" onClick={() => removePlan(plan)}>Delete</button></div>
            </footer>
          </article>
        ))}
        {!plans.length && <div className="empty-state"><b>No plans yet</b><span>Create a plan to organize milestones, dates and progress.</span></div>}
      </div>

      {planOpen && (
        <Modal title={editingPlan ? 'Edit plan' : 'Create plan'} onClose={() => setPlanOpen(false)}>
          <form className="modal-form" onSubmit={submitPlan}>
            <label>Plan title<input required maxLength="180" value={planForm.title} onChange={e => setPlanForm({ ...planForm, title: e.target.value })} /></label>
            <label>Description<textarea rows="4" value={planForm.description} onChange={e => setPlanForm({ ...planForm, description: e.target.value })} /></label>
            <label>Project<select value={planForm.projectId} onChange={e => setPlanForm({ ...planForm, projectId: e.target.value })}><option value="">Personal / no project</option>{projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}</select></label>
            <div className="form-row">
              <label>Status<select value={planForm.status} onChange={e => setPlanForm({ ...planForm, status: e.target.value })}><option>ACTIVE</option><option>DRAFT</option><option>ON_HOLD</option><option>COMPLETED</option><option>CANCELLED</option></select></label>
              <label>Priority<select value={planForm.priority} onChange={e => setPlanForm({ ...planForm, priority: e.target.value })}><option>LOW</option><option>MEDIUM</option><option>HIGH</option><option>URGENT</option></select></label>
            </div>
            <div className="form-row">
              <label>Start date<input type="date" value={planForm.startDate} onChange={e => setPlanForm({ ...planForm, startDate: e.target.value })} /></label>
              <label>End date<input type="date" value={planForm.endDate} onChange={e => setPlanForm({ ...planForm, endDate: e.target.value })} /></label>
            </div>
            <label>Progress %<input type="number" min="0" max="100" value={planForm.progressPercent} onChange={e => setPlanForm({ ...planForm, progressPercent: e.target.value })} /></label>
            <button className="primary-btn">{editingPlan ? 'Save changes' : 'Create plan'}</button>
          </form>
        </Modal>
      )}

      {selectedPlan && (
        <Modal title={`${selectedPlan.title} — checklist`} wide onClose={() => setSelectedPlan(null)}>
          <div className="checklist-layout">
            <div className="checklist-list">
              {items.map(item => (
                <article className="checklist-item" key={item.id}>
                  <div><StatusPill value={item.status} /><b>{item.title}</b><small>{item.taskTitle ? `Linked task: ${item.taskTitle}` : 'Standalone plan item'}</small></div>
                  <div><button className="link-btn" onClick={() => editItem(item)}>Edit</button><button className="danger-link" onClick={() => removeItem(item)}>Delete</button></div>
                </article>
              ))}
              {!items.length && <div className="empty-state small"><b>No checklist items</b><span>Add the first step for this plan.</span></div>}
            </div>
            <form className="modal-form checklist-form" onSubmit={submitItem}>
              <span className="eyebrow">{editingItem ? 'EDIT ITEM' : 'NEW ITEM'}</span>
              <label>Title<input required value={itemForm.title} onChange={e => setItemForm({ ...itemForm, title: e.target.value })} /></label>
              <label>Description<textarea rows="3" value={itemForm.description} onChange={e => setItemForm({ ...itemForm, description: e.target.value })} /></label>
              <label>Linked task<select value={itemForm.taskId} onChange={e => setItemForm({ ...itemForm, taskId: e.target.value })}><option value="">No linked task</option>{tasks.map(t => <option key={t.id} value={t.id}>{t.title}</option>)}</select></label>
              <div className="form-row"><label>Status<select value={itemForm.status} onChange={e => setItemForm({ ...itemForm, status: e.target.value })}><option>TODO</option><option>IN_PROGRESS</option><option>REVIEW</option><option>DONE</option></select></label><label>Order<input type="number" min="0" value={itemForm.sortOrder} onChange={e => setItemForm({ ...itemForm, sortOrder: e.target.value })} /></label></div>
              <div className="form-row"><label>Planned start<input type="datetime-local" value={itemForm.plannedStartAt} onChange={e => setItemForm({ ...itemForm, plannedStartAt: e.target.value })} /></label><label>Planned end<input type="datetime-local" value={itemForm.plannedEndAt} onChange={e => setItemForm({ ...itemForm, plannedEndAt: e.target.value })} /></label></div>
              <button className="primary-btn">{editingItem ? 'Save item' : 'Add item'}</button>
              {editingItem && <button type="button" className="secondary-btn" onClick={() => { setEditingItem(null); setItemForm(emptyItem); }}>Cancel edit</button>}
            </form>
          </div>
        </Modal>
      )}
    </>
  );
}
