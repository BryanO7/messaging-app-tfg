// 2. Actualiza tu EmailServiceAdapter para manejar attachments:

package com.tfgproject.infrastructure.adapter.out.email;

import com.tfgproject.infrastructure.service.EmailService;
import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.domain.port.out.EmailServicePort;
import com.tfgproject.shared.model.EmailMessage;
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
        System.out.println("🔌 ADAPTER: Attachment: " + command.getAttachmentPath());

        // Conversión completa incluyendo attachment
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(command.getTo());
        emailMessage.setSubject(command.getSubject());
        emailMessage.setText(command.getText());

        // ✅ NUEVO: Manejar attachment
        if (command.hasAttachment()) {
            emailMessage.setAttachmentPath(command.getAttachmentPath());
            System.out.println("🔌 ADAPTER: Configurando attachment: " + command.getAttachmentPath());
        }

        // ✅ NUEVO: Manejar HTML
        emailMessage.setHtml(command.isHtml());

        System.out.println("🔌 ADAPTER: Llamando al servicio real EmailService");

        boolean result = emailService.sendEmail(emailMessage);

        System.out.println("🔌 ADAPTER: EmailService retornó: " + result);

        return result;
    }
}