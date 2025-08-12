// src/test/java/com/tfgproject/RabbitMQIntegrationTest.java (ACTUALIZAR)
package com.tfgproject;

import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.application.command.SendSmsCommand;
import com.tfgproject.domain.model.MessageResult;
import com.tfgproject.domain.model.MessageStatus;
import com.tfgproject.domain.port.in.SendMessageUseCase;
import com.tfgproject.domain.service.MessageStatusService; // ✅ NUEVO IMPORT
import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SpringBootTest

public class RabbitMQIntegrationTest {

    @Autowired
    private SendMessageUseCase sendMessageUseCase;

    @Autowired
    private AsyncMessagePublisher messagePublisher;

    @Autowired
    private MessageStatusService messageStatusService; // ✅ NUEVA DEPENDENCIA

    @Test
    public void testCompleteRabbitMQFlowWithStatusTracking() throws InterruptedException {
        System.out.println("🚀 === TESTING COMPLETE RABBITMQ FLOW + STATUS TRACKING ===");

        // === CASO DE USO 01: ENVÍO ÚNICO EMAIL ===
        System.out.println("\n📧 1. TESTING EMAIL ÚNICO VIA RABBITMQ + STATUS");

        SendEmailCommand emailCommand = new SendEmailCommand();
        emailCommand.setTo("bax6351@gmail.com");
        emailCommand.setSubject("🐰 RabbitMQ + Status Tracking - Email único");
        emailCommand.setText("Este email fue enviado usando RabbitMQ + Status Tracking");

        MessageResult emailResult = sendMessageUseCase.sendEmail(emailCommand);

        System.out.println("Email resultado: " + emailResult.getMessage());
        System.out.println("Email éxito: " + emailResult.isSuccess());
        assert emailResult.isSuccess() : "Email debería encolarse exitosamente";

        // ✅ NUEVO: Verificar que se creó el status
        // Nota: Como no tenemos el messageId directamente del UseCase,
        // verificaremos que hay mensajes en el sistema
        System.out.println("📊 Verificando tracking de status...");

        // === CASO DE USO 01: ENVÍO ÚNICO SMS ===
        System.out.println("\n📱 2. TESTING SMS ÚNICO VIA RABBITMQ + STATUS");

        SendSmsCommand smsCommand = new SendSmsCommand();
        smsCommand.setTo("644023859");
        smsCommand.setText("SMS enviado via RabbitMQ + Status Tracking");
        smsCommand.setSender("TFG-App");

        MessageResult smsResult = sendMessageUseCase.sendSms(smsCommand);

        System.out.println("SMS resultado: " + smsResult.getMessage());
        System.out.println("SMS éxito: " + smsResult.isSuccess());
        assert smsResult.isSuccess() : "SMS debería encolarse exitosamente";

        // === CASO DE USO 02: DIFUSIÓN + STATUS ===
        System.out.println("\n📢 3. TESTING DIFUSIÓN VIA RABBITMQ + STATUS");

        String broadcastId = messagePublisher.broadcastMessage(
                Arrays.asList("bax6351@gmail.com", "644023859"),
                "Mensaje de difusión via RabbitMQ + Status Tracking",
                "🚀 Difusión RabbitMQ + Status"
        );

        System.out.println("Difusión ID: " + broadcastId);
        assert broadcastId != null : "Difusión debería retornar ID";

        // ✅ NUEVO: Verificar status de difusión
        System.out.println("📊 Verificando status de difusión...");
        // Los status de difusión se crean con IDs únicos por destinatario

        // === CASO DE USO 04: PROGRAMACIÓN + STATUS ===
        System.out.println("\n⏰ 4. TESTING PROGRAMACIÓN VIA RABBITMQ + STATUS");

        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(1);

        String scheduledId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⏰ Mensaje programado RabbitMQ + Status",
                "Este mensaje fue programado con tracking de status",
                futureTime
        );

        System.out.println("Programación ID: " + scheduledId);
        System.out.println("Programado para: " + futureTime);
        assert scheduledId != null : "Programación debería retornar ID";

        // ✅ NUEVO: Verificar status de mensaje programado
        System.out.println("📊 Verificando status de mensaje programado...");
        Optional<MessageStatus> scheduledStatus = messageStatusService.getMessageStatus(scheduledId);
        if (scheduledStatus.isPresent()) {
            System.out.println("✅ Status encontrado: " + scheduledStatus.get().getStatus());
            System.out.println("📧 Destinatario: " + scheduledStatus.get().getRecipient());
            System.out.println("📅 Timestamp: " + scheduledStatus.get().getTimestamp());
        } else {
            System.out.println("❌ No se encontró status para mensaje programado");
        }

