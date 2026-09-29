package com.taskflow.controller;

import com.taskflow.dto.*;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService service;
    public TaskController(TaskService service) { this.service = service; }

    @GetMapping
    public List<TaskResponse> all(@RequestParam(required = false) Long projectId) {
        return service.getAll(projectId);
    }

    @GetMapping("/mine")
    public List<TaskResponse> mine() { return service.mine(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public TaskResponse create(@Valid @RequestBody TaskRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusRequest request) {
        return service.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> comments(@PathVariable Long id) { return service.getComments(id); }

    @PostMapping("/{id}/comments")
    public CommentResponse addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        return service.addComment(id, request);
    }

    @GetMapping("/{id}/activity")
    public List<ActivityResponse> activity(@PathVariable Long id) {
        return service.getActivity(id);
    }
}
