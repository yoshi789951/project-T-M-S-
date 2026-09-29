package com.taskflow.controller;

import com.taskflow.dto.*;
import com.taskflow.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {
    private final ReminderService service;
    public ReminderController(ReminderService service) { this.service = service; }
    @GetMapping public List<ReminderResponse> mine() { return service.mine(); }
    @GetMapping("/upcoming") public List<ReminderResponse> upcoming() { return service.upcoming(); }
    @PostMapping public ReminderResponse create(@Valid @RequestBody ReminderRequest request) { return service.create(request); }
    @PutMapping("/{id}") public ReminderResponse update(@PathVariable Long id, @Valid @RequestBody ReminderRequest request) { return service.update(id, request); }
    @PatchMapping("/{id}/status") public ReminderResponse status(@PathVariable Long id, @Valid @RequestBody ReminderStatusRequest request) { return service.updateStatus(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
