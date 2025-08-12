package com.tfgproject.infrastructure.adapter.out.persistence;

import com.tfgproject.domain.model.MessageStatus;
import com.tfgproject.domain.model.MessageStatusEnum;
import com.tfgproject.domain.port.out.MessageStatusRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class MessageStatusAdapter implements MessageStatusRepositoryPort {

    @Autowired
    private MessageStatusRepository repository;

    @Override
    public MessageStatus save(MessageStatus messageStatus) {
        return repository.save(messageStatus);
    }

    @Override
    public Optional<MessageStatus> findByMessageId(String messageId) {
        return repository.findByMessageId(messageId);
    }

    @Override
    public List<MessageStatus> findByRecipient(String recipient) {
        return repository.findByRecipient(recipient);
    }

    @Override
    public List<MessageStatus> findByStatus(MessageStatusEnum status) {
        return repository.findByStatus(status);
    }

    @Override
    public List<MessageStatus> findByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return repository.findByTimestampBetween(start, end);
    }

    @Override
    public List<MessageStatus> findByUserIdOrderByTimestampDesc(String userId) {
        return repository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Override
    public void deleteByMessageId(String messageId) {
        repository.deleteByMessageId(messageId);
    }

    @Override
    public long countByStatus(MessageStatusEnum status) {
        return repository.countByStatus(status);
    }
}