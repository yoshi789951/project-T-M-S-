package com.taskflow.service;

import com.taskflow.dto.UserResponse;
import com.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) { this.users = users; }

    @Transactional(readOnly = true)
    public List<UserResponse> activeUsers() {
        return users.findByActiveTrueOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    public UserResponse toResponse(com.taskflow.model.User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isActive(), u.getRegisteredAt(), u.getLastLogin(), u.getCreatedAt());
    }
}
