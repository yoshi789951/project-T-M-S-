package com.taskflow.service;

import com.taskflow.dto.TagResponse;
import com.taskflow.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {
    private final TagRepository tags;

    public TagService(TagRepository tags) { this.tags = tags; }

    @Transactional(readOnly = true)
    public List<TagResponse> all() {
        return tags.findAllByOrderByTitleAsc().stream()
                .map(t -> new TagResponse(t.getId(), t.getTitle(), t.getSlug(), t.getColor()))
                .toList();
    }
}
