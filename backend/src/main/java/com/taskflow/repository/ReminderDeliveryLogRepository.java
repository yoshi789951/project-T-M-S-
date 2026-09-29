package com.taskflow.repository;

import com.taskflow.model.ReminderDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderDeliveryLogRepository extends JpaRepository<ReminderDeliveryLog, Long> {}
