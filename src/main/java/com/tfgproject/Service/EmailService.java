 package com.tfgproject.Service;

import com.tfgproject.model.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Date;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;


    public boolean sendSimpleMessage(EmailMessage emailMessage) {
        try {
            logger.info("Enviando email simple a {}", emailMessage.getTo());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailMessage.getTo());
            message.setSubject(emailMessage.getSubject());
            message.setText(emailMessage.getText());
            message.setSentDate(new Date());

            emailSender.send(message);
            logger.info("Email enviado correctamente");
            return true;
        } catch (Exception e) {
            logger.error("Error enviando email: {}", e.getMessage(), e);
            return false;
        }
    }


    public boolean sendMessageWithAttachment(EmailMessage emailMessage) {
        try {
            logger.info("Enviando email con adjunto/HTML a {}", emailMessage.getTo());

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(emailMessage.getTo());
            helper.setSubject(emailMessage.getSubject());
            helper.setText(emailMessage.getText(), emailMessage.isHtml());
            helper.setSentDate(new Date());

            // Añadir adjunto si existe
            if (emailMessage.getAttachmentPath() != null && !emailMessage.getAttachmentPath().isEmpty()) {
                File file = new File(emailMessage.getAttachmentPath());
                if (file.exists()) {
                    helper.addAttachment(file.getName(), file);
                } else {
                    logger.warn("El archivo adjunto no existe: {}", emailMessage.getAttachmentPath());
                }
            }

            emailSender.send(message);
            logger.info("Email con adjunto/HTML enviado correctamente");
            return true;
        } catch (MessagingException e) {
            logger.error("Error enviando email con adjunto/HTML: {}", e.getMessage(), e);
            return false;
        }
    }


    public boolean sendEmail(EmailMessage emailMessage) {
        // Validar datos básicos
        if (emailMessage.getTo() == null || emailMessage.getTo().isEmpty()) {
            logger.error("Destinatario de email no especificado");
            return false;
        }

        // Decidir qué método usar según el tipo de mensaje
        if (emailMessage.isHtml() ||
                (emailMessage.getAttachmentPath() != null && !emailMessage.getAttachmentPath().isEmpty())) {
            return sendMessageWithAttachment(emailMessage);
        } else {
            return sendSimpleMessage(emailMessage);
        }
    }
}