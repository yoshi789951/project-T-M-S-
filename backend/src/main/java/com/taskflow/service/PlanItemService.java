package com.taskflow.service;

import com.taskflow.dto.PlanItemRequest;
import com.taskflow.dto.PlanItemResponse;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.Plan;
import com.taskflow.model.PlanItem;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.repository.PlanItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlanItemService {
    private final PlanItemRepository items;
    private final PlanService planService;
    private final TaskService taskService;
    private final TaskEventPublisher events;

    public PlanItemService(PlanItemRepository items, PlanService planService, TaskService taskService, TaskEventPublisher events) {
        this.items = items;
        this.planService = planService;
        this.taskService = taskService;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<PlanItemResponse> list(Long planId) {
        planService.findOwned(planId);
        return items.findByPlanIdOrderBySortOrderAscIdAsc(planId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public PlanItemResponse create(Long planId, PlanItemRequest request) {
        Plan plan = planService.findOwned(planId);
        PlanItem item = new PlanItem();
        item.setPlan(plan);
        apply(item, request);
        item = items.save(item);
        events.publish("PLAN_ITEM_CREATED", item.getId(), plan.getProject() == null ? null : plan.getProject().getId());
        return toResponse(item);
    }

    @Transactional
    public PlanItemResponse update(Long planId, Long itemId, PlanItemRequest request) {
        Plan plan = planService.findOwned(planId);
        PlanItem item = find(itemId);
        if (!item.getPlan().getId().equals(plan.getId())) throw new ResourceNotFoundException("Plan item not found: " + itemId);
        apply(item, request);
        item = items.save(item);
        events.publish("PLAN_ITEM_UPDATED", item.getId(), plan.getProject() == null ? null : plan.getProject().getId());
        return toResponse(item);
    }

    @Transactional
    public void delete(Long planId, Long itemId) {
        Plan plan = planService.findOwned(planId);
        PlanItem item = find(itemId);
        if (!item.getPlan().getId().equals(plan.getId())) throw new ResourceNotFoundException("Plan item not found: " + itemId);
        items.delete(item);
        events.publish("PLAN_ITEM_DELETED", itemId, plan.getProject() == null ? null : plan.getProject().getId());
    }

    private void apply(PlanItem item, PlanItemRequest request) {
        if (request.plannedStartAt() != null && request.plannedEndAt() != null && request.plannedEndAt().isBefore(request.plannedStartAt())) {
            throw new BadRequestException("Plan item end time cannot be before start time");
        }
        item.setTitle(request.title().trim());
        item.setDescription(request.description() == null ? null : request.description().trim());
        Task task = request.taskId() == null ? null : taskService.find(request.taskId());
        item.setTask(task);
        TaskStatus status = request.status() == null ? TaskStatus.TODO : request.status();
        item.setStatus(status);
        item.setPlannedStartAt(request.plannedStartAt());
        item.setPlannedEndAt(request.plannedEndAt());
        item.setSortOrder(request.sortOrder() == null ? 0 : Math.max(0, request.sortOrder()));
        if (status == TaskStatus.DONE && item.getCompletedAt() == null) item.setCompletedAt(LocalDateTime.now());
        if (status != TaskStatus.DONE) item.setCompletedAt(null);
    }

    private PlanItem find(Long id) {
        return items.findById(id).orElseThrow(() -> new ResourceNotFoundException("Plan item not found: " + id));
    }

    private PlanItemResponse toResponse(PlanItem item) {
        return new PlanItemResponse(
                item.getId(), item.getPlan().getId(),
                item.getTask() == null ? null : item.getTask().getId(),
                item.getTask() == null ? null : item.getTask().getTitle(),
                item.getTitle(), item.getDescription(), item.getStatus(),
                item.getPlannedStartAt(), item.getPlannedEndAt(), item.getCompletedAt(),
                item.getSortOrder(), item.getCreatedAt(), item.getUpdatedAt()
        );
    }
}
