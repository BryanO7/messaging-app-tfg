package com.tfgproject.infrastructure.messaging;

import com.tfgproject.infrastructure.config.RabbitMQConfig;
import com.tfgproject.infrastructure.service.SmsService;
import com.tfgproject.shared.model.SmsMessage;
import com.tfgproject.shared.model.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(SmsConsumer.class);

    @Autowired
    private SmsService smsService;

    @RabbitListener(queues = RabbitMQConfig.SMS_QUEUE)
    public void processSmsMessage(QueueMessage queueMessage) {
        logger.info("📱 Procesando SMS de la cola. ID: {}", queueMessage.getId());

        try {
            // Convertir QueueMessage a SmsMessage
            SmsMessage smsMessage = convertToSmsMessage(queueMessage);

            // Procesar cada destinatario
            for (String recipient : queueMessage.getRecipients()) {
                logger.info("📤 Enviando SMS a: {}", recipient);

                smsMessage.setTo(recipient);
                boolean success = smsService.sendSms(smsMessage);

                if (success) {
                    logger.info("✅ SMS enviado exitosamente a: {}", recipient);
                } else {
                    logger.error("❌ Error enviando SMS a: {}", recipient);
                    handleFailure(queueMessage, recipient);
                }
            }

        } catch (Exception e) {
            logger.error("💥 Error procesando mensaje SMS de cola: {}", e.getMessage());
            handleFailure(queueMessage, "unknown");
        }
    }

    private SmsMessage convertToSmsMessage(QueueMessage queueMessage) {
        SmsMessage smsMessage = new SmsMessage();
        smsMessage.setText(queueMessage.getContent());
        smsMessage.setSender(queueMessage.getSender() != null ? queueMessage.getSender() : "TFG-App");
        return smsMessage;
    }

    private void handleFailure(QueueMessage queueMessage, String recipient) {
        // Implementar lógica de reintento o envío a Dead Letter Queue
        logger.warn("⚠️ Implementar manejo de fallos SMS para: {}", recipient);
    }
}
