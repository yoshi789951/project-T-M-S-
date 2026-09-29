package com.taskflow.dto;

import com.taskflow.model.TaskStatus;
import java.time.LocalDateTime;

public record PlanItemResponse(
        Long id,
        Long planId,
        Long taskId,
        String taskTitle,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime plannedStartAt,
        LocalDateTime plannedEndAt,
        LocalDateTime completedAt,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
