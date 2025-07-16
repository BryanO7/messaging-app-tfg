// src/main/java/com/tfgproject/infrastructure/service/ScheduledMessageProcessor.java
package com.tfgproject.infrastructure.service;

import com.tfgproject.infrastructure.config.RabbitMQConfig;
import com.tfgproject.shared.model.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ScheduledMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledMessageProcessor.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Almacén temporal de mensajes programados
    private final ConcurrentMap<String, QueueMessage> scheduledMessages = new ConcurrentHashMap<>();

    /**
     * Agregar mensaje programado al almacén
     */
    public void addScheduledMessage(QueueMessage message) {
        scheduledMessages.put(message.getId(), message);
        logger.info("➕ Mensaje programado agregado: {} para {}", message.getId(), message.getScheduledTime());
    }

    /**
     * Procesar mensajes programados cada minuto
     */
    @Scheduled(fixedRate = 60000) // Cada 60 segundos
    public void processScheduledMessages() {
        LocalDateTime now = LocalDateTime.now();

        logger.info("🔍 Verificando mensajes programados... Total: {}", scheduledMessages.size());

        if (scheduledMessages.isEmpty()) {
            logger.debug("📭 No hay mensajes programados");
            return;
        }

        scheduledMessages.values().forEach(message -> {
            logger.info("🔍 Verificando mensaje: {} programado para: {}",
                    message.getId(), message.getScheduledTime());

            if (message.getScheduledTime() != null &&
                    now.isAfter(message.getScheduledTime())) {

                logger.info("⏰ ¡Es hora de enviar mensaje programado: {}!", message.getId());

                try {
                    // Enviar según el tipo
                    if ("EMAIL".equals(message.getType())) {
                        logger.info("📧 Enviando email programado a: {}", message.getRecipients().get(0));
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.DIRECT_EXCHANGE,
                                RabbitMQConfig.EMAIL_ROUTING_KEY,
                                message
                        );
                    } else if ("SMS".equals(message.getType())) {
                        logger.info("📱 Enviando SMS programado a: {}", message.getRecipients().get(0));
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.DIRECT_EXCHANGE,
                                RabbitMQConfig.SMS_ROUTING_KEY,
                                message
                        );
                    }

                    // Remover del almacén
                    scheduledMessages.remove(message.getId());
                    logger.info("✅ Mensaje programado enviado y removido: {}", message.getId());

                } catch (Exception e) {
                    logger.error("❌ Error enviando mensaje programado {}: {}",
                            message.getId(), e.getMessage());

                    // Opcional: implementar reintentos
                    message.setRetryCount(message.getRetryCount() + 1);
                    if (message.getRetryCount() >= 3) {
                        scheduledMessages.remove(message.getId());
                        logger.error("💀 Mensaje programado descartado tras {} intentos: {}",
                                message.getRetryCount(), message.getId());
                    }
                }
            } else {
                logger.debug("⏳ Mensaje {} aún no es hora (ahora: {}, programado: {})",
                        message.getId(), now, message.getScheduledTime());
            }
        });
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
        return false;
    }

    /**
     * Obtener todos los mensajes programados
     */
    public ConcurrentMap<String, QueueMessage> getAllScheduledMessages() {
        return scheduledMessages;
    }

    /**
     * Obtener estadísticas
     */
    public int getScheduledMessageCount() {
        return scheduledMessages.size();
    }
}