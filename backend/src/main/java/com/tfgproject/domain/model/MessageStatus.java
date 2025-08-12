package com.tfgproject.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatusEnum status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String errorMessage;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String type; // EMAIL, SMS, BROADCAST

    private String subject;

    @Column(length = 2000)
    private String content;

    private String userId; // Para filtrar por usuario

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // Métodos de conveniencia
    public boolean isSuccess() {
        return status == MessageStatusEnum.SENT || status == MessageStatusEnum.DELIVERED;
    }

    public boolean isFailed() {
        return status == MessageStatusEnum.FAILED;
    }

    public boolean isPending() {
        return status == MessageStatusEnum.QUEUED || status == MessageStatusEnum.PROCESSING;
    }

    // Factory methods
    public static MessageStatus create(String messageId, String recipient, String type) {
        MessageStatus status = new MessageStatus();
        status.setMessageId(messageId);
        status.setRecipient(recipient);
        status.setType(type);
        status.setStatus(MessageStatusEnum.QUEUED);
        status.setTimestamp(LocalDateTime.now());
        return status;
    }

    public void updateStatus(MessageStatusEnum newStatus, String errorMessage) {
        this.status = newStatus;
        this.timestamp = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
}
