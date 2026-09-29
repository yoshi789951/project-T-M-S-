import { useEffect, useState } from 'react';
import { getDashboard, getMyTasks, getTasks } from '../api/taskApi.js';
import { getPlans } from '../api/planningApi.js';
import { getUpcomingReminders } from '../api/reminderApi.js';
import { messageFrom } from '../api/client.js';
import { useAuthStore } from '../store/authStore.js';
import StatCard from '../components/StatCard.jsx';
import StatusPill from '../components/StatusPill.jsx';

export default function DashboardPage() {
  const user = useAuthStore(s => s.user);
  const [dashboard, setDashboard] = useState({});
  const [tasks, setTasks] = useState([]);
  const [plans, setPlans] = useState([]);
  const [reminders, setReminders] = useState([]);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      setError('');
      const [dashboardData, taskData, planData, reminderData] = await Promise.all([
        getDashboard(),
        user.role === 'USER' ? getMyTasks() : getTasks({}),
        getPlans(),
        getUpcomingReminders()
      ]);
      setDashboard(dashboardData);
      setTasks(taskData.slice(0, 6));
      setPlans(planData.slice(0, 4));
      setReminders(reminderData.slice(0, 4));
    } catch (e) { setError(messageFrom(e)); }
  };

  useEffect(() => {
    load();
    const handler = () => load();
    window.addEventListener('taskflow:event', handler);
    return () => window.removeEventListener('taskflow:event', handler);
  }, [user.role]);

  const workflow = [
    ['TODO', dashboard.todo], ['IN PROGRESS', dashboard.inProgress], ['REVIEW', dashboard.review], ['DONE', dashboard.done]
  ];
  const maxWorkflow = Math.max(1, ...workflow.map(([, value]) => Number(value || 0)));

  return (
    <>
      <div className="page-head">
        <div><span className="eyebrow">OVERVIEW</span><h1>Work at a glance</h1><p>{user.role === 'USER' ? 'Your assigned workload, plans and reminders.' : 'Team delivery, workflow, planning and deadlines.'}</p></div>
      </div>
      {error && <div className="form-error">{error}</div>}

      <div className="stat-grid">
        <StatCard label="Projects" value={dashboard.totalProjects ?? 0} meta="active workspace" />
        <StatCard label="Tasks" value={dashboard.totalTasks ?? 0} meta="in current scope" accent="cyan" />
        <StatCard label="In progress" value={dashboard.inProgress ?? 0} meta="currently moving" accent="gold" />
        <StatCard label="Overdue" value={dashboard.overdue ?? 0} meta="needs attention" accent="red" />
      </div>

      <div className="dashboard-grid">
        <section className="panel">
          <div className="panel-head"><h3>Workflow</h3><span>Live totals</span></div>
          <div className="workflow-bars">
            {workflow.map(([label, value]) => <div key={label}><span>{label}</span><div className="bar"><i style={{ width: `${Math.max(4, (Number(value || 0) / maxWorkflow) * 100)}%` }} /></div><b>{value || 0}</b></div>)}
          </div>
        </section>

        <section className="panel">
          <div className="panel-head"><h3>Recent tasks</h3><span>{tasks.length} shown</span></div>
          <div className="compact-list">
            {tasks.map(task => <div key={task.id}><div><b>{task.title}</b><small>{task.projectName} • {task.assigneeName || 'Unassigned'}</small></div><StatusPill value={task.status} /></div>)}
            {!tasks.length && <small className="empty-note">No tasks in this scope.</small>}
          </div>
        </section>

        <section className="panel">
          <div className="panel-head"><h3>Planning</h3><span>{plans.length} shown</span></div>
          <div className="compact-list">
            {plans.map(plan => <div key={plan.id}><div><b>{plan.title}</b><small>{plan.itemCount || 0} items • {plan.progressPercent || 0}% complete</small></div><StatusPill value={plan.status} /></div>)}
            {!plans.length && <small className="empty-note">No plans yet.</small>}
          </div>
        </section>

        <section className="panel">
          <div className="panel-head"><h3>Upcoming reminders</h3><span>{reminders.length} shown</span></div>
          <div className="compact-list">
            {reminders.map(reminder => <div key={reminder.id}><div><b>{reminder.title}</b><small>{new Date(reminder.remindAt).toLocaleString()}</small></div><StatusPill value={reminder.status} /></div>)}
            {!reminders.length && <small className="empty-note">No upcoming reminders.</small>}
          </div>
        </section>
      </div>
    </>
  );
}
