package com.taskflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "task_meta", uniqueConstraints = @UniqueConstraint(name = "uq_task_meta", columnNames = {"task_id", "meta_key"}))
public class TaskMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "meta_key", nullable = false, length = 50)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String content;

    public Long getId() { return id; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
