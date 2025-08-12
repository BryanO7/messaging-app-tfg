package com.tfgproject.application.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueStatusResponse {

    private String status;
    private LocalDateTime timestamp;
    private Map<String, QueueInfo> queues;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueInfo {
        private String name;
        private Integer messageCount;
        private Integer consumerCount;
        private String state; // "running", "idle", "flow"
        private Long messagesReady;
        private Long messagesUnacknowledged;
    }

    public static QueueStatusResponse active() {
        return QueueStatusResponse.builder()
                .status("ACTIVE")
                .timestamp(LocalDateTime.now())
                .build();
    }
}