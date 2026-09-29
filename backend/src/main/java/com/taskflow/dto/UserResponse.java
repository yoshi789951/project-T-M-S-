package com.taskflow.dto;

import com.taskflow.model.Role;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean active,
        LocalDateTime registeredAt,
        LocalDateTime lastLogin,
        LocalDateTime createdAt
) {}
