// src/test/java/com/tfgproject/FutureSchedulingTest.java
package com.tfgproject;

import com.tfgproject.infrastructure.service.AsyncScheduledMessageProcessor;
import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class FutureSchedulingTest {

    @Autowired
    private AsyncMessagePublisher messagePublisher; // ✅ Cambiado a AsyncMessagePublisher

    @Autowired
    private AsyncScheduledMessageProcessor scheduledProcessor;

    @Test
    public void testSchedulingFor30SecondsInFuture() throws InterruptedException {
        System.out.println("🎯 === TEST PROGRAMACIÓN PARA EL FUTURO (30 SEGUNDOS) ===");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusSeconds(30);

        System.out.println("⏰ Hora actual: " + now.toString().substring(11, 19));
        System.out.println("🎯 Programado para: " + futureTime.toString().substring(11, 19));
        System.out.println("⏳ Diferencia: 30 segundos");

        // Programar mensaje para 30 segundos en el futuro
        String messageId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "🎯 Programado 30 segundos - " + now.toString().substring(11, 19),
                "Este mensaje fue programado a las " + now.toString().substring(11, 19) +
                        " para enviarse a las " + futureTime.toString().substring(11, 19) +
                        "\n\nSi recibes este email, el scheduler de futuro funciona correctamente!",
                futureTime
        );

        System.out.println("✅ Mensaje programado con ID: " + messageId);

        // Verificar que se almacenó
        int storedCount = scheduledProcessor.getScheduledMessageCount();
        System.out.println("📊 Mensajes en scheduler: " + storedCount);

        if (storedCount == 0) {
            System.out.println("❌ ERROR: Mensaje no se almacenó en scheduler");
            return;
        }

        // Monitorear cada 10 segundos durante 50 segundos
        System.out.println("\n🔍 === MONITOREANDO ENVÍO ===");
        System.out.println("⏱️  Verificando cada 10 segundos...");

        for (int i = 1; i <= 5; i++) { // 5 × 10 = 50 segundos
            TimeUnit.SECONDS.sleep(10);

            LocalDateTime currentTime = LocalDateTime.now();
            int currentCount = scheduledProcessor.getScheduledMessageCount();
            boolean shouldHaveSent = currentTime.isAfter(futureTime);

            System.out.printf("🕐 +%02ds | Hora: %s | Count: %d | ¿Debe enviarse?: %s\n",
                    i * 10,
                    currentTime.toString().substring(11, 19),
                    currentCount,
                    shouldHaveSent ? "SÍ" : "NO"
            );

            if (shouldHaveSent && currentCount < storedCount) {
                System.out.println("🎉 ¡ÉXITO! Mensaje programado enviado correctamente");
                System.out.println("📧 Revisa tu email: bax6351@gmail.com");
                System.out.println("✅ El scheduler de futuro funciona perfectamente");
                return;
            }
        }

        // Verificación final
        System.out.println("\n❌ RESULTADO:");
        int finalCount = scheduledProcessor.getScheduledMessageCount();
        if (finalCount == storedCount) {
            System.out.println("🚨 El mensaje NO se envió después de 50 segundos");
            System.out.println("🔍 Problema: El scheduler no procesa mensajes futuros");
        } else {
            System.out.println("✅ El mensaje se envió (count cambió de " + storedCount + " a " + finalCount + ")");
            System.out.println("📧 Revisa tu email");
        }
    }

    @Test
    public void testSchedulingFor45SecondsInFuture() throws InterruptedException {
        System.out.println("🎯 === TEST PROGRAMACIÓN PARA 45 SEGUNDOS ===");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusSeconds(45);

        System.out.println("⏰ Hora actual: " + now.toString().substring(11, 19));
        System.out.println("🎯 Programado para: " + futureTime.toString().substring(11, 19));

        // Programar mensaje
        String messageId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "🎯 Test 45 segundos - " + now.toString().substring(11, 19),
                "MENSAJE PROGRAMADO PARA EL FUTURO\n\n" +
                        "Este mensaje demuestra el scheduling real:\n" +
                        "• Programado: " + now.toString().substring(11, 19) + "\n" +
                        "• Debe enviarse: " + futureTime.toString().substring(11, 19) + "\n" +
                        "• Diferencia: 45 segundos\n\n" +
                        "Si recibes este email, ¡el scheduler funciona correctamente!",
                futureTime
        );

        System.out.println("✅ Mensaje programado: " + messageId);
        System.out.println("📊 Mensajes en scheduler: " + scheduledProcessor.getScheduledMessageCount());

        // Esperar y verificar más detalladamente
        System.out.println("\n🔍 === MONITOREO DETALLADO ===");
        System.out.println("👀 Observa los logs de consola buscando:");
        System.out.println("   📝 '🔍 Procesando X mensajes programados'");
        System.out.println("   📝 '⏰ Mensaje ... listo para envío'");
        System.out.println("   📝 '📧 Email programado enviado a cola'");

        int initialCount = scheduledProcessor.getScheduledMessageCount();

        // Verificar cada 5 segundos durante 60 segundos
        for (int i = 1; i <= 12; i++) { // 12 × 5 = 60 segundos
            TimeUnit.SECONDS.sleep(5);

            LocalDateTime currentTime = LocalDateTime.now();
            int currentCount = scheduledProcessor.getScheduledMessageCount();
            long secondsElapsed = java.time.Duration.between(now, currentTime).getSeconds();
            long secondsUntilSend = java.time.Duration.between(currentTime, futureTime).getSeconds();

            System.out.printf("🕐 +%02ds | Faltan: %02ds | Count: %d | Estado: %s\n",
                    secondsElapsed,
                    Math.max(0, secondsUntilSend),
                    currentCount,
                    secondsUntilSend <= 0 ? "DEBERÍA ENVIARSE" : "ESPERANDO"
            );

            // Si ya pasó el tiempo y el count disminuyó
            if (secondsUntilSend <= 0 && currentCount < initialCount) {
                System.out.println("🎉 ¡MENSAJE ENVIADO EXITOSAMENTE!");
                System.out.println("📧 El email debería haber llegado a: bax6351@gmail.com");
                System.out.println("✅ Scheduler de futuro funciona correctamente");
                return;
            }
        }

        System.out.println("\n📊 RESULTADO FINAL:");
        int finalCount = scheduledProcessor.getScheduledMessageCount();
        System.out.println("📊 Count inicial: " + initialCount);
        System.out.println("📊 Count final: " + finalCount);

        if (finalCount < initialCount) {
            System.out.println("✅ ¡ÉXITO! El mensaje se procesó");
            System.out.println("📧 Revisa tu email: bax6351@gmail.com");
        } else {
            System.out.println("❌ El mensaje NO se procesó");
            System.out.println("🚨 Hay un problema con el scheduler de futuro");
        }
    }

    @Test
    public void testMultipleFutureMessages() throws InterruptedException {
        System.out.println("🎯 === TEST MÚLTIPLES MENSAJES FUTUROS ===");

        LocalDateTime now = LocalDateTime.now();

        // Programar 3 mensajes en diferentes tiempos futuros
        String id1 = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "🎯 Futuro 1 - En 20 segundos",
                "Primer mensaje programado para " + now.plusSeconds(20).toString().substring(11, 19),
                now.plusSeconds(20)
        );

        String id2 = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "🎯 Futuro 2 - En 40 segundos",
                "Segundo mensaje programado para " + now.plusSeconds(40).toString().substring(11, 19),
                now.plusSeconds(40)
        );

        String id3 = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "🎯 Futuro 3 - En 60 segundos",
                "Tercer mensaje programado para " + now.plusSeconds(60).toString().substring(11, 19),
                now.plusSeconds(60)
        );

        System.out.println("✅ Tres mensajes programados:");
        System.out.println("   📧 " + id1 + " → +20s");
        System.out.println("   📧 " + id2 + " → +40s");
        System.out.println("   📧 " + id3 + " → +60s");

        int initialCount = scheduledProcessor.getScheduledMessageCount();
        System.out.println("📊 Total mensajes en scheduler: " + initialCount);

        // Monitorear durante 75 segundos
        System.out.println("\n🔍 === MONITOREANDO ENVÍOS ESCALONADOS ===");

        for (int i = 1; i <= 15; i++) { // 15 × 5 = 75 segundos
            TimeUnit.SECONDS.sleep(5);

            LocalDateTime currentTime = LocalDateTime.now();
            int currentCount = scheduledProcessor.getScheduledMessageCount();
            long elapsed = java.time.Duration.between(now, currentTime).getSeconds();

            String expectedSent = "";
            if (elapsed >= 20) expectedSent += "1";
            if (elapsed >= 40) expectedSent += "2";
            if (elapsed >= 60) expectedSent += "3";

            System.out.printf("🕐 +%02ds | Count: %d | Esperados enviados: %s\n",
                    elapsed, currentCount, expectedSent.isEmpty() ? "ninguno" : expectedSent);
        }

        System.out.println("\n📊 RESULTADO:");
        int finalCount = scheduledProcessor.getScheduledMessageCount();
        int sentCount = initialCount - finalCount;

        System.out.println("📊 Mensajes enviados: " + sentCount + "/3");
        System.out.println("📧 Revisa tu email para confirmar recepción");

        if (sentCount == 3) {
            System.out.println("🎉 ¡PERFECTO! Todos los mensajes se enviaron");
        } else if (sentCount > 0) {
            System.out.println("⚠️ Algunos mensajes se enviaron (" + sentCount + "/3)");
        } else {
            System.out.println("❌ Ningún mensaje se envió");
        }
    }

    @Test
    public void quickFutureTest() throws InterruptedException {
        System.out.println("⚡ === TEST RÁPIDO FUTURO (35 SEGUNDOS) ===");

        LocalDateTime scheduledTime = LocalDateTime.now().plusSeconds(35);

        System.out.println("⏰ Programando para: " + scheduledTime.toString().substring(11, 19));

        String messageId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⚡ Test Rápido Futuro",
                "Este mensaje fue programado para 35 segundos en el futuro.\n\n" +
                        "Si lo recibes, el scheduler funciona correctamente!",
                scheduledTime
        );

        System.out.println("✅ Programado: " + messageId);
        System.out.println("📊 En scheduler: " + scheduledProcessor.getScheduledMessageCount());

        // Esperar 45 segundos
        System.out.println("⏳ Esperando 45 segundos...");
        TimeUnit.SECONDS.sleep(45);

        int finalCount = scheduledProcessor.getScheduledMessageCount();
        System.out.println("📊 Count final: " + finalCount);

        if (finalCount == 0) {
            System.out.println("✅ ¡Mensaje enviado! Revisa tu email");
        } else {
            System.out.println("❌ Mensaje NO enviado");
        }
    }
}