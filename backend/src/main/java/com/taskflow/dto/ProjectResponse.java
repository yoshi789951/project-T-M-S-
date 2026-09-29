package com.taskflow.dto;

import com.taskflow.model.ProjectStatus;
import java.time.LocalDateTime;

public record ProjectResponse(Long id, String name, String description, ProjectStatus status, String color,
                              long taskCount, Long createdById, String createdByName,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {}
