package com.taskflow.service;

import com.taskflow.dto.*;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ForbiddenOperationException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.*;
import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.TagRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {
    private final TaskRepository tasks;
    private final CommentRepository comments;
    private final UserRepository users;
    private final TagRepository tags;
    private final ProjectService projectService;
    private final CurrentUserService currentUser;
    private final TaskEventPublisher events;
    private final ActivityLogService activity;

    public TaskService(
            TaskRepository tasks,
            CommentRepository comments,
            UserRepository users,
            TagRepository tags,
            ProjectService projectService,
            CurrentUserService currentUser,
            TaskEventPublisher events,
            ActivityLogService activity
    ) {
        this.tasks = tasks;
        this.comments = comments;
        this.users = users;
        this.tags = tags;
        this.projectService = projectService;
        this.currentUser = currentUser;
        this.events = events;
        this.activity = activity;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAll(Long projectId) {
        List<Task> list = projectId == null
                ? tasks.findAllByOrderByCreatedAtDesc()
                : tasks.findByProjectIdOrderByCreatedAtDesc(projectId);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> mine() {
        return tasks.findByAssigneeIdOrderByDueDateAscCreatedAtDesc(currentUser.get().getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        apply(task, request);
        task.setCreatedBy(currentUser.get());
        task = tasks.save(task);
        activity.taskEvent("Task created", "Created task: " + task.getTitle(), task, currentUser.get());
        events.publish("TASK_CREATED", task.getId(), task.getProject().getId());
        return toResponse(task);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = find(id);
        Long oldProjectId = task.getProject().getId();
        apply(task, request);
        task = tasks.save(task);
        activity.taskEvent("Task updated", "Updated task details", task, currentUser.get());
        events.publish("TASK_UPDATED", task.getId(), task.getProject().getId());
        if (!oldProjectId.equals(task.getProject().getId())) {
            events.publish("PROJECT_UPDATED", oldProjectId, oldProjectId);
        }
        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatusRequest request) {
        Task task = find(id);
        User current = currentUser.get();
        boolean manager = current.getRole() == Role.ADMIN || current.getRole() == Role.MANAGER;
        boolean assignee = task.getAssignee() != null && task.getAssignee().getId().equals(current.getId());
        if (!manager && !assignee) {
            throw new ForbiddenOperationException("Only managers/admins or the assigned user can change task status");
        }

        task.setStatus(request.status());
        if (request.status() == TaskStatus.IN_PROGRESS && task.getActualStartAt() == null) {
            task.setActualStartAt(java.time.LocalDateTime.now());
        }
        if (request.status() == TaskStatus.DONE) {
            task.setActualEndAt(java.time.LocalDateTime.now());
        } else {
            task.setActualEndAt(null);
        }

        task = tasks.save(task);
        activity.taskEvent("Task status changed", "Status changed to " + task.getStatus().name(), task, current);
        events.publish("TASK_STATUS_CHANGED", task.getId(), task.getProject().getId());
        return toResponse(task);
    }

    @Transactional
    public void delete(Long id) {
        Task task = find(id);
        Long projectId = task.getProject().getId();
        tasks.delete(task);
        events.publish("TASK_DELETED", id, projectId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long taskId) {
        find(taskId);
        return comments.findByTaskIdOrderByCreatedAtAsc(taskId).stream().map(this::toComment).toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivity(Long taskId) {
        find(taskId);
        return activity.forTask(taskId);
    }

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request) {
        Task task = find(taskId);
        Comment comment = new Comment();
        comment.setTask(task);
        User actor = currentUser.get();
        comment.setAuthor(actor);
        comment.setTitle("Comment");
        comment.setBody(request.body().trim());
        comment = comments.save(comment);
        ActivityLog activityLog = activity.taskEvent("Comment added", "A comment was added to the task", task, actor);
        comment.setActivity(activityLog);
        comment = comments.save(comment);
        events.publish("COMMENT_ADDED", taskId, task.getProject().getId());
        return toComment(comment);
    }

    private void apply(Task task, TaskRequest r) {
        if (r.plannedStartAt() != null && r.plannedEndAt() != null && r.plannedEndAt().isBefore(r.plannedStartAt())) {
            throw new BadRequestException("Planned end time cannot be before planned start time");
        }
        if (r.actualStartAt() != null && r.actualEndAt() != null && r.actualEndAt().isBefore(r.actualStartAt())) {
            throw new BadRequestException("Actual end time cannot be before actual start time");
        }

        task.setTitle(r.title().trim());
        task.setDescription(trimToNull(r.description()));
        task.setProject(projectService.find(r.projectId()));
        task.setAssignee(resolveAssignee(r.assigneeId()));
        task.setPriority(r.priority() == null ? Priority.MEDIUM : r.priority());
        task.setStatus(r.status() == null ? TaskStatus.TODO : r.status());
        task.setDueDate(r.dueDate());
        task.setEstimatedHours(r.estimatedHours() == null ? BigDecimal.ZERO : r.estimatedHours());
        task.setActualHours(r.actualHours() == null ? BigDecimal.ZERO : r.actualHours());
        task.setPlannedStartAt(r.plannedStartAt());
        task.setPlannedEndAt(r.plannedEndAt());
        task.setActualStartAt(r.actualStartAt());
        task.setActualEndAt(r.actualEndAt());
        task.setContent(trimToNull(r.content()));
        task.setTags(resolveTags(r.tagIds()));
    }

    private User resolveAssignee(Long id) {
        if (id == null) return null;
        User user = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Assignee not found: " + id));
        if (!user.isActive()) throw new ForbiddenOperationException("Cannot assign a task to a suspended user");
        return user;
    }

    private Set<Tag> resolveTags(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new LinkedHashSet<>();
        List<Tag> found = tags.findAllById(ids);
        if (found.size() != ids.size()) throw new ResourceNotFoundException("One or more tags were not found");
        return new LinkedHashSet<>(found);
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Task find(Long id) {
        return tasks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    public TaskResponse toResponse(Task task) {
        User assignee = task.getAssignee();
        List<TagResponse> tagResponses = task.getTags().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getTitle(), tag.getSlug(), tag.getColor()))
                .toList();

        return new TaskResponse(
                task.getId(), task.getTitle(), task.getDescription(),
                task.getProject().getId(), task.getProject().getName(),
                assignee == null ? null : assignee.getId(), assignee == null ? null : assignee.getName(),
                task.getPriority(), task.getStatus(), task.getDueDate(),
                task.getEstimatedHours(), task.getActualHours(),
                task.getPlannedStartAt(), task.getPlannedEndAt(), task.getActualStartAt(), task.getActualEndAt(),
                task.getContent(), tagResponses,
                task.getCreatedBy().getId(), task.getCreatedBy().getName(),
                task.getCreatedAt(), task.getUpdatedAt()
        );
    }

    private CommentResponse toComment(Comment comment) {
        return new CommentResponse(
                comment.getId(), comment.getTask().getId(), comment.getAuthor().getId(),
                comment.getAuthor().getName(), comment.getBody(), comment.getCreatedAt()
        );
    }
}
