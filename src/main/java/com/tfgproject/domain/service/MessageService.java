package com.tfgproject.domain.service;

import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.application.command.SendSmsCommand;
import com.tfgproject.domain.model.MessageResult;
import com.tfgproject.domain.port.in.SendMessageUseCase;
import com.tfgproject.domain.port.out.EmailServicePort;
import com.tfgproject.domain.port.out.SmsServicePort;
import org.springframework.stereotype.Service;

@Service("sendMessageUseCase")
public class MessageService implements SendMessageUseCase {

    private final EmailServicePort emailService;
    private final SmsServicePort smsService;

    public MessageService(EmailServicePort emailService, SmsServicePort smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @Override
    public MessageResult sendEmail(SendEmailCommand command) {
        // Validaciones de dominio para email
        if (command.getTo() == null || command.getTo().trim().isEmpty()) {
            return MessageResult.failure("Destinatario requerido");
        }

        if (command.getSubject() == null || command.getSubject().trim().isEmpty()) {
            return MessageResult.failure("Asunto requerido");
        }

        if (command.getText() == null || command.getText().trim().isEmpty()) {
            return MessageResult.failure("Contenido del mensaje requerido");
        }

        try {
            boolean result = emailService.sendEmail(command);

            if (result) {
                return MessageResult.success("Email enviado correctamente");
            } else {
                return MessageResult.failure("Error al enviar el email");
            }
        } catch (Exception e) {
            return MessageResult.failure("Error inesperado: " + e.getMessage());
        }
    }

    @Override
    public MessageResult sendSms(SendSmsCommand command) {
        // Validaciones de dominio para SMS
        if (command.getTo() == null || command.getTo().trim().isEmpty()) {
            return MessageResult.failure("Número de teléfono requerido");
        }

        if (command.getText() == null || command.getText().trim().isEmpty()) {
            return MessageResult.failure("Contenido del mensaje requerido");
        }

        // Validación básica de formato de teléfono (opcional)
        if (!isValidPhoneNumber(command.getTo())) {
            return MessageResult.failure("Formato de número de teléfono inválido");
        }

        try {
            boolean result = smsService.sendSms(command);

            if (result) {
                return MessageResult.success("SMS enviado correctamente");
            } else {
                return MessageResult.failure("Error al enviar el SMS");
            }
        } catch (Exception e) {
            return MessageResult.failure("Error inesperado: " + e.getMessage());
        }
    }

    // Método auxiliar para validar números de teléfono
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Validación básica: debe contener solo números, espacios, +, - y ()
        // Puedes hacer esta validación más estricta según tus necesidades
        return phoneNumber.matches("^[+]?[0-9\\s\\-\\(\\)]+$") && phoneNumber.length() >= 9;
    }
}