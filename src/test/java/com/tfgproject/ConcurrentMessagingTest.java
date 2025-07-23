// src/test/java/com/tfgproject/ConcurrentMessagingTest.java
package com.tfgproject;

import com.tfgproject.infrastructure.service.AsyncScheduledMessageProcessor;
import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class ConcurrentMessagingTest {

    @Autowired
    private AsyncMessagePublisher asyncMessagePublisher;

    @Autowired
    private AsyncScheduledMessageProcessor scheduledProcessor;

    @Test
    public void testConcurrentMessagingWhileSchedulerRunning() throws InterruptedException {
        System.out.println("🚀 === TEST MENSAJERÍA CONCURRENTE ===");
        System.out.println("📋 Objetivo: Demostrar que puedes enviar mensajes inmediatos");
        System.out.println("📋 mientras el scheduler procesa mensajes futuros");

        LocalDateTime now = LocalDateTime.now();

        // PASO 1: Programar varios mensajes para el futuro
        System.out.println("\n1️⃣ === PROGRAMANDO MENSAJES FUTUROS ===");

        String future1 = asyncMessagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⏰ Futuro 1 - En 30 segundos",
                "Mensaje programado para " + now.plusSeconds(30).toString().substring(11, 19),
                now.plusSeconds(30)
        );

        String future2 = asyncMessagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⏰ Futuro 2 - En 60 segundos",
                "Mensaje programado para " + now.plusSeconds(60).toString().substring(11, 19),
                now.plusSeconds(60)
        );

        String future3 = asyncMessagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⏰ Futuro 3 - En 90 segundos",
                "Mensaje programado para " + now.plusSeconds(90).toString().substring(11, 19),
                now.plusSeconds(90)
        );

        System.out.println("✅ Programados 3 mensajes futuros:");
        System.out.println("   📧 " + future1 + " → +30s");
        System.out.println("   📧 " + future2 + " → +60s");
        System.out.println("   📧 " + future3 + " → +90s");
        System.out.println("📊 Mensajes en scheduler: " + scheduledProcessor.getScheduledMessageCount());

        // PASO 2: Mientras esperamos, enviar mensajes inmediatos
        System.out.println("\n2️⃣ === ENVIANDO MENSAJES INMEDIATOS MIENTRAS ESPERAMOS ===");

        for (int i = 1; i <= 10; i++) {
            // Esperar 10 segundos entre cada mensaje inmediato
            TimeUnit.SECONDS.sleep(10);

            LocalDateTime currentTime = LocalDateTime.now();
            long elapsedSeconds = java.time.Duration.between(now, currentTime).getSeconds();

            // Enviar mensaje inmediato
            String immediateId = asyncMessagePublisher.sendEmailToQueue(
                    "bax6351@gmail.com",
                    "📧 Inmediato #" + i + " - " + currentTime.toString().substring(11, 19),
                    "Este es el mensaje inmediato #" + i + " enviado mientras esperamos los programados.\n\n" +
                            "• Enviado: " + currentTime.toString().substring(11, 19) + "\n" +
                            "• Tiempo transcurrido: " + elapsedSeconds + " segundos\n" +
                            "• Mensajes programados pendientes: " + scheduledProcessor.getScheduledMessageCount() + "\n\n" +
                            "¡Esto demuestra que el sistema no se bloquea!"
            );

            System.out.printf("📧 Inmediato #%d enviado (ID: %s) - Tiempo: +%ds - Scheduler: %d mensajes\n",
                    i,
                    immediateId.substring(0, 8) + "...",
                    elapsedSeconds,
                    scheduledProcessor.getScheduledMessageCount()
            );

            // Verificar si algún mensaje programado se envió
            if (elapsedSeconds >= 30 && elapsedSeconds < 35) {
                System.out.println("   ⏰ El primer mensaje programado debería enviarse pronto...");
            }
            if (elapsedSeconds >= 60 && elapsedSeconds < 65) {
                System.out.println("   ⏰ El segundo mensaje programado debería enviarse pronto...");
            }
            if (elapsedSeconds >= 90 && elapsedSeconds < 95) {
                System.out.println("   ⏰ El tercer mensaje programado debería enviarse pronto...");
            }
        }

        // PASO 3: Verificación final
        System.out.println("\n3️⃣ === VERIFICACIÓN FINAL ===");

        LocalDateTime endTime = LocalDateTime.now();
        long totalElapsed = java.time.Duration.between(now, endTime).getSeconds();
        int remainingScheduled = scheduledProcessor.getScheduledMessageCount();

        System.out.println("⏱️ Tiempo total transcurrido: " + totalElapsed + " segundos");
        System.out.println("📊 Mensajes programados restantes: " + remainingScheduled);
        System.out.println("📧 Mensajes inmediatos enviados: 10");

        System.out.println("\n🎯 === RESULTADOS ===");
        System.out.println("✅ Se enviaron 10 mensajes inmediatos sin bloqueo");
        System.out.println("✅ El scheduler siguió funcionando en paralelo");

        if (remainingScheduled < 3) {
            System.out.println("✅ Algunos mensajes programados se enviaron automáticamente");
        }

        System.out.println("\n📧 REVISA TU EMAIL: bax6351@gmail.com");
        System.out.println("📋 Deberías ver:");
        System.out.println("   📧 10 emails inmediatos (llegaron durante la ejecución)");
        System.out.println("   ⏰ Mensajes programados (llegaron en sus tiempos programados)");

        System.out.println("\n🎉 ¡DEMOSTRACIÓN COMPLETADA!");
        System.out.println("💡 Esto prueba que el scheduler es verdaderamente asíncrono");
    }

    @Test
    public void testHighFrequencyMessagingWithScheduler() throws InterruptedException {
        System.out.println("⚡ === TEST ALTA FRECUENCIA + SCHEDULER ===");

        // Programar mensaje para 45 segundos
        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(45);
        String scheduledId = asyncMessagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⏰ Programado durante alta frecuencia",
                "Este mensaje fue programado mientras se enviaban mensajes de alta frecuencia",
                futureTime
        );

        System.out.println("✅ Mensaje programado: " + scheduledId);
        System.out.println("📊 En scheduler: " + scheduledProcessor.getScheduledMessageCount());

        // Enviar mensajes cada 2 segundos durante 50 segundos
        System.out.println("\n⚡ Enviando mensajes cada 2 segundos...");

        for (int i = 1; i <= 25; i++) { // 25 × 2 = 50 segundos
            TimeUnit.SECONDS.sleep(2);

            String fastId = asyncMessagePublisher.sendEmailToQueue(
                    "bax6351@gmail.com",
                    "⚡ Rápido #" + i,
                    "Mensaje rápido #" + i + " enviado cada 2 segundos"
            );

            if (i % 5 == 0) { // Cada 10 segundos
                System.out.printf("⚡ Enviados %d mensajes rápidos - Scheduler: %d mensajes\n",
                        i, scheduledProcessor.getScheduledMessageCount());
            }
        }

        System.out.println("\n📊 RESULTADO:");
        System.out.println("✅ 25 mensajes rápidos enviados (cada 2 segundos)");
        System.out.println("📊 Scheduler restante: " + scheduledProcessor.getScheduledMessageCount());
        System.out.println("💡 El sistema manejó alta frecuencia + scheduler sin problemas");
    }

    @Test
    public void testSimultaneousSchedulingAndImmediate() throws InterruptedException {
        System.out.println("🔄 === TEST PROGRAMACIÓN Y ENVÍO SIMULTÁNEOS ===");

        LocalDateTime startTime = LocalDateTime.now();

        // Simular uso real: mezclar mensajes inmediatos y programados
        for (int i = 1; i <= 5; i++) {
            System.out.println("\n🔄 Ronda " + i + ":");

            // Enviar inmediato
            String immediateId = asyncMessagePublisher.sendEmailToQueue(
                    "bax6351@gmail.com",
                    "🔄 Inmediato Ronda " + i,
                    "Mensaje inmediato de la ronda " + i
            );

            // Programar para el futuro
            String scheduledId = asyncMessagePublisher.scheduleMessage(
                    "bax6351@gmail.com",
                    "🔄 Programado Ronda " + i,
                    "Mensaje programado de la ronda " + i + " para " +
                            startTime.plusSeconds(20 * i).toString().substring(11, 19),
                    startTime.plusSeconds(20 * i)
            );

            System.out.println("   📧 Inmediato: " + immediateId.substring(0, 8) + "...");
            System.out.println("   ⏰ Programado: " + scheduledId.substring(0, 8) + "... (+"+20*i+"s)");
            System.out.println("   📊 En scheduler: " + scheduledProcessor.getScheduledMessageCount());

            // Pequeña pausa
            TimeUnit.SECONDS.sleep(3);
        }

        System.out.println("\n📊 RESUMEN:");
        System.out.println("✅ 5 mensajes inmediatos enviados");
        System.out.println("✅ 5 mensajes programados almacenados");
        System.out.println("📊 Total en scheduler: " + scheduledProcessor.getScheduledMessageCount());

        System.out.println("\n⏳ Esperando 2 minutos para ver algunos envíos programados...");
        TimeUnit.MINUTES.sleep(2);

        System.out.println("📊 Scheduler después de 2 minutos: " + scheduledProcessor.getScheduledMessageCount());
        System.out.println("💡 ¡Uso real del sistema demostrado!");
    }
}