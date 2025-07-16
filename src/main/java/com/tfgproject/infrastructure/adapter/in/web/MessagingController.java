// src/main/java/com/tfgproject/infrastructure/adapter/in/web/MessagingController.java
package com.tfgproject.infrastructure.adapter.in.web;

import com.tfgproject.application.dto.request.BroadcastRequest;
import com.tfgproject.application.dto.request.ScheduleRequest;
import com.tfgproject.application.dto.request.MessageRequest;
import com.tfgproject.application.dto.response.MessageResponse;
import com.tfgproject.application.dto.response.QueueStatusResponse;
import com.tfgproject.infrastructure.service.MessagePublisher;
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
    private MessagePublisher messagePublisher;

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

        logger.info("📨 Solicitud de envío unificado. Tipo: {}", request.getType());

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
                // Envío único
                if ("SMS".equals(request.getType())) {
                    messageId = messagePublisher.sendSmsToQueue(
                            request.getTo(),
                            request.getContent(),
                            request.getSender()
                    );
                } else {
                    messageId = messagePublisher.sendEmailToQueue(
                            request.getTo(),
                            request.getSubject(),
                            request.getContent(),
                            request.getAttachmentPath(),
                            request.isHtml()
                    );
                }
                return ResponseEntity.ok(MessageResponse.success("Mensaje enviado", messageId));
            }

        } catch (Exception e) {
            logger.error("❌ Error en envío unificado: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(MessageResponse.failure("Error al enviar mensaje: " + e.getMessage()));
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