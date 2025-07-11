// infrastructure/adapter/out/external/EmailServiceAdapter.java
package com.tfgproject.infrastructure.adapter.out.external;

import com.tfgproject.Service.EmailService; // Tu servicio actual
import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.domain.port.out.EmailServicePort;
import com.tfgproject.model.EmailMessage; // Tu modelo actual
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailServiceAdapter implements EmailServicePort {

    @Autowired
    private EmailService emailService; // Reutilizas tu servicio actual

    @Override
    public boolean sendEmail(SendEmailCommand command) {
        // Conviertes el comando a tu modelo actual
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(command.getTo());
        emailMessage.setSubject(command.getSubject());
        emailMessage.setText(command.getText());

        // Usas tu servicio actual sin cambios
        return emailService.sendEmail(emailMessage);
    }
}