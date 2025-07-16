// src/test/java/com/tfgproject/RabbitMQIntegrationTest.java
package com.tfgproject;

import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.application.command.SendSmsCommand;
import com.tfgproject.domain.model.MessageResult;
import com.tfgproject.domain.port.in.SendMessageUseCase;
import com.tfgproject.infrastructure.service.MessagePublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@TestPropertySource(properties = {
        "app.messaging.async=true",
        "spring.rabbitmq.host=localhost",
        "spring.rabbitmq.port=5672"
})
public class RabbitMQIntegrationTest {

    @Autowired
    private SendMessageUseCase sendMessageUseCase;

    @Autowired
    private MessagePublisher messagePublisher;

    @Test
    public void testCompleteRabbitMQFlow() throws InterruptedException {
        System.out.println("🚀 === TESTING COMPLETE RABBITMQ FLOW ===");

        // === CASO DE USO 01: ENVÍO ÚNICO EMAIL ===
        System.out.println("\n📧 1. TESTING EMAIL ÚNICO VIA RABBITMQ");

        SendEmailCommand emailCommand = new SendEmailCommand();
        emailCommand.setTo("bax6351@gmail.com");
        emailCommand.setSubject("🐰 RabbitMQ + Hexagonal - Email único");
        emailCommand.setText("Este email fue enviado usando RabbitMQ + Arquitectura Hexagonal");

        MessageResult emailResult = sendMessageUseCase.sendEmail(emailCommand);

        System.out.println("Email resultado: " + emailResult.getMessage());
        System.out.println("Email éxito: " + emailResult.isSuccess());
        assert emailResult.isSuccess() : "Email debería encolarse exitosamente";

        // === CASO DE USO 01: ENVÍO ÚNICO SMS ===
        System.out.println("\n📱 2. TESTING SMS ÚNICO VIA RABBITMQ");

        SendSmsCommand smsCommand = new SendSmsCommand();
        smsCommand.setTo("644023859");
        smsCommand.setText("SMS enviado via RabbitMQ + Arquitectura Hexagonal");
        smsCommand.setSender("TFG-App");

        MessageResult smsResult = sendMessageUseCase.sendSms(smsCommand);

        System.out.println("SMS resultado: " + smsResult.getMessage());
        System.out.println("SMS éxito: " + smsResult.isSuccess());
        assert smsResult.isSuccess() : "SMS debería encolarse exitosamente";

        // === CASO DE USO 02: DIFUSIÓN ===
        System.out.println("\n📢 3. TESTING DIFUSIÓN VIA RABBITMQ");

        String broadcastId = messagePublisher.broadcastMessage(
                Arrays.asList("bax6351@gmail.com", "644023859"),
                "Mensaje de difusión via RabbitMQ",
                "🚀 Difusión RabbitMQ + Hexagonal"
        );

        System.out.println("Difusión ID: " + broadcastId);
        assert broadcastId != null : "Difusión debería retornar ID";

        // === CASO DE USO 04: PROGRAMACIÓN ===
        System.out.println("\n⏰ 4. TESTING PROGRAMACIÓN VIA RABBITMQ");

        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(1);

        String scheduledId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⏰ Mensaje programado RabbitMQ",
                "Este mensaje fue programado para enviarse en 1 minuto",
                futureTime
        );

        System.out.println("Programación ID: " + scheduledId);
        System.out.println("Programado para: " + futureTime);
        assert scheduledId != null : "Programación debería retornar ID";

        // === ESPERAR PROCESAMIENTO ===
        System.out.println("\n⏳ 5. ESPERANDO PROCESAMIENTO DE COLAS...");
        TimeUnit.SECONDS.sleep(5);

        System.out.println("✅ === TESTING RABBITMQ COMPLETADO ===");
        System.out.println("📧 Revisa tu email: bax6351@gmail.com");
        System.out.println("📱 Revisa tu SMS: 644023859");
        System.out.println("⏰ El mensaje programado llegará en 1 minuto");

        System.out.println("\n🏗️ ARQUITECTURA UTILIZADA:");
        System.out.println("  Test → UseCase → Domain → Adapter → RabbitMQ → Consumer → Service");
        System.out.println("  ✅ Hexagonal Architecture");
        System.out.println("  ✅ Async Processing");
        System.out.println("  ✅ Message Queues");
        System.out.println("  ✅ Fault Tolerance");
        System.out.println("  ✅ Scalability");
    }

    @Test
    public void testRabbitMQDirectPublisher() {
        System.out.println("🐰 === TESTING DIRECT RABBITMQ PUBLISHER ===");

        // Test directo del publisher (sin arquitectura hexagonal)
        String emailId = messagePublisher.sendEmailToQueue(
                "bax6351@gmail.com",
                "🔥 Test directo RabbitMQ",
                "Este email fue enviado directamente al publisher"
        );

        System.out.println("Email ID: " + emailId);
        assert emailId != null : "Publisher debería retornar ID";

        String smsId = messagePublisher.sendSmsToQueue(
                "644023859",
                "SMS directo via RabbitMQ Publisher",
                "TFG-Direct"
        );

        System.out.println("SMS ID: " + smsId);
        assert smsId != null : "SMS Publisher debería retornar ID";

        System.out.println("✅ Direct Publisher tests passed");
    }

    @Test
    public void testRabbitMQFailureHandling() {
        System.out.println("🛡️ === TESTING FAILURE HANDLING ===");

        // Test con email inválido
        SendEmailCommand invalidEmail = new SendEmailCommand();
        invalidEmail.setTo(""); // Email vacío
        invalidEmail.setSubject("Test fallo");
        invalidEmail.setText("Este debería fallar");

        MessageResult result = sendMessageUseCase.sendEmail(invalidEmail);

        System.out.println("Resultado fallo: " + result.getMessage());
        System.out.println("¿Falló correctamente?: " + !result.isSuccess());

        assert !result.isSuccess() : "Email inválido debería fallar";

        System.out.println("✅ Failure handling test passed");
    }

    @Test
    public void testRabbitMQWithAttachment() {
        System.out.println("📎 === TESTING RABBITMQ WITH ATTACHMENT ===");

        SendEmailCommand commandWithAttachment = new SendEmailCommand();
        commandWithAttachment.setTo("bax6351@gmail.com");
        commandWithAttachment.setSubject("📎 RabbitMQ + Attachment");
        commandWithAttachment.setText("Email con attachment via RabbitMQ");
        commandWithAttachment.setAttachmentPath("/home/bryan/SD_Task_2-2024.pdf");

        MessageResult result = sendMessageUseCase.sendEmail(commandWithAttachment);

        System.out.println("Attachment resultado: " + result.getMessage());
        System.out.println("Attachment éxito: " + result.isSuccess());

        assert result.isSuccess() : "Email con attachment debería encolarse";

        System.out.println("✅ Attachment via RabbitMQ test passed");
    }
}