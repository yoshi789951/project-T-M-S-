package com.taskflow.dto;

import com.taskflow.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record PlanItemRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 4000) String description,
        Long taskId,
        TaskStatus status,
        LocalDateTime plannedStartAt,
        LocalDateTime plannedEndAt,
        Integer sortOrder
) {}
