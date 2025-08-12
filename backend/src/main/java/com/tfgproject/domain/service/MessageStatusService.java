package com.tfgproject.domain.service;

import com.tfgproject.domain.model.MessageStatus;
import com.tfgproject.domain.model.MessageStatusEnum;
import com.tfgproject.domain.model.SystemStatusReport; // ← IMPORT DE LA CLASE EXTERNA
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

    // === MÉTODOS ORIGINALES (TU CÓDIGO ACTUAL) ===

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

    // === MÉTODOS ADICIONALES PARA CASO DE USO 05 ===

    /**
     * CASO DE USO 05: Obtener mensajes por estado
     */
    public List<MessageStatus> getMessagesByStatus(MessageStatusEnum status) {
        return messageStatusRepository.findByStatus(status);
    }

    /**
     * CASO DE USO 05: Obtener mensajes por destinatario
     */
    public List<MessageStatus> getMessagesByRecipient(String recipient) {
        return messageStatusRepository.findByRecipient(recipient);
    }

    /**
     * CASO DE USO 05: Obtener mensajes en un rango de fechas
     */
    public List<MessageStatus> getMessagesBetweenDates(LocalDateTime start, LocalDateTime end) {
        return messageStatusRepository.findByTimestampBetween(start, end);
    }

    /**
     * CASO DE USO 05: Obtener último mensaje a un destinatario
     */
    public Optional<MessageStatus> getLastMessageToRecipient(String recipient) {
        List<MessageStatus> messages = getMessagesByRecipient(recipient);
        return messages.stream()
                .max((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
    }

    /**
     * CASO DE USO 05: Obtener mensajes recientes (últimas 24 horas)
     */
    public List<MessageStatus> getRecentMessages() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return getMessagesBetweenDates(yesterday, LocalDateTime.now());
    }

    /**
     * CASO DE USO 05: Contar mensajes exitosos
     */
    public long getSuccessfulMessageCount() {
        return messageStatusRepository.countByStatus(MessageStatusEnum.SENT) +
                messageStatusRepository.countByStatus(MessageStatusEnum.DELIVERED);
    }

    /**
     * CASO DE USO 05: Contar total de mensajes
     */
    public long getTotalMessageCount() {
        long total = 0;
        for (MessageStatusEnum status : MessageStatusEnum.values()) {
            total += messageStatusRepository.countByStatus(status);
        }
        return total;
    }

    /**
     * CASO DE USO 05: Calcular tasa de éxito
     */
    public double getSuccessRate() {
        long total = getTotalMessageCount();
        if (total == 0) return 0.0;

        long successful = getSuccessfulMessageCount();
        return (double) successful / total * 100.0;
    }

    /**
     * CASO DE USO 05: Estadísticas por estado
     */
    public java.util.Map<MessageStatusEnum, Long> getStatusStatistics() {
        java.util.Map<MessageStatusEnum, Long> stats = new java.util.HashMap<>();

        for (MessageStatusEnum status : MessageStatusEnum.values()) {
            long count = messageStatusRepository.countByStatus(status);
            stats.put(status, count);
        }

        return stats;
    }

    /**
     * CASO DE USO 05: Verificar si un mensaje está pendiente
     */
    public boolean isMessagePending(String messageId) {
        Optional<MessageStatus> statusOpt = getMessageStatus(messageId);
        return statusOpt.map(MessageStatus::isPending).orElse(false);
    }

    /**
     * CASO DE USO 05: Verificar si un mensaje falló
     */
    public boolean isMessageFailed(String messageId) {
        Optional<MessageStatus> statusOpt = getMessageStatus(messageId);
        return statusOpt.map(MessageStatus::isFailed).orElse(false);
    }

    /**
     * CASO DE USO 05: Eliminar mensaje por ID
     */
    public void deleteMessage(String messageId) {
        messageStatusRepository.deleteByMessageId(messageId);
    }

    /**
     * CASO DE USO 05: Crear reporte completo del sistema
     */
    public SystemStatusReport getSystemStatusReport() {
        List<MessageStatus> recentMessages = getRecentMessages();

        return SystemStatusReport.createComplete(
                getTotalMessageCount(),
                getSuccessfulMessageCount(),
                getFailedMessageCount(),
                getPendingMessageCount(),
                recentMessages.size()
        );
    }

    // === MÉTODOS DE UTILIDAD ADICIONALES ===

    /**
     * Obtener resumen rápido para dashboard
     */
    public java.util.Map<String, Object> getQuickSummary() {
        java.util.Map<String, Object> summary = new java.util.HashMap<>();

        summary.put("totalMessages", getTotalMessageCount());
        summary.put("successfulMessages", getSuccessfulMessageCount());
        summary.put("failedMessages", getFailedMessageCount());
        summary.put("pendingMessages", getPendingMessageCount());
        summary.put("successRate", Math.round(getSuccessRate() * 100.0) / 100.0);
        summary.put("recentActivity", getRecentMessages().size());
        summary.put("lastUpdated", LocalDateTime.now());

        return summary;
    }

    /**
     * Verificar salud del sistema
     */
    public String getSystemHealth() {
        SystemStatusReport report = getSystemStatusReport();
        return report.getSystemHealth().getDisplayName();
    }

    /**
     * Obtener alertas del sistema
     */
    public List<String> getSystemAlerts() {
        List<String> alerts = new java.util.ArrayList<>();

        long failedCount = getFailedMessageCount();
        long pendingCount = getPendingMessageCount();
        double successRate = getSuccessRate();

        if (failedCount > 10) {
            alerts.add("Alto número de mensajes fallidos: " + failedCount);
        }

        if (pendingCount > 50) {
            alerts.add("Muchos mensajes pendientes: " + pendingCount);
        }

        if (successRate < 90) {
            alerts.add("Tasa de éxito baja: " + String.format("%.1f%%", successRate));
        }

        if (alerts.isEmpty()) {
            alerts.add("Sistema funcionando correctamente");
        }

        return alerts;
    }

    /**
     * Verificar si el sistema necesita atención
     */
    public boolean systemNeedsAttention() {
        SystemStatusReport report = getSystemStatusReport();
        return report.hasCriticalAlerts();
    }

    /**
     * Obtener estado operacional del sistema
     */
    public String getSystemOperationalStatus() {
        SystemStatusReport report = getSystemStatusReport();
        return report.getSystemStatus().getDisplayName();
    }
}