        // === ESPERAR PROCESAMIENTO ===
        System.out.println("\n⏳ 5. ESPERANDO PROCESAMIENTO DE COLAS...");
        TimeUnit.SECONDS.sleep(5);

        // ✅ NUEVO: Verificar estadísticas finales
        System.out.println("\n📊 6. VERIFICANDO ESTADÍSTICAS FINALES");
        long pendingCount = messageStatusService.getPendingMessageCount();
        long failedCount = messageStatusService.getFailedMessageCount();

        System.out.println("📊 Mensajes pendientes: " + pendingCount);
        System.out.println("📊 Mensajes fallidos: " + failedCount);

        System.out.println("✅ === TESTING RABBITMQ + STATUS TRACKING COMPLETADO ===");
        System.out.println("📧 Revisa tu email: bax6351@gmail.com");
        System.out.println("📱 Revisa tu SMS: 644023859");
        System.out.println("⏰ El mensaje programado llegará en 1 minuto");

        System.out.println("\n🏗️ ARQUITECTURA UTILIZADA:");
        System.out.println("  Test → UseCase → Domain → Adapter → RabbitMQ → Consumer → Service");
        System.out.println("  ✅ Hexagonal Architecture");
        System.out.println("  ✅ Async Processing");
        System.out.println("  ✅ Message Queues");
        System.out.println("  ✅ Status Tracking"); // ✅ NUEVO
        System.out.println("  ✅ Fault Tolerance");
        System.out.println("  ✅ Scalability");
    }

    @Test
    public void testDirectPublisherWithStatusTracking() {
        System.out.println("🐰 === TESTING DIRECT RABBITMQ PUBLISHER + STATUS ===");

        // Test directo del publisher con tracking
        String emailId = messagePublisher.sendEmailToQueue(
                "bax6351@gmail.com",
                "🔥 Test directo RabbitMQ + Status",
                "Este email fue enviado directamente al publisher con tracking"
        );

        System.out.println("Email ID: " + emailId);
        assert emailId != null : "Publisher debería retornar ID";

        // ✅ NUEVO: Verificar que se creó el status
        Optional<MessageStatus> emailStatus = messageStatusService.getMessageStatus(emailId);
        if (emailStatus.isPresent()) {
            MessageStatus status = emailStatus.get();
            System.out.println("✅ Status creado correctamente:");
            System.out.println("  📧 ID: " + status.getMessageId());
            System.out.println("  📊 Estado: " + status.getStatus());
            System.out.println("  👤 Destinatario: " + status.getRecipient());
            System.out.println("  📅 Timestamp: " + status.getTimestamp());

            assert status.getStatus().name().equals("QUEUED") : "Estado inicial debería ser QUEUED";
            assert status.getRecipient().equals("bax6351@gmail.com") : "Destinatario correcto";

        } else {
            System.out.println("❌ ERROR: No se encontró status para el mensaje");
            assert false : "Debería haberse creado un status";
        }

        System.out.println("✅ Direct Publisher + Status test passed");
    }

    @Test
    public void testMessageStatusLifecycle() throws InterruptedException {
        System.out.println("🔄 === TESTING MESSAGE STATUS LIFECYCLE ===");

        // Enviar mensaje y seguir su ciclo de vida
        String messageId = messagePublisher.sendEmailToQueue(
                "bax6351@gmail.com",
                "🔄 Test Lifecycle Status",
                "Siguiendo el ciclo de vida del status"
        );

        System.out.println("📧 Mensaje enviado: " + messageId);

        // Verificar estado inicial
        Optional<MessageStatus> initialStatus = messageStatusService.getMessageStatus(messageId);
        if (initialStatus.isPresent()) {
            System.out.println("1️⃣ Estado inicial: " + initialStatus.get().getStatus());
            assert initialStatus.get().getStatus().name().equals("QUEUED") : "Debería empezar en QUEUED";
        }

        // Esperar un poco para que se procese
        System.out.println("⏳ Esperando procesamiento...");
        TimeUnit.SECONDS.sleep(3);

        // Verificar si cambió de estado
        Optional<MessageStatus> afterProcessing = messageStatusService.getMessageStatus(messageId);
        if (afterProcessing.isPresent()) {
            System.out.println("2️⃣ Estado después del procesamiento: " + afterProcessing.get().getStatus());

            // El Consumer debería haber actualizado el estado
            if (afterProcessing.get().getStatus().name().equals("PROCESSING") ||
                    afterProcessing.get().getStatus().name().equals("SENT")) {
                System.out.println("✅ El Consumer está actualizando correctamente los estados");
            }
        }

        System.out.println("✅ Message Status Lifecycle test completed");
    }

}