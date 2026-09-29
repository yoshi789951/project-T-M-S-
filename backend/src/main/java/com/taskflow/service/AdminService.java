package com.taskflow.service;

import com.taskflow.dto.UserResponse;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.Role;
import com.taskflow.model.User;
import com.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final UserRepository users;
    private final UserService userService;
    private final CurrentUserService currentUser;
    private final TaskEventPublisher events;

    public AdminService(UserRepository users, UserService userService, CurrentUserService currentUser, TaskEventPublisher events) {
        this.users = users;
        this.userService = userService;
        this.currentUser = currentUser;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> allUsers() {
        return users.findAllByOrderByCreatedAtDesc().stream().map(userService::toResponse).toList();
    }

    @Transactional
    public UserResponse setRole(Long id, Role role) {
        if (role == null) throw new BadRequestException("role is required");
        User target = find(id);
        User current = currentUser.get();
        if (target.getId().equals(current.getId()) && role != Role.ADMIN)
            throw new BadRequestException("You cannot remove your own ADMIN role");
        target.setRole(role);
        target = users.save(target);
        events.publish("USER_ROLE_CHANGED", target.getId(), null);
        return userService.toResponse(target);
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active) {
        User target = find(id);
        User current = currentUser.get();
        if (target.getId().equals(current.getId()) && !active)
            throw new BadRequestException("You cannot suspend your own account");
        target.setActive(active);
        target = users.save(target);
        events.publish(active ? "USER_ACTIVATED" : "USER_SUSPENDED", target.getId(), null);
        return userService.toResponse(target);
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
