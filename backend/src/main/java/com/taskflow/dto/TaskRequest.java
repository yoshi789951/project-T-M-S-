package com.taskflow.dto;

import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record TaskRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 10000) String description,
        @NotNull Long projectId,
        Long assigneeId,
        Priority priority,
        TaskStatus status,
        LocalDate dueDate,
        @DecimalMin("0.0") BigDecimal estimatedHours,
        @DecimalMin("0.0") BigDecimal actualHours,
        LocalDateTime plannedStartAt,
        LocalDateTime plannedEndAt,
        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt,
        @Size(max = 20000) String content,
        Set<Long> tagIds
) {}
