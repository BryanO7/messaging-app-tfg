// src/main/java/com/tfgproject/infrastructure/adapter/in/web/MessagingController.java
package com.tfgproject.infrastructure.adapter.in.web;

import com.tfgproject.application.dto.request.BroadcastRequest;
import com.tfgproject.application.dto.request.ScheduleRequest;
import com.tfgproject.application.dto.request.MessageRequest;
import com.tfgproject.application.dto.response.MessageResponse;
import com.tfgproject.application.dto.response.QueueStatusResponse;
import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/messaging")
@CrossOrigin(origins = "*")
@Validated
public class MessagingController {
    private static final Logger logger = LoggerFactory.getLogger(MessagingController.class);

    @Autowired
    private AsyncMessagePublisher messagePublisher;

    // === CASO DE USO 02: DIFUSIÓN ===
    @PostMapping("/broadcast")
    public ResponseEntity<MessageResponse> broadcastMessage(
            @Valid @RequestBody BroadcastRequest request) {

        logger.info("📢 Solicitud de difusión a {} destinatarios", request.getRecipients().size());

        try {
            // Validación adicional
            if (!request.isValid()) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.failure("Datos de difusión inválidos"));
            }

            String messageId = messagePublisher.broadcastMessage(
                    request.getRecipients(),
                    request.getContent(),
                    request.getSubject()
            );

            MessageResponse response = MessageResponse.broadcast(messageId, request.getRecipients());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error en difusión: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(MessageResponse.failure("Error al procesar difusión: " + e.getMessage()));
        }
    }

    // === CASO DE USO 04: PROGRAMACIÓN ===
    @PostMapping("/schedule")
    public ResponseEntity<MessageResponse> scheduleMessage(
            @Valid @RequestBody ScheduleRequest request) {

        logger.info("⏰ Solicitud de programación para: {}", request.getScheduledTime());

        try {
            // Validación adicional
            if (!request.isValid()) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.failure("Datos de programación inválidos"));
            }

            String messageId = messagePublisher.scheduleMessage(
                    request.getTo(),
                    request.getSubject(),
                    request.getContent(),
                    request.getScheduledTime()
            );

            MessageResponse response = MessageResponse.scheduled(messageId, request.getScheduledTime());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error programando mensaje: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(MessageResponse.failure("Error al programar mensaje: " + e.getMessage()));
        }
    }

    // === ENDPOINT UNIFICADO ===
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request) {

        // ✅ CORREGIDO: Leer el canal en lugar del tipo
        logger.info("📨 Solicitud de envío unificado. Canal: {}", request.getChannel());
        logger.info("📋 Datos recibidos: to={}, channel={}, content={}",
                request.getTo(), request.getChannel(), request.getContent());

        try {
            String messageId;

            if (request.isBroadcast()) {
                // Difusión
                messageId = messagePublisher.broadcastMessage(
                        request.getRecipients(),
                        request.getContent(),
                        request.getSubject()
                );
                return ResponseEntity.ok(MessageResponse.broadcast(messageId, request.getRecipients()));

            } else if (request.isScheduled()) {
                // Programado
                LocalDateTime scheduledTime = LocalDateTime.parse(request.getScheduledTime());
                messageId = messagePublisher.scheduleMessage(
                        request.getTo(),
                        request.getSubject(),
                        request.getContent(),
                        scheduledTime
                );
                return ResponseEntity.ok(MessageResponse.scheduled(messageId, scheduledTime));

            } else {
                // ✅ CORREGIDO: Envío único basado en el canal
                String channel = request.getChannel() != null ? request.getChannel().toLowerCase() : "email";

                switch (channel) {
                    case "sms":
                        logger.info("📱 Procesando como SMS");
                        messageId = messagePublisher.sendSmsToQueue(
                                request.getTo(),
                                request.getContent(),
                                request.getSender() != null ? request.getSender() : "TFG-App"
                        );
                        break;

                    case "email":
                        logger.info("📧 Procesando como EMAIL");
                        messageId = messagePublisher.sendEmailToQueue(
                                request.getTo(),
                                request.getSubject(),
                                request.getContent(),
                                request.getAttachmentPath(),
                                request.isHtml()
                        );
                        break;

                    case "both":
                        logger.info("📧📱 Procesando como AMBOS canales");

                        // ✅ NUEVO: Primero enviar email
                        String emailId = messagePublisher.sendEmailToQueue(
                                request.getEmail() != null ? request.getEmail() : request.getTo(),
                                request.getSubject(),
                                request.getContent(),
                                request.getAttachmentPath(),
                                request.isHtml()
                        );
                        logger.info("📧 Email encolado con ID: {}", emailId);

                        // ✅ NUEVO: Después enviar SMS
                        String smsId = messagePublisher.sendSmsToQueue(
                                request.getPhone() != null ? request.getPhone() : request.getTo(),
                                request.getContent(),
                                request.getSender() != null ? request.getSender() : "TFG-App"
                        );
                        logger.info("📱 SMS encolado con ID: {}", smsId);

                        // Usar el ID del email como principal
                        messageId = emailId;
                        break;

                    default:
                        logger.warn("⚠️ Canal desconocido: {}", request.getChannel());
                        return ResponseEntity.badRequest()
                                .body(MessageResponse.failure("Canal no soportado: " + request.getChannel()));
                }

                return ResponseEntity.ok(MessageResponse.success("Mensaje enviado exitosamente", messageId));
            }

        } catch (Exception e) {
            logger.error("❌ Error en envío unificado: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(MessageResponse.failure("Error al enviar mensaje: " + e.getMessage()));
        }
    }

    // === ENDPOINTS DIRECTOS PARA COMPATIBILIDAD ===

    // Endpoint directo para email (para compatibilidad)
    @PostMapping("/email")
    public ResponseEntity<MessageResponse> sendEmail(@RequestBody MessageRequest request) {
        logger.info("📧 Solicitud de email directo a: {}", request.getTo());

        try {
            String messageId = messagePublisher.sendEmailToQueue(
                    request.getTo(),
                    request.getSubject(),
                    request.getContent(),
                    request.getAttachmentPath(),
                    request.isHtml()
            );

            MessageResponse response = MessageResponse.success("Email enviado exitosamente", messageId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error enviando email: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(MessageResponse.failure("Error al enviar email: " + e.getMessage()));
        }
    }

    // Endpoint directo para SMS (para compatibilidad)
    @PostMapping("/sms")
    public ResponseEntity<MessageResponse> sendSms(@RequestBody MessageRequest request) {
        logger.info("📱 Solicitud de SMS directo a: {}", request.getTo());

        try {
            String messageId = messagePublisher.sendSmsToQueue(
                    request.getTo(),
                    request.getContent(),
                    request.getSender() != null ? request.getSender() : "TFG-App"
            );

            MessageResponse response = MessageResponse.success("SMS enviado exitosamente", messageId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error enviando SMS: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(MessageResponse.failure("Error al enviar SMS: " + e.getMessage()));
        }
    }

    // === TESTING: Estado de las colas ===
    @GetMapping("/queue-status")
    public ResponseEntity<QueueStatusResponse> getQueueStatus() {

        // Aquí podrías integrar RabbitMQ Management API
        QueueStatusResponse response = QueueStatusResponse.active();
        return ResponseEntity.ok(response);
    }
}