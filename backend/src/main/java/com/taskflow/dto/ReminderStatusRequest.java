package com.taskflow.dto;

import com.taskflow.model.ReminderStatus;
import jakarta.validation.constraints.NotNull;

public record ReminderStatusRequest(@NotNull ReminderStatus status, Boolean read) {}
