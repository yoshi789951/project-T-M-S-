package com.taskflow.dto;

import com.taskflow.model.Role;

/**
 * Authentication response returned to the React frontend.
 * Mirrors the JWT reference flow: user identity + role + message + token.
 */
public record AuthResponse(
        Long userId,
        String name,
        String email,
        Role role,
        String message,
        String token
) {}
