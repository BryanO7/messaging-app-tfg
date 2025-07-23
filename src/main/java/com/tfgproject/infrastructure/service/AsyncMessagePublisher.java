package com.tfgproject.infrastructure.service;

import com.tfgproject.infrastructure.config.RabbitMQConfig;
import com.tfgproject.shared.model.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncMessagePublisher {
    private static final Logger logger = LoggerFactory.getLogger(AsyncMessagePublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AsyncScheduledMessageProcessor scheduledMessageProcessor;

    /**
     * PROGRAMACIÓN ASÍNCRONA DE MENSAJES - NO BLOQUEA
     */
    @Async("taskExecutor")
    public CompletableFuture<String> scheduleMessageAsync(String to, String subject,
                                                          String content, LocalDateTime scheduledTime) {
        logger.info("⏰ Programando mensaje asincrónicamente para: {}", scheduledTime);

        try {
            QueueMessage message = QueueMessage.forEmail(to, subject, content);
            message.setScheduledTime(scheduledTime);

            // Enviar a cola de programados o agregar al processor según la lógica
            if (scheduledTime.isAfter(LocalDateTime.now().plusMinutes(1))) {
                // Si es más de 1 minuto en el futuro, usar cola de RabbitMQ
                rabbitTemplate.convertAndSend(RabbitMQConfig.SCHEDULED_QUEUE, message);
                logger.info("📨 Mensaje enviado a cola de programados: {}", message.getId());
            } else {
                // Si es muy pronto, usar el processor en memoria
                scheduledMessageProcessor.addScheduledMessageAsync(message);
                logger.info("🧠 Mensaje agregado a processor en memoria: {}", message.getId());
            }

            logger.info("✅ Mensaje programado exitosamente (ASYNC). ID: {}", message.getId());
            return CompletableFuture.completedFuture(message.getId());

        } catch (Exception e) {
            logger.error("❌ Error programando mensaje: {}", e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Método síncrono mejorado (compatible con código existente)
     */
    public String scheduleMessage(String to, String subject, String content, LocalDateTime scheduledTime) {
        logger.info("⏰ Programando mensaje para: {}", scheduledTime);

        QueueMessage message = QueueMessage.forEmail(to, subject, content);
        message.setScheduledTime(scheduledTime);

        try {
            if (scheduledTime.isAfter(LocalDateTime.now())) {
                // Usar el processor asíncrono - NO BLOQUEA
                scheduledMessageProcessor.addScheduledMessage(message);
                logger.info("✅ Mensaje programado exitosamente. ID: {}", message.getId());
                return message.getId();
            } else {
                logger.warn("⚠️ Fecha programada en el pasado, enviando inmediatamente");
                // Enviar inmediatamente
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.EMAIL_ROUTING_KEY,
                        message
                );
                return message.getId();
            }

        } catch (Exception e) {
            logger.error("❌ Error programando mensaje: {}", e.getMessage());
            throw new RuntimeException("Error al programar mensaje: " + e.getMessage());
        }
    }

    // === MÉTODOS PARA ENVÍO INMEDIATO ===

    public String sendEmailToQueue(String to, String subject, String content) {
        return sendEmailToQueue(to, subject, content, null, false);
    }

    public String sendEmailToQueue(String to, String subject, String content,
                                   String attachmentPath, boolean isHtml) {
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
