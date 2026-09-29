package com.taskflow.dto;

import com.taskflow.model.ReminderRepeat;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record ReminderRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 4000) String message,
        @NotNull LocalDateTime remindAt,
        Long taskId,
        Long planId,
        ReminderRepeat repeatType
) {}
