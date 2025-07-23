package com.tfgproject.infrastructure.adapter.in.web;

import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import com.tfgproject.infrastructure.service.AsyncScheduledMessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/async-schedule")
@CrossOrigin(origins = "*")
public class AsyncScheduleController {

    @Autowired
    private AsyncMessagePublisher asyncMessagePublisher;

    @Autowired
    private AsyncScheduledMessageProcessor scheduledProcessor;

    /**
     * Programar mensaje de forma ASÍNCRONA - NO BLOQUEA
     */
    @PostMapping("/schedule-async")
    public ResponseEntity<Map<String, Object>> scheduleMessageAsync(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestParam int minutesFromNow) {

        LocalDateTime scheduledTime = LocalDateTime.now().plusMinutes(minutesFromNow);

        // ESTE MÉTODO NO BLOQUEA - DEVUELVE INMEDIATAMENTE
        CompletableFuture<String> futureMessageId = asyncMessagePublisher
                .scheduleMessageAsync(to, subject, content, scheduledTime);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Mensaje programado asincrónicamente");
        response.put("scheduledTime", scheduledTime);
        response.put("estimatedMinutes", minutesFromNow);
        response.put("status", "PROCESSING");

        return ResponseEntity.ok(response);
    }

    /**
     * Estado del scheduler
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("scheduledCount", scheduledProcessor.getScheduledMessageCount());
        status.put("timestamp", LocalDateTime.now());
        status.put("status", "ACTIVE");

        return ResponseEntity.ok(status);
    }
}