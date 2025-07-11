package com.tfgproject.Control;

import com.tfgproject.application.command.SendSmsCommand;
import com.tfgproject.domain.model.MessageResult;
import com.tfgproject.domain.port.in.SendMessageUseCase;
import com.tfgproject.model.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "*")
public class SmsController {
    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);

    @Autowired
    private SendMessageUseCase sendMessageUseCase; // Nueva dependencia hexagonal

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendSms(@RequestBody SmsMessage smsMessage) {
        logger.info("Solicitud recibida para enviar SMS a: {}", smsMessage.getTo());

        Map<String, Object> response = new HashMap<>();

        // Convertimos a comando hexagonal
        SendSmsCommand command = SendSmsCommand.fromSmsMessage(smsMessage);

        // Usamos el caso de uso hexagonal
        MessageResult result = sendMessageUseCase.sendSms(command);

        if (result.isSuccess()) {
            response.put("success", true);
            response.put("message", result.getMessage());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", result.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testSms(@RequestParam(required = false) String to) {
        String phoneNumber = (to != null && !to.isEmpty()) ? to : "644023859";

        logger.info("Solicitud de prueba de SMS a: {}", phoneNumber);

        // Crear comando directamente
        SendSmsCommand command = new SendSmsCommand();
        command.setTo(phoneNumber);
        command.setText("Este es un mensaje de prueba enviado desde la aplicación de mensajería unificada TFG.");
        command.setSender("TFG-App");

        Map<String, Object> response = new HashMap<>();

        MessageResult result = sendMessageUseCase.sendSms(command);

        if (result.isSuccess()) {
            response.put("success", true);
            response.put("message", "SMS de prueba enviado correctamente a " + phoneNumber);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Error al enviar el SMS de prueba: " + result.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}