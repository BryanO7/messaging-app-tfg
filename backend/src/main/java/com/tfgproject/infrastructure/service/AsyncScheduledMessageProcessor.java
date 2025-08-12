package com.tfgproject.infrastructure.service;

import com.tfgproject.infrastructure.config.RabbitMQConfig;
import com.tfgproject.shared.model.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsyncScheduledMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AsyncScheduledMessageProcessor.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Almacén thread-safe de mensajes programados
    private final ConcurrentMap<String, QueueMessage> scheduledMessages = new ConcurrentHashMap<>();

    /**
     * Agregar mensaje programado de forma asíncrona
     */
    @Async("taskExecutor")
    public void addScheduledMessageAsync(QueueMessage message) {
        logger.info("➕ Agregando mensaje programado asincrónicamente: {} para {}",
                message.getId(), message.getScheduledTime());

        scheduledMessages.put(message.getId(), message);
        logger.info("✅ Mensaje programado agregado. Total mensajes: {}", scheduledMessages.size());
    }

    /**
     * Método síncrono para compatibilidad
     */
    public void addScheduledMessage(QueueMessage message) {
        scheduledMessages.put(message.getId(), message);
        logger.info("➕ Mensaje programado agregado: {} para {}. Total: {}",
                message.getId(), message.getScheduledTime(), scheduledMessages.size());
    }

    /**
     * Procesamiento asíncrono cada 30 segundos (más frecuente)
     */
    @Scheduled(fixedRate = 30000) // Cada 30 segundos
    @Async("taskExecutor")
    public void processScheduledMessages() {
        if (scheduledMessages.isEmpty()) {
            logger.debug("📭 No hay mensajes programados para procesar");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        logger.info("🔍 Procesando {} mensajes programados - Hora actual: {}",
                scheduledMessages.size(), now);

        // Obtener mensajes que deben enviarse
        List<QueueMessage> messagesToSend = scheduledMessages.values().stream()
                .filter(message -> {
                    LocalDateTime scheduledTime = message.getScheduledTime();
                    boolean shouldSend = scheduledTime != null &&
                            (now.isAfter(scheduledTime) || now.isEqual(scheduledTime));

                    if (shouldSend) {
                        logger.info("⏰ Mensaje {} listo para envío - Programado: {}, Ahora: {}",
                                message.getId(), scheduledTime, now);
                    }

                    return shouldSend;
                })
                .collect(Collectors.toList());

        logger.info("📬 Encontrados {} mensajes listos para envío", messagesToSend.size());

        // Procesar cada mensaje de forma asíncrona
        messagesToSend.forEach(this::sendScheduledMessageAsync);
    }

    /**
     * Enviar mensaje programado de forma asíncrona
     */
    @Async("taskExecutor")
    public void sendScheduledMessageAsync(QueueMessage message) {
        try {
            logger.info("🚀 Enviando mensaje programado: {} (Tipo: {})",
                    message.getId(), message.getType());

            // Enviar según el tipo
            if ("EMAIL".equals(message.getType())) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.EMAIL_ROUTING_KEY,
                        message
                );
                logger.info("📧 Email programado enviado a cola: {}", message.getRecipients().get(0));

            } else if ("SMS".equals(message.getType())) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.SMS_ROUTING_KEY,
                        message
                );
                logger.info("📱 SMS programado enviado a cola: {}", message.getRecipients().get(0));
            }

            // Remover del almacén después del envío exitoso
            scheduledMessages.remove(message.getId());
            logger.info("✅ Mensaje programado {} enviado y removido. Restantes: {}",
                    message.getId(), scheduledMessages.size());

        } catch (Exception e) {
            logger.error("❌ Error enviando mensaje programado {}: {}",
                    message.getId(), e.getMessage());

            // Implementar lógica de reintentos
            handleSendFailure(message);
        }
    }

    /**
     * Manejar fallos en el envío
     */
    private void handleSendFailure(QueueMessage message) {
        message.setRetryCount(message.getRetryCount() + 1);

        if (message.getRetryCount() >= 3) {
            logger.error("💀 Mensaje programado {} descartado tras {} intentos",
                    message.getId(), message.getRetryCount());
            scheduledMessages.remove(message.getId());
        } else {
            logger.warn("🔄 Mensaje programado {} marcado para reintento ({}/3)",
                    message.getId(), message.getRetryCount());
            // El mensaje permanece en el mapa para ser reintentado
        }
    }

    /**
     * Cancelar mensaje programado
     */
    public boolean cancelScheduledMessage(String messageId) {
        QueueMessage removed = scheduledMessages.remove(messageId);
        if (removed != null) {
            logger.info("❌ Mensaje programado cancelado: {}", messageId);
            return true;
        }
        logger.warn("⚠️ No se encontró mensaje programado para cancelar: {}", messageId);
        return false;
    }

    /**
     * Obtener estadísticas
     */
    public int getScheduledMessageCount() {
        return scheduledMessages.size();
    }

    /**
     * Obtener todos los mensajes programados (para debugging)
     */
    public ConcurrentMap<String, QueueMessage> getAllScheduledMessages() {
        return new ConcurrentHashMap<>(scheduledMessages);
    }

    /**
     * Método para obtener mensajes programados por usuario
     */
    public List<QueueMessage> getScheduledMessagesByUser(String userId) {
        return scheduledMessages.values().stream()
                .filter(message -> userId.equals(message.getUserId()))
                .collect(Collectors.toList());
    }

    /**
     * Limpiar mensajes expirados (opcional - para mensajes muy antiguos)
     */
    @Scheduled(fixedRate = 3600000) // Cada hora
    @Async("taskExecutor")
    public void cleanExpiredMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1); // Mensajes de más de 1 día

        List<String> expiredIds = scheduledMessages.values().stream()
                .filter(message -> message.getScheduledTime().isBefore(threshold))
                .map(QueueMessage::getId)
                .collect(Collectors.toList());

        expiredIds.forEach(id -> {
            scheduledMessages.remove(id);
            logger.warn("🗑️ Mensaje programado expirado removido: {}", id);
        });

        if (!expiredIds.isEmpty()) {
            logger.info("🧹 Limpieza completada: {} mensajes expirados removidos", expiredIds.size());
        }
    }
}