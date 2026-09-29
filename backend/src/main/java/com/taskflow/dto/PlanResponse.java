package com.taskflow.dto;

import com.taskflow.model.PlanStatus;
import com.taskflow.model.Priority;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PlanResponse(
        Long id,
        String title,
        String description,
        Long projectId,
        String projectName,
        PlanStatus status,
        Priority priority,
        LocalDate startDate,
        LocalDate endDate,
        Integer progressPercent,
        int itemCount,
        Long userId,
        String userName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
