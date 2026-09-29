package com.taskflow.controller;

import com.taskflow.dto.UserResponse;
import com.taskflow.model.Role;
import com.taskflow.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service = service; }

    @GetMapping
    public List<UserResponse> all() { return service.allUsers(); }

    @PatchMapping("/{id}/role")
    public UserResponse setRole(@PathVariable Long id, @RequestParam Role role) {
        return service.setRole(id, role);
    }

    @PatchMapping("/{id}/active")
    public UserResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
        return service.setActive(id, active);
    }
}
