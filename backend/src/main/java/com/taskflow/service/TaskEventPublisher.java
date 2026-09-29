package com.taskflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TaskEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(TaskEventPublisher.class);
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final String channel;

    public TaskEventPublisher(StringRedisTemplate redis, ObjectMapper mapper,
                              @Value("${app.realtime.channel:taskflow-events}") String channel) {
        this.redis = redis;
        this.mapper = mapper;
        this.channel = channel;
    }

    public void publish(String type, Long entityId, Long projectId) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", type);
            event.put("entityId", entityId);
            if (projectId != null) event.put("projectId", projectId);
            event.put("timestamp", Instant.now().toString());
            redis.convertAndSend(channel, mapper.writeValueAsString(event));
        } catch (Exception ex) {
            // Realtime is additive; a Redis outage must not break task CRUD.
            log.warn("Could not publish realtime event {}: {}", type, ex.getMessage());
        }
    }
}
