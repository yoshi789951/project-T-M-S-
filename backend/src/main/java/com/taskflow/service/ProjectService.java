package com.taskflow.service;

import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.Project;
import com.taskflow.model.ProjectStatus;
import com.taskflow.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projects;
    private final CurrentUserService currentUser;
    private final TaskEventPublisher events;

    public ProjectService(ProjectRepository projects, CurrentUserService currentUser, TaskEventPublisher events) {
        this.projects = projects;
        this.currentUser = currentUser;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAll() {
        return projects.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(Long id) { return toResponse(find(id)); }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        Project p = new Project();
        apply(p, request);
        p.setCreatedBy(currentUser.get());
        p = projects.save(p);
        events.publish("PROJECT_CREATED", p.getId(), p.getId());
        return toResponse(p);
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project p = find(id);
        apply(p, request);
        p = projects.save(p);
        events.publish("PROJECT_UPDATED", p.getId(), p.getId());
        return toResponse(p);
    }

    @Transactional
    public void delete(Long id) {
        Project p = find(id);
        projects.delete(p);
        events.publish("PROJECT_DELETED", id, id);
    }

    private void apply(Project p, ProjectRequest r) {
        p.setName(r.name().trim());
        p.setDescription(r.description() == null ? null : r.description().trim());
        p.setStatus(r.status() == null ? ProjectStatus.ACTIVE : r.status());
        p.setColor(r.color() == null || r.color().isBlank() ? "#2563eb" : r.color());
    }

    public Project find(Long id) {
        return projects.findById(id).orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    public ProjectResponse toResponse(Project p) {
        return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getStatus(), p.getColor(),
                p.getTasks().size(), p.getCreatedBy().getId(), p.getCreatedBy().getName(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
