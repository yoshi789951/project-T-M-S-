package com.taskflow.controller;

import com.taskflow.dto.*;
import com.taskflow.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
    private final PlanService service;
    public PlanController(PlanService service) { this.service = service; }
    @GetMapping public List<PlanResponse> mine() { return service.mine(); }
    @PostMapping public PlanResponse create(@Valid @RequestBody PlanRequest request) { return service.create(request); }
    @PutMapping("/{id}") public PlanResponse update(@PathVariable Long id, @Valid @RequestBody PlanRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
