package com.taskflow.repository;

import com.taskflow.model.Reminder;
import com.taskflow.model.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUserIdOrderByRemindAtAsc(Long userId);
    List<Reminder> findByUserIdAndStatusAndRemindAtAfterOrderByRemindAtAsc(Long userId, ReminderStatus status, LocalDateTime after);
    List<Reminder> findByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc(ReminderStatus status, LocalDateTime at);
}
