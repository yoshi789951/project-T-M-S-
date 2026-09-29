package com.taskflow.service;

import com.taskflow.dto.ActivityResponse;
import com.taskflow.model.ActivityLog;
import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityLogService {
    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository) { this.repository = repository; }

    @Transactional
    public ActivityLog taskEvent(String title, String description, Task task, User actor) {
        ActivityLog log = new ActivityLog();
        log.setUser(task.getAssignee() == null ? actor : task.getAssignee());
        log.setTask(task);
        log.setProject(task.getProject());
        log.setCreatedBy(actor);
        log.setUpdatedBy(actor);
        log.setTitle(title);
        log.setDescription(description);
        log.setStatus(task.getStatus().name());
        log.setHours(task.getActualHours());
        log.setPlannedStartAt(task.getPlannedStartAt());
        log.setPlannedEndAt(task.getPlannedEndAt());
        log.setActualStartAt(task.getActualStartAt());
        log.setActualEndAt(task.getActualEndAt());
        log.setContent(task.getContent() == null ? task.getDescription() : task.getContent());
        return repository.save(log);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> forTask(Long taskId) {
        return repository.findByTaskIdOrderByCreatedAtDesc(taskId).stream().map(this::toResponse).toList();
    }

    private ActivityResponse toResponse(ActivityLog log) {
        return new ActivityResponse(
                log.getId(),
                log.getTask() == null ? null : log.getTask().getId(),
                log.getProject() == null ? null : log.getProject().getId(),
                log.getUser().getId(),
                log.getUser().getName(),
                log.getCreatedBy().getId(),
                log.getCreatedBy().getName(),
                log.getTitle(),
                log.getDescription(),
                log.getStatus(),
                log.getHours(),
                log.getCreatedAt()
        );
    }
}
