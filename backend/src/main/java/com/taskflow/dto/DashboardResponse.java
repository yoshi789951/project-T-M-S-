package com.taskflow.dto;

public record DashboardResponse(long totalProjects, long totalTasks, long todo, long inProgress,
                                long review, long done, long overdue) {}
