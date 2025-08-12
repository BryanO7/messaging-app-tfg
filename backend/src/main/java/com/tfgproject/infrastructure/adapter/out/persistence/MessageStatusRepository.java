package com.tfgproject.infrastructure.adapter.out.persistence;

import com.tfgproject.domain.model.MessageStatus;
import com.tfgproject.domain.model.MessageStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, Long> {

    Optional<MessageStatus> findByMessageId(String messageId);

    List<MessageStatus> findByRecipient(String recipient);

    List<MessageStatus> findByStatus(MessageStatusEnum status);

    List<MessageStatus> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<MessageStatus> findByUserIdOrderByTimestampDesc(String userId);

    void deleteByMessageId(String messageId);

    long countByStatus(MessageStatusEnum status);

    @Query("SELECT ms FROM MessageStatus ms WHERE ms.userId = :userId AND ms.timestamp >= :since ORDER BY ms.timestamp DESC")
    List<MessageStatus> findRecentByUserId(@Param("userId") String userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(ms) FROM MessageStatus ms WHERE ms.status = :status AND ms.timestamp >= :since")
    long countByStatusSince(@Param("status") MessageStatusEnum status, @Param("since") LocalDateTime since);

    @Query("SELECT ms.status as status, COUNT(ms) as count FROM MessageStatus ms WHERE ms.timestamp >= :since GROUP BY ms.status")
    List<Object[]> getStatusStatistics(@Param("since") LocalDateTime since);
}