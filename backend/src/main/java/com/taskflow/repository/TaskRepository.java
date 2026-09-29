package com.taskflow.repository;

import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOrderByCreatedAtDesc();

    List<Task> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<Task> findByAssigneeIdOrderByDueDateAscCreatedAtDesc(Long assigneeId);

    long countByStatus(TaskStatus status);

    long countByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);

    long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

    long countByAssigneeIdAndDueDateBeforeAndStatusNot(
            Long assigneeId,
            LocalDate date,
            TaskStatus status
    );

    long countByAssigneeId(Long assigneeId);

    @Query("""
            select count(distinct t.project.id)
            from Task t
            where t.assignee.id = :assigneeId
            """)
    long countDistinctProjectsByAssigneeId(
            @Param("assigneeId") Long assigneeId
    );
}
