package com.taskflow.service;

import com.taskflow.dto.PlanRequest;
import com.taskflow.dto.PlanResponse;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ForbiddenOperationException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.*;
import com.taskflow.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PlanService {
    private final PlanRepository plans;
    private final ProjectService projects;
    private final CurrentUserService currentUser;
    private final TaskEventPublisher events;

    public PlanService(PlanRepository plans, ProjectService projects, CurrentUserService currentUser, TaskEventPublisher events) {
        this.plans = plans; this.projects = projects; this.currentUser = currentUser; this.events = events;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> mine() {
        return plans.findByOwnerIdOrderByStartDateAscCreatedAtDesc(currentUser.get().getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public PlanResponse create(PlanRequest request) {
        Plan plan = new Plan();
        User user = currentUser.get();
        plan.setOwner(user); plan.setCreatedBy(user);
        apply(plan, request);
        plan = plans.save(plan);
        events.publish("PLAN_CREATED", plan.getId(), plan.getProject() == null ? null : plan.getProject().getId());
        return toResponse(plan);
    }

    @Transactional
    public PlanResponse update(Long id, PlanRequest request) {
        Plan plan = findOwned(id);
        apply(plan, request);
        plan = plans.save(plan);
        events.publish("PLAN_UPDATED", plan.getId(), plan.getProject() == null ? null : plan.getProject().getId());
        return toResponse(plan);
    }

    @Transactional
    public void delete(Long id) {
        Plan plan = findOwned(id);
        plans.delete(plan);
        events.publish("PLAN_DELETED", id, plan.getProject() == null ? null : plan.getProject().getId());
    }

    public Plan findOwned(Long id) {
        Plan plan = plans.findById(id).orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + id));
        User user = currentUser.get();
        if (!plan.getOwner().getId().equals(user.getId()) && user.getRole() != Role.ADMIN)
            throw new ForbiddenOperationException("You cannot access this plan");
        return plan;
    }

    private void apply(Plan plan, PlanRequest r) {
        if (r.startDate() != null && r.endDate() != null && r.endDate().isBefore(r.startDate()))
            throw new BadRequestException("Plan end date cannot be before start date");
        plan.setTitle(r.title().trim());
        plan.setDescription(r.description() == null ? null : r.description().trim());
        plan.setProject(r.projectId() == null ? null : projects.find(r.projectId()));
        plan.setStatus(r.status() == null ? PlanStatus.ACTIVE : r.status());
        plan.setPriority(r.priority() == null ? Priority.MEDIUM : r.priority());
        plan.setStartDate(r.startDate());
        plan.setEndDate(r.endDate());
        plan.setProgressPercent(r.progressPercent() == null ? 0 : r.progressPercent());
    }

    private PlanResponse toResponse(Plan p) {
        return new PlanResponse(p.getId(), p.getTitle(), p.getDescription(),
                p.getProject() == null ? null : p.getProject().getId(),
                p.getProject() == null ? null : p.getProject().getName(),
                p.getStatus(), p.getPriority(), p.getStartDate(), p.getEndDate(), p.getProgressPercent(),
                p.getItems().size(), p.getOwner().getId(), p.getOwner().getName(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
