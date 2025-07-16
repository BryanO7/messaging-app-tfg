// src/test/java/com/tfgproject/DebugScheduledTest.java
package com.tfgproject;

import com.tfgproject.infrastructure.service.MessagePublisher;
import com.tfgproject.infrastructure.service.ScheduledMessageProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class DebugScheduledTest {

    @Autowired
    private MessagePublisher messagePublisher;

    @Autowired
    private ScheduledMessageProcessor scheduledMessageProcessor;

    @Test
    public void debugScheduledEmailFlow() throws InterruptedException {
        System.out.println("🔍 === DEBUG COMPLETO DE PROGRAMACIÓN ===");

        // === PASO 1: VERIFICAR ESTADO INICIAL ===
        System.out.println("\n1️⃣ ESTADO INICIAL:");
        System.out.println("📊 Mensajes programados en memoria: " + scheduledMessageProcessor.getScheduledMessageCount());
        System.out.println("⏰ Hora actual: " + LocalDateTime.now());

        // === PASO 2: PROGRAMAR MENSAJE ===
        System.out.println("\n2️⃣ PROGRAMANDO MENSAJE:");

        LocalDateTime scheduledTime = LocalDateTime.now().plusMinutes(1);
        System.out.println("🎯 Programado para: " + scheduledTime);

        String messageId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "🔍 DEBUG - Email programado para 1 minuto",
                "Este email es para debuggear el sistema de programación.\n\n" +
                        "Hora de programación: " + LocalDateTime.now() + "\n" +
                        "Hora esperada de envío: " + scheduledTime + "\n\n" +
                        "Si recibes este email, el sistema funciona correctamente!",
                scheduledTime
        );

        System.out.println("✅ Mensaje programado con ID: " + messageId);

        // === PASO 3: VERIFICAR QUE SE ALMACENÓ ===
        System.out.println("\n3️⃣ VERIFICANDO ALMACENAMIENTO:");
        TimeUnit.SECONDS.sleep(2); // Esperar que se procese

        int messageCount = scheduledMessageProcessor.getScheduledMessageCount();
        System.out.println("📊 Mensajes en memoria después de programar: " + messageCount);

        if (messageCount > 0) {
            System.out.println("✅ El mensaje se almacenó correctamente en memoria");
        } else {
            System.out.println("❌ ERROR: El mensaje NO se almacenó en memoria");
            System.out.println("🚨 Problema en: ScheduledMessageConsumer o ScheduledMessageProcessor");
        }

        // === PASO 4: SIMULAR CHECKING MANUAL ===
        System.out.println("\n4️⃣ SIMULANDO VERIFICACIÓN MANUAL:");
        System.out.println("⏰ Hora actual: " + LocalDateTime.now());
        System.out.println("🎯 Hora programada: " + scheduledTime);
        System.out.println("⏳ Esperando hasta el momento programado...");

        // Esperar hasta que llegue el momento + 5 segundos de buffer
        long waitTime = java.time.Duration.between(LocalDateTime.now(), scheduledTime).toMillis() + 5000;
        if (waitTime > 0) {
            System.out.println("⏳ Esperando " + (waitTime/1000) + " segundos...");
            TimeUnit.MILLISECONDS.sleep(waitTime);
        }

        // === PASO 5: VERIFICAR QUE SE PROCESÓ ===
        System.out.println("\n5️⃣ VERIFICANDO PROCESAMIENTO:");
        System.out.println("⏰ Hora actual: " + LocalDateTime.now());

        int remainingMessages = scheduledMessageProcessor.getScheduledMessageCount();
        System.out.println("📊 Mensajes restantes en memoria: " + remainingMessages);

        if (remainingMessages == 0) {
            System.out.println("✅ El mensaje se procesó y removió de memoria");
            System.out.println("📧 Debería haber llegado el email!");
        } else {
            System.out.println("❌ ERROR: El mensaje NO se procesó");
            System.out.println("🚨 Problema en: ScheduledMessageProcessor.processScheduledMessages()");
        }

        // === PASO 6: VERIFICAR LOGS ===
        System.out.println("\n6️⃣ VERIFICAR LOGS:");
        System.out.println("🔍 Revisa los logs de consola buscando:");
        System.out.println("   📧 'Enviando email a cola: bax6351@gmail.com'");
        System.out.println("   🐰 'Email encolado exitosamente'");
        System.out.println("   📤 'Procesando email de la cola'");
        System.out.println("   ✅ 'Email enviado exitosamente'");

        System.out.println("\n🎯 === DEBUG COMPLETADO ===");
        System.out.println("📧 Si no hay errores arriba, revisa tu email: bax6351@gmail.com");
        System.out.println("📁 También revisa carpeta SPAM/PROMOCIONES");

        assert messageId != null : "Debería retornar un ID de mensaje";
    }

    @Test
    public void testScheduledProcessorDirectly() throws InterruptedException {
        System.out.println("🔧 === TEST DIRECTO DEL PROCESADOR ===");

        // Crear un mensaje programado manualmente
        com.tfgproject.shared.model.QueueMessage testMessage =
                com.tfgproject.shared.model.QueueMessage.forEmail(
                        "bax6351@gmail.com",
                        "🔧 Test directo del procesador",
                        "Este email fue creado directamente para testing"
                );

        LocalDateTime scheduledTime = LocalDateTime.now().plusSeconds(30);
        testMessage.setScheduledTime(scheduledTime);

        System.out.println("⏰ Agregando mensaje programado para: " + scheduledTime);

        // Agregar directamente al procesador
        scheduledMessageProcessor.addScheduledMessage(testMessage);

        System.out.println("✅ Mensaje agregado al procesador");
        System.out.println("📊 Total mensajes: " + scheduledMessageProcessor.getScheduledMessageCount());

        // Esperar que se procese
        System.out.println("⏳ Esperando 35 segundos para procesamiento...");
        TimeUnit.SECONDS.sleep(35);

        System.out.println("📊 Mensajes restantes: " + scheduledMessageProcessor.getScheduledMessageCount());
        System.out.println("📧 Revisa tu email para ver si llegó");
    }
}