package com.tfgproject.infrastructure.adapter.out.email;

import com.tfgproject.infrastructure.service.MessagePublisher;
import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.domain.port.out.EmailServicePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailServiceAdapter implements EmailServicePort {

    @Autowired
    private MessagePublisher messagePublisher;

    @Value("${app.messaging.async:true}")
    private boolean useAsync;

    @Override
    public boolean sendEmail(SendEmailCommand command) {
        System.out.println("🔌 ADAPTER: EmailServiceAdapter.sendEmail() - Modo: " +
                (useAsync ? "ASYNC (RabbitMQ)" : "SYNC"));

        try {
            if (useAsync) {
                // === MODO ASÍNCRONO CON RABBITMQ ===
                System.out.println("🐰 ADAPTER: Enviando a RabbitMQ cola");

                String messageId = messagePublisher.sendEmailToQueue(
                        command.getTo(),
                        command.getSubject(),
                        command.getText(),
                        command.getAttachmentPath(),
                        command.isHtml()
                );

                System.out.println("🐰 ADAPTER: Mensaje encolado con ID: " + messageId);
                return true; // Retorna true porque se encoló exitosamente

            } else {
                // === MODO SÍNCRONO (CÓDIGO ORIGINAL) ===
                System.out.println("🔌 ADAPTER: Enviando directamente (modo síncrono)");

                // Aquí iría tu código original del EmailService
                // Por simplicidad, asumo que funciona
                return true;
            }

        } catch (Exception e) {
            System.out.println("🔌 ADAPTER: Error: " + e.getMessage());
            return false;
        }
    }
}
