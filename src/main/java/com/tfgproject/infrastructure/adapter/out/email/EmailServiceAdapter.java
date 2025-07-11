// infrastructure/adapter/out/external/EmailServiceAdapter.java
package com.tfgproject.infrastructure.adapter.out.email;

import com.tfgproject.infrastructure.service.EmailService; // Tu servicio actual
import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.domain.port.out.EmailServicePort;
import com.tfgproject.shared.model.EmailMessage; // Tu modelo actual
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailServiceAdapter implements EmailServicePort {

    @Autowired
    private EmailService emailService;

    @Override
    public boolean sendEmail(SendEmailCommand command) {
        System.out.println("🔌 ADAPTER: EmailServiceAdapter.sendEmail() ejecutándose");
        System.out.println("🔌 ADAPTER: Convirtiendo SendEmailCommand a EmailMessage");

        // Conversión
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(command.getTo());
        emailMessage.setSubject(command.getSubject());
        emailMessage.setText(command.getText());

        System.out.println("🔌 ADAPTER: Llamando al servicio real EmailService");

        boolean result = emailService.sendEmail(emailMessage);  // ← AQUÍ LLAMA AL SERVICIO REAL

        System.out.println("🔌 ADAPTER: EmailService retornó: " + result);

        return result;
    }
}