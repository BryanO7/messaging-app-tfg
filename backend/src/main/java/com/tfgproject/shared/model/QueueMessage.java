// src/main/java/com/tfgproject/shared/model/QueueMessage.java
package com.tfgproject.shared.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueMessage {
    private String id;
    private String type; // "EMAIL", "SMS", "BROADCAST"
    private String content;
    private String subject;
    private List<String> recipients;
    private String sender;
    private String attachmentPath;
    private boolean isHtml;
    private LocalDateTime scheduledTime;
    private LocalDateTime createdAt;
    private int retryCount;
    private String userId;

    // Constructor para email único
    public static QueueMessage forEmail(String to, String subject, String content) {
        QueueMessage msg = new QueueMessage();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setType("EMAIL");
        msg.setRecipients(List.of(to));
        msg.setSubject(subject);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setRetryCount(0);
        return msg;
    }

    // Constructor para SMS único
    public static QueueMessage forSms(String to, String content, String sender) {
        QueueMessage msg = new QueueMessage();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setType("SMS");
        msg.setRecipients(List.of(to));
        msg.setContent(content);
        msg.setSender(sender);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setRetryCount(0);
        return msg;
    }

    // Constructor para difusión
    public static QueueMessage forBroadcast(List<String> recipients, String content, String subject) {
        QueueMessage msg = new QueueMessage();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setType("BROADCAST");
        msg.setRecipients(recipients);
        msg.setContent(content);
        msg.setSubject(subject);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setRetryCount(0);
        return msg;
    }
}