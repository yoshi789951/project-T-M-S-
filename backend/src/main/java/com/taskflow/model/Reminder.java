package com.taskflow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders", indexes = {
        @Index(name = "idx_reminders_user", columnList = "user_id"),
        @Index(name = "idx_reminders_task", columnList = "task_id"),
        @Index(name = "idx_reminders_plan", columnList = "plan_id"),
        @Index(name = "idx_reminders_due", columnList = "remind_at,status")
})
public class Reminder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false, length = 20)
    private ReminderRepeat repeatType = ReminderRepeat.NONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }
    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; } public void setUser(User user) { this.user = user; }
    public Task getTask() { return task; } public void setTask(Task task) { this.task = task; }
    public Plan getPlan() { return plan; } public void setPlan(Plan plan) { this.plan = plan; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; } public void setMessage(String message) { this.message = message; }
    public LocalDateTime getRemindAt() { return remindAt; } public void setRemindAt(LocalDateTime remindAt) { this.remindAt = remindAt; }
    public ReminderRepeat getRepeatType() { return repeatType; } public void setRepeatType(ReminderRepeat repeatType) { this.repeatType = repeatType; }
    public ReminderStatus getStatus() { return status; } public void setStatus(ReminderStatus status) { this.status = status; }
    public boolean isRead() { return read; } public void setRead(boolean read) { this.read = read; }
    public User getCreatedBy() { return createdBy; } public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; } public LocalDateTime getUpdatedAt() { return updatedAt; }
}
