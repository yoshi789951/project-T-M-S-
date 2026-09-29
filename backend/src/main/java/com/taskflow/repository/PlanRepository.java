package com.taskflow.repository;

import com.taskflow.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByOwnerIdOrderByStartDateAscCreatedAtDesc(Long userId);
}
