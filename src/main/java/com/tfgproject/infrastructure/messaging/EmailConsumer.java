package com.tfgproject.infrastructure.messaging;

import com.tfgproject.infrastructure.config.RabbitMQConfig;
import com.tfgproject.infrastructure.service.EmailService;
import com.tfgproject.shared.model.EmailMessage;
import com.tfgproject.shared.model.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void processEmailMessage(QueueMessage queueMessage) {
        logger.info("📧 Procesando email de la cola. ID: {}", queueMessage.getId());

        try {
            // Convertir QueueMessage a EmailMessage
            EmailMessage emailMessage = convertToEmailMessage(queueMessage);

            // Procesar cada destinatario
            for (String recipient : queueMessage.getRecipients()) {
                logger.info("📤 Enviando email a: {}", recipient);

                emailMessage.setTo(recipient);
                boolean success = emailService.sendEmail(emailMessage);

                if (success) {
                    logger.info("✅ Email enviado exitosamente a: {}", recipient);
                } else {
                    logger.error("❌ Error enviando email a: {}", recipient);
                    handleFailure(queueMessage, recipient);
                }
            }

        } catch (Exception e) {
            logger.error("💥 Error procesando mensaje de cola: {}", e.getMessage());
            handleFailure(queueMessage, "unknown");
        }
    }

    private EmailMessage convertToEmailMessage(QueueMessage queueMessage) {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setSubject(queueMessage.getSubject());
        emailMessage.setText(queueMessage.getContent());
        emailMessage.setAttachmentPath(queueMessage.getAttachmentPath());
        emailMessage.setHtml(queueMessage.isHtml());
        return emailMessage;
    }

    private void handleFailure(QueueMessage queueMessage, String recipient) {
        // Implementar lógica de reintento o envío a Dead Letter Queue
        logger.warn("⚠️ Implementar manejo de fallos para: {}", recipient);
    }
}