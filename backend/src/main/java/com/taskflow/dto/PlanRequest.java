package com.taskflow.dto;

import com.taskflow.model.PlanStatus;
import com.taskflow.model.Priority;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PlanRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 4000) String description,
        Long projectId,
        PlanStatus status,
        Priority priority,
        LocalDate startDate,
        LocalDate endDate,
        @Min(0) @Max(100) Integer progressPercent
) {}
