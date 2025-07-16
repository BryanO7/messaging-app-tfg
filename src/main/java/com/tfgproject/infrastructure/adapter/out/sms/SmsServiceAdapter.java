package com.tfgproject.infrastructure.adapter.out.sms;

import com.tfgproject.infrastructure.service.MessagePublisher;
import com.tfgproject.application.command.SendSmsCommand;
import com.tfgproject.domain.port.out.SmsServicePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsServiceAdapter implements SmsServicePort {

    @Autowired
    private MessagePublisher messagePublisher;

    @Value("${app.messaging.async:true}")
    private boolean useAsync;

    @Override
    public boolean sendSms(SendSmsCommand command) {
        System.out.println("🔍 ADAPTER: SmsServiceAdapter.sendSms() - Modo: " +
                (useAsync ? "ASYNC (RabbitMQ)" : "SYNC"));

        try {
            if (useAsync) {
                // === MODO ASÍNCRONO CON RABBITMQ ===
                System.out.println("🐰 ADAPTER: Enviando SMS a RabbitMQ cola");

                String messageId = messagePublisher.sendSmsToQueue(
                        command.getTo(),
                        command.getText(),
                        command.getSender()
                );

                System.out.println("🐰 ADAPTER: SMS encolado con ID: " + messageId);
                return true; // Retorna true porque se encoló exitosamente

            } else {
                // === MODO SÍNCRONO (CÓDIGO ORIGINAL) ===
                System.out.println("🔍 ADAPTER: Enviando SMS directamente (modo síncrono)");

                // Aquí iría tu código original del SmsService
                return true;
            }

        } catch (Exception e) {
            System.out.println("🔍 ADAPTER: Error SMS: " + e.getMessage());
            return false;
        }
    }
}