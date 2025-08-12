package com.tfgproject.domain.port.out;

import com.tfgproject.domain.model.MessageStatus;
import com.tfgproject.domain.model.MessageStatusEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageStatusRepositoryPort {
    MessageStatus save(MessageStatus messageStatus);
    Optional<MessageStatus> findByMessageId(String messageId);
    List<MessageStatus> findByRecipient(String recipient);
    List<MessageStatus> findByStatus(MessageStatusEnum status);
    List<MessageStatus> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<MessageStatus> findByUserIdOrderByTimestampDesc(String userId);
    void deleteByMessageId(String messageId);
    long countByStatus(MessageStatusEnum status);
}
