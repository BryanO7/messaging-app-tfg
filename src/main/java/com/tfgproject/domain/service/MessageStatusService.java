package com.tfgproject.domain.service;

import com.tfgproject.domain.model.MessageStatus;
import com.tfgproject.domain.model.MessageStatusEnum;
import com.tfgproject.domain.port.out.MessageStatusRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageStatusService {

    @Autowired
    private MessageStatusRepositoryPort messageStatusRepository;

    public MessageStatus createMessageStatus(String messageId, String recipient, String type, String userId) {
        MessageStatus status = MessageStatus.create(messageId, recipient, type);
        status.setUserId(userId);
        return messageStatusRepository.save(status);
    }

    public void updateMessageStatus(String messageId, MessageStatusEnum newStatus, String errorMessage) {
        Optional<MessageStatus> statusOpt = messageStatusRepository.findByMessageId(messageId);
        if (statusOpt.isPresent()) {
            MessageStatus status = statusOpt.get();
            status.updateStatus(newStatus, errorMessage);
            messageStatusRepository.save(status);
        }
    }

    public List<MessageStatus> getUserMessageHistory(String userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return messageStatusRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public Optional<MessageStatus> getMessageStatus(String messageId) {
        return messageStatusRepository.findByMessageId(messageId);
    }

    public long getPendingMessageCount() {
        return messageStatusRepository.countByStatus(MessageStatusEnum.QUEUED) +
                messageStatusRepository.countByStatus(MessageStatusEnum.PROCESSING);
    }

    public long getFailedMessageCount() {
        return messageStatusRepository.countByStatus(MessageStatusEnum.FAILED);
    }
}