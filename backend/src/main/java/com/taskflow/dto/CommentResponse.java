package com.taskflow.dto;

import java.time.LocalDateTime;

public record CommentResponse(Long id, Long taskId, Long authorId, String authorName, String body, LocalDateTime createdAt) {}
