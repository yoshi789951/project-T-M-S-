package com.taskflow.repository;

import com.taskflow.model.TaskMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskMetaRepository extends JpaRepository<TaskMeta, Long> {}
