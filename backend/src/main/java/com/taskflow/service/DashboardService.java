package com.taskflow.service;

import com.taskflow.dto.DashboardResponse;
import com.taskflow.model.Role;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.User;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DashboardService {
    private final ProjectRepository projects;
    private final TaskRepository tasks;
    private final CurrentUserService currentUser;

    public DashboardService(ProjectRepository projects, TaskRepository tasks, CurrentUserService currentUser) {
        this.projects = projects;
        this.tasks = tasks;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public DashboardResponse get() {
        User user = currentUser.get();
        if (user.getRole() == Role.USER) {
            long uid = user.getId();
            return new DashboardResponse(
                    tasks.countDistinctProjectsByAssigneeId(uid),
                    tasks.countByAssigneeId(uid),
                    tasks.countByAssigneeIdAndStatus(uid, TaskStatus.TODO),
                    tasks.countByAssigneeIdAndStatus(uid, TaskStatus.IN_PROGRESS),
                    tasks.countByAssigneeIdAndStatus(uid, TaskStatus.REVIEW),
                    tasks.countByAssigneeIdAndStatus(uid, TaskStatus.DONE),
                    tasks.countByAssigneeIdAndDueDateBeforeAndStatusNot(uid, LocalDate.now(), TaskStatus.DONE)
            );
        }
        return new DashboardResponse(
                projects.count(),
                tasks.count(),
                tasks.countByStatus(TaskStatus.TODO),
                tasks.countByStatus(TaskStatus.IN_PROGRESS),
                tasks.countByStatus(TaskStatus.REVIEW),
                tasks.countByStatus(TaskStatus.DONE),
                tasks.countByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE)
        );
    }
}
