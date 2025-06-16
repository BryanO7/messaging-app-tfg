package org.example.messagingapp.Control;

import org.example.messagingapp.Service.EmailService;
import org.example.messagingapp.model.EmailMessage;
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
@CrossOrigin(origins = "*") // Para desarrollo - ajusta en producción
public class EmailController {
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody EmailMessage emailMessage) {
        logger.info("Solicitud recibida para enviar email a: {}", emailMessage.getTo());

        Map<String, Object> response = new HashMap<>();

        boolean result = emailService.sendEmail(emailMessage);

        if (result) {
            response.put("success", true);
            response.put("message", "Email enviado correctamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Error al enviar el email");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String to) {
        logger.info("Solicitud de prueba de email a: {}", to);

        EmailMessage testMessage = new EmailMessage();
        testMessage.setTo(to);
        testMessage.setSubject("Prueba de email desde TFG Messaging App");
        testMessage.setText("Este es un mensaje de prueba enviado desde la aplicación de mensajería unificada.");

        Map<String, Object> response = new HashMap<>();

        boolean result = emailService.sendEmail(testMessage);

        if (result) {
            response.put("success", true);
            response.put("message", "Email de prueba enviado correctamente a " + to);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Error al enviar el email de prueba");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}