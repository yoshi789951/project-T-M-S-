package com.taskflow.repository;

import com.taskflow.model.PlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanItemRepository extends JpaRepository<PlanItem, Long> {
    List<PlanItem> findByPlanIdOrderBySortOrderAscIdAsc(Long planId);
}
