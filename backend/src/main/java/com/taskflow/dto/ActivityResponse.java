package com.taskflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ActivityResponse(
        Long id,
        Long taskId,
        Long projectId,
        Long userId,
        String userName,
        Long createdById,
        String createdByName,
        String title,
        String description,
        String status,
        BigDecimal hours,
        LocalDateTime createdAt
) {}
