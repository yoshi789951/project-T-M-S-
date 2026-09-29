package com.taskflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_user", columnList = "user_id"),
        @Index(name = "idx_activity_task", columnList = "task_id"),
        @Index(name = "idx_activity_project", columnList = "project_id"),
        @Index(name = "idx_activity_created_at", columnList = "created_at")
})
public class ActivityLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    @Column(nullable = false, length = 512)
    private String title;
    @Column(length = 2048) private String description;
    @Column(nullable = false, length = 30) private String status = "OPEN";
    @Column(nullable = false, precision = 8, scale = 2) private BigDecimal hours = BigDecimal.ZERO;
    private LocalDateTime plannedStartAt;
    private LocalDateTime plannedEndAt;
    private LocalDateTime actualStartAt;
    private LocalDateTime actualEndAt;
    @Column(columnDefinition = "TEXT") private String content;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void onCreate(){ createdAt=LocalDateTime.now(); }
    @PreUpdate void onUpdate(){ updatedAt=LocalDateTime.now(); }

    public Long getId(){return id;}
    public User getUser(){return user;} public void setUser(User v){user=v;}
    public Task getTask(){return task;} public void setTask(Task v){task=v;}
    public Project getProject(){return project;} public void setProject(Project v){project=v;}
    public User getCreatedBy(){return createdBy;} public void setCreatedBy(User v){createdBy=v;}
    public User getUpdatedBy(){return updatedBy;} public void setUpdatedBy(User v){updatedBy=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public BigDecimal getHours(){return hours;} public void setHours(BigDecimal v){hours=v;}
    public LocalDateTime getPlannedStartAt(){return plannedStartAt;} public void setPlannedStartAt(LocalDateTime v){plannedStartAt=v;}
    public LocalDateTime getPlannedEndAt(){return plannedEndAt;} public void setPlannedEndAt(LocalDateTime v){plannedEndAt=v;}
    public LocalDateTime getActualStartAt(){return actualStartAt;} public void setActualStartAt(LocalDateTime v){actualStartAt=v;}
    public LocalDateTime getActualEndAt(){return actualEndAt;} public void setActualEndAt(LocalDateTime v){actualEndAt=v;}
    public String getContent(){return content;} public void setContent(String v){content=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
