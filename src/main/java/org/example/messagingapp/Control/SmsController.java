package org.example.messagingapp.Control;

import org.example.messagingapp.model.SmsMessage;
import org.example.messagingapp.Service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "*") // Para desarrollo - ajusta en producción
public class SmsController {
    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);

    @Autowired
    private SmsService smsService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendSms(@RequestBody SmsMessage smsMessage) {
        logger.info("Solicitud recibida para enviar SMS a: {}", smsMessage.getTo());

        Map<String, Object> response = new HashMap<>();

        boolean result = smsService.sendSms(smsMessage);

        if (result) {
            response.put("success", true);
            response.put("message", "SMS enviado correctamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Error al enviar el SMS");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testSms(@RequestParam(required = false) String to) {
        String phoneNumber = (to != null && !to.isEmpty()) ? to : "644023859"; // Usa el número proporcionado o el predeterminado

        logger.info("Solicitud de prueba de SMS a: {}", phoneNumber);

        SmsMessage testMessage = new SmsMessage();
        testMessage.setTo(phoneNumber);
        testMessage.setText("Este es un mensaje de prueba enviado desde la aplicación de mensajería unificada TFG.");
        testMessage.setSender("TFG-App");

        Map<String, Object> response = new HashMap<>();

        boolean result = smsService.sendSms(testMessage);

        if (result) {
            response.put("success", true);
            response.put("message", "SMS de prueba enviado correctamente a " + phoneNumber);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Error al enviar el SMS de prueba");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}