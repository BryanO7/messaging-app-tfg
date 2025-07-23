// src/test/java/com/tfgproject/SchedulerRealTimeVerificationTest.java
package com.tfgproject;

import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import com.tfgproject.infrastructure.service.AsyncScheduledMessageProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class SchedulerRealTimeVerificationTest {

    @Autowired
    private AsyncMessagePublisher asyncPublisher;

    @Autowired
    private AsyncScheduledMessageProcessor scheduledProcessor;

    @Test
    public void testSchedulerStep1_ProgramMessage() {
        System.out.println("🚀 === PASO 1: PROGRAMAR MENSAJE ===");

        LocalDateTime scheduledTime = LocalDateTime.now().plusMinutes(1); // Solo 1 minuto para probar

        System.out.println("⏰ Hora actual: " + LocalDateTime.now());
        System.out.println("🎯 Programando para: " + scheduledTime);

        String messageId = asyncPublisher.scheduleMessage(
                "bax6351@gmail.com",
                "🔍 Test Scheduler Real-Time",
                "Este email debería llegar en exactamente 1 minuto desde: " + LocalDateTime.now(),
                scheduledTime
        );

        System.out.println("✅ Mensaje programado con ID: " + messageId);
        System.out.println("📊 Mensajes en scheduler: " + scheduledProcessor.getScheduledMessageCount());

        System.out.println("\n🔍 === ESTADO ACTUAL ===");
        System.out.println("📋 Mensajes programados en memoria: " + scheduledProcessor.getScheduledMessageCount());
        System.out.println("⏳ El mensaje se enviará automáticamente en 1 minuto");
        System.out.println("📧 Revisa tu email: bax6351@gmail.com en 1-2 minutos");

        assert messageId != null;
        assert scheduledProcessor.getScheduledMessageCount() > 0;
    }

    @Test
    public void testSchedulerStep2_WaitAndVerify() throws InterruptedException {
        System.out.println("⏳ === PASO 2: ESPERAR Y VERIFICAR ===");

        // Primero programar el mensaje
        LocalDateTime scheduledTime = LocalDateTime.now().plusSeconds(90); // 1.5 minutos

        System.out.println("📅 Programando mensaje para: " + scheduledTime);

        String messageId = asyncPublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⏳ Test con Verificación en Tiempo Real",
                "Este mensaje fue programado a las " + LocalDateTime.now() +
                        " para enviarse a las " + scheduledTime,
                scheduledTime
        );

        System.out.println("✅ Mensaje programado: " + messageId);
        int initialCount = scheduledProcessor.getScheduledMessageCount();
        System.out.println("📊 Mensajes iniciales en scheduler: " + initialCount);

        // Esperar y verificar cada 30 segundos
        for (int i = 1; i <= 3; i++) {
            System.out.println("\n🕐 === VERIFICACIÓN " + i + " (+" + (i * 30) + " segundos) ===");
            TimeUnit.SECONDS.sleep(30);

            LocalDateTime now = LocalDateTime.now();
            int currentCount = scheduledProcessor.getScheduledMessageCount();

            System.out.println("⏰ Hora actual: " + now);
            System.out.println("🎯 Hora objetivo: " + scheduledTime);
            System.out.println("📊 Mensajes en scheduler: " + currentCount);

            if (now.isAfter(scheduledTime)) {
                System.out.println("🚀 ¡Ya pasó la hora programada!");
                if (currentCount < initialCount) {
                    System.out.println("✅ ¡Mensaje enviado! (removido del scheduler)");
                    System.out.println("📧 Revisa tu email: bax6351@gmail.com");
                    break;
                } else {
                    System.out.println("⚠️ El mensaje aún está en el scheduler...");
                }
            } else {
                long minutesLeft = java.time.Duration.between(now, scheduledTime).toMinutes();
                System.out.println("⏳ Faltan " + minutesLeft + " minutos");
            }
        }

        System.out.println("\n🎯 === VERIFICACIÓN FINAL ===");
        System.out.println("📊 Mensajes finales en scheduler: " + scheduledProcessor.getScheduledMessageCount());
        System.out.println("📧 Si el contador bajó, el email fue enviado exitosamente");
    }

    @Test
    public void testSchedulerWithLogging() throws InterruptedException {
        System.out.println("📝 === TEST CON LOGGING DETALLADO ===");

        // Programar mensaje para 45 segundos (tiempo corto para testing)
        LocalDateTime scheduledTime = LocalDateTime.now().plusSeconds(45);

        System.out.println("📅 Programando mensaje para: " + scheduledTime);

        String messageId = asyncPublisher.scheduleMessage(
                "bax6351@gmail.com",
                "📝 Test Logging Detallado",
                "Mensaje programado para testing detallado. Hora: " + LocalDateTime.now(),
                scheduledTime
        );

        System.out.println("✅ Mensaje programado: " + messageId);

        // Monitorear por 1 minuto
        System.out.println("\n🔍 Monitoreando por 60 segundos...");
        System.out.println("👀 Observa los logs de consola para ver:");
        System.out.println("   📝 '🔍 Verificando mensajes programados...'");
        System.out.println("   📝 '⏰ ¡Es hora de enviar mensaje programado!'");
        System.out.println("   📝 '📧 Enviando email programado a cola'");
        System.out.println("   📝 '✅ Mensaje programado enviado y removido'");

        for (int i = 0; i < 12; i++) { // 12 × 5 segundos = 60 segundos
            TimeUnit.SECONDS.sleep(5);

            LocalDateTime now = LocalDateTime.now();
            int count = scheduledProcessor.getScheduledMessageCount();

            System.out.printf("🕐 +%02ds | Scheduler: %d mensajes | Estado: %s\n",
                    i * 5,
                    count,
                    now.isAfter(scheduledTime) ? "DEBERÍA HABERSE ENVIADO" : "ESPERANDO"
            );
        }

        System.out.println("\n📧 ¡Revisa tu email: bax6351@gmail.com!");
        System.out.println("📋 Mensajes finales en scheduler: " + scheduledProcessor.getScheduledMessageCount());
    }

    @Test
    public void testMultipleScheduledMessages() {
        System.out.println("📅 === TEST MÚLTIPLES MENSAJES PROGRAMADOS ===");

        LocalDateTime now = LocalDateTime.now();

        // Programar 3 mensajes en diferentes tiempos
        String id1 = asyncPublisher.scheduleMessage(
                "bax6351@gmail.com",
                "📅 Mensaje 1 - 1 minuto",
                "Primer mensaje programado para " + now.plusMinutes(1),
                now.plusMinutes(1)
        );

        String id2 = asyncPublisher.scheduleMessage(
                "bax6351@gmail.com",
                "📅 Mensaje 2 - 2 minutos",
                "Segundo mensaje programado para " + now.plusMinutes(2),
                now.plusMinutes(2)
        );

        String id3 = asyncPublisher.scheduleMessage(
                "bax6351@gmail.com",
                "📅 Mensaje 3 - 3 minutos",
                "Tercer mensaje programado para " + now.plusMinutes(3),
                now.plusMinutes(3)
        );

        System.out.println("✅ Tres mensajes programados:");
        System.out.println("   📧 " + id1 + " → " + now.plusMinutes(1));
        System.out.println("   📧 " + id2 + " → " + now.plusMinutes(2));
        System.out.println("   📧 " + id3 + " → " + now.plusMinutes(3));

        System.out.println("📊 Total en scheduler: " + scheduledProcessor.getScheduledMessageCount());
        System.out.println("⏰ Los emails llegarán en 1, 2 y 3 minutos respectivamente");

        assert scheduledProcessor.getScheduledMessageCount() >= 3;
    }
}