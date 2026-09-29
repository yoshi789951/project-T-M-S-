package com.taskflow.controller;

import com.taskflow.dto.PlanItemRequest;
import com.taskflow.dto.PlanItemResponse;
import com.taskflow.service.PlanItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans/{planId}/items")
public class PlanItemController {
    private final PlanItemService service;

    public PlanItemController(PlanItemService service) { this.service = service; }

    @GetMapping
    public List<PlanItemResponse> list(@PathVariable Long planId) { return service.list(planId); }

    @PostMapping
    public PlanItemResponse create(@PathVariable Long planId, @Valid @RequestBody PlanItemRequest request) {
        return service.create(planId, request);
    }

    @PutMapping("/{itemId}")
    public PlanItemResponse update(@PathVariable Long planId, @PathVariable Long itemId, @Valid @RequestBody PlanItemRequest request) {
        return service.update(planId, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable Long planId, @PathVariable Long itemId) {
        service.delete(planId, itemId);
        return ResponseEntity.noContent().build();
    }
}
