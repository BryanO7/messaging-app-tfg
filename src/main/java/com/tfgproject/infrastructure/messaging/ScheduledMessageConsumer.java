package com.tfgproject.infrastructure.messaging;

import com.tfgproject.infrastructure.config.RabbitMQConfig;
import com.tfgproject.infrastructure.service.ScheduledMessageProcessor;
import com.tfgproject.shared.model.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledMessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledMessageConsumer.class);

    @Autowired
    private ScheduledMessageProcessor scheduledMessageProcessor;

    @RabbitListener(queues = RabbitMQConfig.SCHEDULED_QUEUE)
    public void processScheduledMessage(QueueMessage queueMessage) {
        logger.info("⏰ Recibido mensaje programados. ID: {}", queueMessage.getId());

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime scheduledTime = queueMessage.getScheduledTime();

            if (scheduledTime == null) {
                logger.warn("⚠️ Mensaje sin fecha programada, enviando inmediatamente");
                scheduledTime = now;
            }

            if (scheduledTime.isAfter(now)) {
                // Agregar al procesador para envío posterior
                scheduledMessageProcessor.addScheduledMessage(queueMessage);
                logger.info("📅 Mensaje programado para: {}", scheduledTime);
            } else {
                // Enviar inmediatamente
                logger.info("🚀 Enviando mensaje programado inmediatamente");
                scheduledMessageProcessor.addScheduledMessage(queueMessage);
            }

        } catch (Exception e) {
            logger.error("💥 Error procesando mensaje programado: {}", e.getMessage());
        }
    }
}