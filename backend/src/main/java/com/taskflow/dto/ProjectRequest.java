package com.taskflow.dto;

import com.taskflow.model.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 5000) String description,
        ProjectStatus status,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "color must be a hex value such as #2563eb") String color
) {}
