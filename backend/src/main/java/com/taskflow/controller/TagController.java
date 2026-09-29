package com.taskflow.controller;

import com.taskflow.dto.TagResponse;
import com.taskflow.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagService service;

    public TagController(TagService service) { this.service = service; }

    @GetMapping
    public List<TagResponse> all() { return service.all(); }
}
