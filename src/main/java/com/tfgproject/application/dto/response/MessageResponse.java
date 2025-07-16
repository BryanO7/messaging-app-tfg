package com.tfgproject.application.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private boolean success;
    private String message;
    private String messageId;
    private LocalDateTime timestamp;

    // Para difusión
    private List<String> recipients;
    private Integer recipientCount;

    // Para programación
    private LocalDateTime scheduledTime;

    // Para status
    private String status; // "QUEUED", "PROCESSING", "SENT", "FAILED"

    // Métodos de conveniencia para crear respuestas
    public static MessageResponse success(String message, String messageId) {
        return MessageResponse.builder()
                .success(true)
                .message(message)
                .messageId(messageId)
                .timestamp(LocalDateTime.now())
                .status("QUEUED")
                .build();
    }

    public static MessageResponse failure(String message) {
        return MessageResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status("FAILED")
                .build();
    }

    public static MessageResponse broadcast(String messageId, List<String> recipients) {
        return MessageResponse.builder()
                .success(true)
                .message("Difusión encolada exitosamente")
                .messageId(messageId)
                .recipients(recipients)
                .recipientCount(recipients.size())
                .timestamp(LocalDateTime.now())
                .status("QUEUED")
                .build();
    }

    public static MessageResponse scheduled(String messageId, LocalDateTime scheduledTime) {
        return MessageResponse.builder()
                .success(true)
                .message("Mensaje programado exitosamente")
                .messageId(messageId)
                .scheduledTime(scheduledTime)
                .timestamp(LocalDateTime.now())
                .status("SCHEDULED")
                .build();
    }
}