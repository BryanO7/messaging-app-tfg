package com.tfgproject.infrastructure.service;

import com.tfgproject.infrastructure.config.RabbitMQConfig;
import com.tfgproject.shared.model.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessagePublisher {
    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ScheduledMessageProcessor scheduledMessageProcessor;

    // === CASO DE USO 01: ENVÍO ÚNICO ===
    public String sendEmailToQueue(String to, String subject, String content) {
        return sendEmailToQueue(to, subject, content, null, false);
    }

    public String sendEmailToQueue(String to, String subject, String content, String attachmentPath, boolean isHtml) {
        logger.info("📧 Enviando email a cola: {}", to);

        QueueMessage message = QueueMessage.forEmail(to, subject, content);
        message.setAttachmentPath(attachmentPath);
        message.setHtml(isHtml);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DIRECT_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    message
            );

            logger.info("✅ Email encolado exitosamente. ID: {}", message.getId());
            return message.getId();

        } catch (Exception e) {
            logger.error("❌ Error encolando email: {}", e.getMessage());
            throw new RuntimeException("Error al encolar email: " + e.getMessage());
        }
    }

    public String sendSmsToQueue(String to, String content, String sender) {
        logger.info("📱 Enviando SMS a cola: {}", to);

        QueueMessage message = QueueMessage.forSms(to, content, sender);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DIRECT_EXCHANGE,
                    RabbitMQConfig.SMS_ROUTING_KEY,
                    message
            );

            logger.info("✅ SMS encolado exitosamente. ID: {}", message.getId());
            return message.getId();

        } catch (Exception e) {
            logger.error("❌ Error encolando SMS: {}", e.getMessage());
            throw new RuntimeException("Error al encolar SMS: " + e.getMessage());
        }
    }

    // === CASO DE USO 02: DIFUSIÓN MÚLTIPLES CANALES ===
    public String broadcastMessage(List<String> recipients, String content, String subject) {
        logger.info("📢 Enviando difusión a {} destinatarios", recipients.size());

        QueueMessage message = QueueMessage.forBroadcast(recipients, content, subject);

        try {
            // Usar FANOUT para enviar a todas las colas
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.FANOUT_EXCHANGE,
                    "", // Fanout no usa routing key
                    message
            );

            logger.info("✅ Difusión encolada exitosamente. ID: {}", message.getId());
            return message.getId();

        } catch (Exception e) {
            logger.error("❌ Error encolando difusión: {}", e.getMessage());
            throw new RuntimeException("Error al encolar difusión: " + e.getMessage());
        }
    }

    // === CASO DE USO 04: PROGRAMACIÓN ===
    public String scheduleMessage(String to, String subject, String content, LocalDateTime scheduledTime) {
        logger.info("⏰ Programando mensaje para: {}", scheduledTime);

        QueueMessage message = QueueMessage.forEmail(to, subject, content);
        message.setScheduledTime(scheduledTime);

        try {
            if (scheduledTime.isAfter(LocalDateTime.now())) {
                // Enviar a cola de programados
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.SCHEDULED_QUEUE,
                        message
                );

                logger.info("✅ Mensaje programado exitosamente. ID: {}", message.getId());
                return message.getId();
            } else {
                logger.warn("⚠️ Fecha programada en el pasado, enviando inmediatamente");
                return sendEmailToQueue(to, subject, content);
            }

        } catch (Exception e) {
            logger.error("❌ Error programando mensaje: {}", e.getMessage());
            throw new RuntimeException("Error al programar mensaje: " + e.getMessage());
        }
    }

    // === MÉTODO GENÉRICO PARA REINTENTOS ===
    public void retryMessage(QueueMessage message, String queueName) {
        logger.info("🔄 Reintentando mensaje ID: {}", message.getId());

        message.setRetryCount(message.getRetryCount() + 1);

        try {
            rabbitTemplate.convertAndSend(queueName, message);
            logger.info("✅ Mensaje reenviado a cola: {}", queueName);

        } catch (Exception e) {
            logger.error("❌ Error reenviando mensaje: {}", e.getMessage());
            throw new RuntimeException("Error al reenviar mensaje: " + e.getMessage());
        }
    }
}