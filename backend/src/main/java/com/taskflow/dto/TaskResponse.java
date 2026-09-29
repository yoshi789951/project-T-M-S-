package com.taskflow.dto;

import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TaskResponse(
        Long id,
        String title,
        String description,
        Long projectId,
        String projectName,
        Long assigneeId,
        String assigneeName,
        Priority priority,
        TaskStatus status,
        LocalDate dueDate,
        BigDecimal estimatedHours,
        BigDecimal actualHours,
        LocalDateTime plannedStartAt,
        LocalDateTime plannedEndAt,
        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt,
        String content,
        List<TagResponse> tags,
        Long createdById,
        String createdByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
