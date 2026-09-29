import StatusPill from './StatusPill.jsx';

export default function TaskCard({ task, onStatus, onOpen, canUpdate }) {
  const overdue = task.dueDate && new Date(`${task.dueDate}T23:59:59`) < new Date() && task.status !== 'DONE';

  return (
    <article className="task-card" onDoubleClick={() => onOpen?.(task)}>
      <div className="task-top">
        <StatusPill value={task.priority} />
        <span className={overdue ? 'due overdue' : 'due'}>{task.dueDate || 'No due date'}</span>
      </div>

      <h4>{task.title}</h4>
      <p>{task.description || 'No description'}</p>

      {!!task.tags?.length && (
        <div className="tag-row">
          {task.tags.slice(0, 3).map(tag => (
            <span className="tag-chip" key={tag.id} style={{ '--tag-color': tag.color }}>{tag.title}</span>
          ))}
          {task.tags.length > 3 && <span className="tag-more">+{task.tags.length - 3}</span>}
        </div>
      )}

      <div className="task-meta">
        <span>{task.assigneeName || 'Unassigned'}</span>
        {Number(task.estimatedHours) > 0 && <span>{task.estimatedHours}h est.</span>}
        {onOpen && <button className="link-btn" type="button" onClick={() => onOpen(task)}>Details</button>}
      </div>

      {canUpdate && (
        <select className="status-select" value={task.status} onChange={e => onStatus(task, e.target.value)}>
          <option>TODO</option>
          <option>IN_PROGRESS</option>
          <option>REVIEW</option>
          <option>DONE</option>
        </select>
      )}
    </article>
  );
}
