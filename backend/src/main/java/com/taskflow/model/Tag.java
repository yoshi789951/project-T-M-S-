package com.taskflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(name = "uq_tags_slug", columnNames = "slug"))
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 75)
    private String title;

    @Column(nullable = false, length = 100, unique = true)
    private String slug;

    @Column(nullable = false, length = 20)
    private String color = "#2563eb";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
