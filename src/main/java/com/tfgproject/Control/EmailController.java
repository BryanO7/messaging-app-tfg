package com.tfgproject.Control;

import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.domain.model.MessageResult;
import com.tfgproject.domain.port.in.SendMessageUseCase;
import com.tfgproject.model.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private SendMessageUseCase sendMessageUseCase; // Nueva dependencia hexagonal

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody EmailMessage emailMessage) {
        logger.info("Solicitud recibida para enviar email a: {}", emailMessage.getTo());

        Map<String, Object> response = new HashMap<>();

        // Convertimos a comando hexagonal
        SendEmailCommand command = SendEmailCommand.fromEmailMessage(emailMessage);

        // Usamos el caso de uso hexagonal
        MessageResult result = sendMessageUseCase.sendEmail(command);

        if (result.isSuccess()) {
            response.put("success", true);
            response.put("message", result.getMessage());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", result.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String to) {
        logger.info("Solicitud de prueba de email a: {}", to);

        // Crear comando directamente
        SendEmailCommand command = new SendEmailCommand();
        command.setTo(to);
        command.setSubject("Prueba de email desde TFG Messaging App");
        command.setText("Este es un mensaje de prueba enviado desde la aplicación de mensajería unificada.");

        Map<String, Object> response = new HashMap<>();

        MessageResult result = sendMessageUseCase.sendEmail(command);

        if (result.isSuccess()) {
            response.put("success", true);
            response.put("message", "Email de prueba enviado correctamente a " + to);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Error al enviar el email de prueba: " + result.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}