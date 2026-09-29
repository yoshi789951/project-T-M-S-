package com.taskflow.service;

import com.taskflow.dto.ReminderRequest;
import com.taskflow.dto.ReminderResponse;
import com.taskflow.dto.ReminderStatusRequest;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ForbiddenOperationException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.*;
import com.taskflow.repository.ReminderDeliveryLogRepository;
import com.taskflow.repository.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {
    private final ReminderRepository reminders;
    private final ReminderDeliveryLogRepository deliveryLogs;
    private final CurrentUserService currentUser;
    private final TaskService taskService;
    private final PlanService planService;
    private final TaskEventPublisher events;

    public ReminderService(
            ReminderRepository reminders,
            ReminderDeliveryLogRepository deliveryLogs,
            CurrentUserService currentUser,
            TaskService taskService,
            PlanService planService,
            TaskEventPublisher events
    ) {
        this.reminders = reminders;
        this.deliveryLogs = deliveryLogs;
        this.currentUser = currentUser;
        this.taskService = taskService;
        this.planService = planService;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> mine() {
        return reminders.findByUserIdOrderByRemindAtAsc(currentUser.get().getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> upcoming() {
        return reminders.findByUserIdAndStatusAndRemindAtAfterOrderByRemindAtAsc(
                        currentUser.get().getId(), ReminderStatus.PENDING, LocalDateTime.now().minusMinutes(1))
                .stream().limit(10).map(this::toResponse).toList();
    }

    @Transactional
    public ReminderResponse create(ReminderRequest request) {
        Reminder reminder = new Reminder();
        User user = currentUser.get();
        reminder.setUser(user);
        reminder.setCreatedBy(user);
        apply(reminder, request);
        reminder = reminders.save(reminder);
        logDelivery(reminder, "CREATED", null);
        events.publish("REMINDER_CREATED", reminder.getId(), projectId(reminder));
        return toResponse(reminder);
    }

    @Transactional
    public ReminderResponse update(Long id, ReminderRequest request) {
        Reminder reminder = findOwned(id);
        apply(reminder, request);
        reminder = reminders.save(reminder);
        events.publish("REMINDER_UPDATED", reminder.getId(), projectId(reminder));
        return toResponse(reminder);
    }

    @Transactional
    public ReminderResponse updateStatus(Long id, ReminderStatusRequest request) {
        Reminder reminder = findOwned(id);
        reminder.setStatus(request.status());
        if (request.read() != null) reminder.setRead(request.read());
        else if (request.status() == ReminderStatus.DISMISSED || request.status() == ReminderStatus.SENT) reminder.setRead(true);
        reminder = reminders.save(reminder);

        if (request.status() == ReminderStatus.SENT) {
            logDelivery(reminder, "SENT", LocalDateTime.now());
        } else if (request.status() == ReminderStatus.DISMISSED) {
            logDelivery(reminder, "DISMISSED", null);
        }

        events.publish("REMINDER_STATUS_CHANGED", reminder.getId(), projectId(reminder));
        return toResponse(reminder);
    }

    @Transactional
    public void delete(Long id) {
        Reminder reminder = findOwned(id);
        Long projectId = projectId(reminder);
        reminders.delete(reminder);
        events.publish("REMINDER_DELETED", id, projectId);
    }

    @Scheduled(fixedDelayString = "${app.reminders.scan-ms:30000}")
    @Transactional
    public void processDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> due = reminders.findByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc(ReminderStatus.PENDING, now);
        for (Reminder reminder : due) {
            logDelivery(reminder, "SENT", now);
            if (reminder.getRepeatType() == ReminderRepeat.NONE) {
                reminder.setStatus(ReminderStatus.SENT);
            } else {
                reminder.setRemindAt(nextOccurrence(reminder.getRemindAt(), reminder.getRepeatType(), now));
                reminder.setStatus(ReminderStatus.PENDING);
                reminder.setRead(false);
            }
            reminders.save(reminder);
            events.publish("REMINDER_DUE", reminder.getId(), projectId(reminder));
        }
    }

    private LocalDateTime nextOccurrence(LocalDateTime from, ReminderRepeat repeat, LocalDateTime now) {
        LocalDateTime next = from;
        do {
            next = switch (repeat) {
                case DAILY -> next.plusDays(1);
                case WEEKLY -> next.plusWeeks(1);
                case MONTHLY -> next.plusMonths(1);
                case NONE -> next;
            };
        } while (repeat != ReminderRepeat.NONE && !next.isAfter(now));
        return next;
    }

    private Reminder findOwned(Long id) {
        Reminder reminder = reminders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + id));
        User user = currentUser.get();
        if (!reminder.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("You cannot access this reminder");
        }
        return reminder;
    }

    private void apply(Reminder reminder, ReminderRequest request) {
        reminder.setTitle(request.title().trim());
        reminder.setMessage(trimToNull(request.message()));
        reminder.setRemindAt(request.remindAt());
        reminder.setRepeatType(request.repeatType() == null ? ReminderRepeat.NONE : request.repeatType());
        reminder.setTask(request.taskId() == null ? null : taskService.find(request.taskId()));
        reminder.setPlan(request.planId() == null ? null : planService.findOwned(request.planId()));

        if (reminder.getTask() != null && reminder.getPlan() != null
                && reminder.getPlan().getProject() != null
                && !reminder.getTask().getProject().getId().equals(reminder.getPlan().getProject().getId())) {
            throw new BadRequestException("Selected task and plan belong to different projects");
        }
    }

    private void logDelivery(Reminder reminder, String status, LocalDateTime deliveredAt) {
        ReminderDeliveryLog log = new ReminderDeliveryLog();
        log.setReminder(reminder);
        log.setDeliveryChannel("IN_APP");
        log.setDeliveryStatus(status);
        log.setDeliveredAt(deliveredAt);
        deliveryLogs.save(log);
    }

    private Long projectId(Reminder reminder) {
        if (reminder.getTask() != null) return reminder.getTask().getProject().getId();
        if (reminder.getPlan() != null && reminder.getPlan().getProject() != null) return reminder.getPlan().getProject().getId();
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ReminderResponse toResponse(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(), reminder.getTitle(), reminder.getMessage(), reminder.getRemindAt(),
                reminder.getRepeatType(), reminder.getStatus(), reminder.isRead(),
                reminder.getTask() == null ? null : reminder.getTask().getId(),
                reminder.getTask() == null ? null : reminder.getTask().getTitle(),
                reminder.getPlan() == null ? null : reminder.getPlan().getId(),
                reminder.getPlan() == null ? null : reminder.getPlan().getTitle(),
                reminder.getUser().getId(), reminder.getUser().getName(),
                reminder.getCreatedAt(), reminder.getUpdatedAt()
        );
    }
}
