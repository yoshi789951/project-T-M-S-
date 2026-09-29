package com.taskflow.dto;

import com.taskflow.model.ReminderRepeat;
import com.taskflow.model.ReminderStatus;
import java.time.LocalDateTime;

public record ReminderResponse(
        Long id, String title, String message, LocalDateTime remindAt,
        ReminderRepeat repeatType, ReminderStatus status, boolean read,
        Long taskId, String taskTitle, Long planId, String planTitle,
        Long userId, String userName, LocalDateTime createdAt, LocalDateTime updatedAt
) {}
