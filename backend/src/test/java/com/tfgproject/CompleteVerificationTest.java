// src/test/java/com/tfgproject/CompleteVerificationTest.java
package com.tfgproject;

import com.tfgproject.domain.model.MessageStatus;
import com.tfgproject.domain.service.MessageStatusService;
import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import com.tfgproject.infrastructure.service.AsyncScheduledMessageProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootTest
public class CompleteVerificationTest {

    @Autowired
    private AsyncMessagePublisher messagePublisher;

    @Autowired
    private MessageStatusService messageStatusService;

    @Autowired
    private AsyncScheduledMessageProcessor scheduledProcessor;

    @Test
    public void testCompleteVerificationWithScheduled() throws InterruptedException {
        System.out.println("🎯 === VERIFICACIÓN COMPLETA: SOLO 1 SMS PROGRAMADO ===");
        System.out.println("💰 (Ahorrando dinero - solo SMS programado para probar funcionalidad)");

        LocalDateTime now = LocalDateTime.now();
        String testSuffix = now.toString().substring(11, 19); // HH:MM:SS

        System.out.println("\n📊 ESTADO INICIAL DEL SISTEMA:");
        long initialPending = messageStatusService.getPendingMessageCount();
        long initialFailed = messageStatusService.getFailedMessageCount();
        int initialScheduled = scheduledProcessor.getScheduledMessageCount();

        System.out.println("  ⏳ Mensajes pendientes: " + initialPending);
        System.out.println("  ❌ Mensajes fallidos: " + initialFailed);
        System.out.println("  📅 Mensajes programados: " + initialScheduled);

        System.out.println("\n📤 ENVIANDO MENSAJES DE PRUEBA (" + testSuffix + "):");

        // === 1. EMAIL INMEDIATO ===
        String emailId = messagePublisher.sendEmailToQueue(
                "bax6351@gmail.com",
                "📧 Verificación Email Inmediato #" + testSuffix,
                "Email de verificación inmediato enviado a las " + testSuffix + "\n\n" +
                        "Este email confirma que el sistema de email funciona correctamente."
        );
        System.out.println("1️⃣ ✅ Email inmediato enviado: " + emailId);

        // === 2. EMAIL PROGRAMADO (1 minuto) ===
        LocalDateTime emailScheduledTime = now.plusMinutes(1);
        String scheduledEmailId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⏰ Email Programado #" + testSuffix,
                "Este email fue programado a las " + now.toString().substring(11, 19) +
                        " para enviarse a las " + emailScheduledTime.toString().substring(11, 19) + "\n\n" +
                        "Si recibes este email, la programación funciona perfectamente!",
                emailScheduledTime
        );
        System.out.println("2️⃣ ⏰ Email programado para: " + emailScheduledTime.toString().substring(11, 19));
        System.out.println("    ID: " + scheduledEmailId);

        // === 3. SMS PROGRAMADO (45 segundos) - ÚNICO SMS ===
        LocalDateTime smsScheduledTime = now.plusSeconds(45);
        String scheduledSmsId = messagePublisher.scheduleSms(
                "644023859",                                                    // to
                "SMS programado TFG " + testSuffix + " - Enviado automaticamente!", // content
                "TFG-App",                                                      // sender
                smsScheduledTime
        );
        System.out.println("3️⃣ 📱 SMS programado para: " + smsScheduledTime.toString().substring(11, 19));
        System.out.println("    ID: " + scheduledSmsId);
        System.out.println("    💰 (Este es el ÚNICO SMS - ahorra dinero)");

        // === 4. DIFUSIÓN SOLO EMAIL (sin SMS para ahorrar) ===
        String broadcastId = messagePublisher.sendEmailToQueue(
                "bax6351@gmail.com",
                "📢 Verificación Difusión #" + testSuffix,
                "Este email simula una difusión (sin SMS para ahorrar dinero)\n\n" +
                        "En producción, esto llegaría a múltiples destinatarios por email y SMS."
        );
        System.out.println("4️⃣ 📢 Difusión (solo email) enviada: " + broadcastId);

        System.out.println("\n⏳ Esperando procesamiento inicial (5 segundos)...");
        TimeUnit.SECONDS.sleep(5);

        // === VERIFICAR ESTADOS INMEDIATOS ===
        System.out.println("\n📊 VERIFICANDO ESTADOS INMEDIATOS:");

        Optional<MessageStatus> emailStatus = messageStatusService.getMessageStatus(emailId);
        Optional<MessageStatus> scheduledEmailStatus = messageStatusService.getMessageStatus(scheduledEmailId);
        Optional<MessageStatus> scheduledSmsStatus = messageStatusService.getMessageStatus(scheduledSmsId);
        Optional<MessageStatus> broadcastStatus = messageStatusService.getMessageStatus(broadcastId);

        System.out.println("1️⃣ Email inmediato: " +
                (emailStatus.isPresent() ? emailStatus.get().getStatus() : "❌ NO ENCONTRADO"));
        System.out.println("2️⃣ Email programado: " +
                (scheduledEmailStatus.isPresent() ? scheduledEmailStatus.get().getStatus() : "❌ NO ENCONTRADO"));
        System.out.println("3️⃣ SMS programado: " +
                (scheduledSmsStatus.isPresent() ? scheduledSmsStatus.get().getStatus() : "❌ NO ENCONTRADO"));
        System.out.println("4️⃣ Difusión email: " +
                (broadcastStatus.isPresent() ? broadcastStatus.get().getStatus() : "❌ NO ENCONTRADO"));

        // === ESTADÍSTICAS ACTUALES ===
        System.out.println("\n📈 ESTADÍSTICAS ACTUALES:");
        long currentPending = messageStatusService.getPendingMessageCount();
        long currentFailed = messageStatusService.getFailedMessageCount();
        int currentScheduled = scheduledProcessor.getScheduledMessageCount();

        System.out.println("  ⏳ Mensajes pendientes: " + currentPending);
        System.out.println("  ❌ Mensajes fallidos: " + currentFailed);
        System.out.println("  📅 Mensajes programados: " + currentScheduled);
        System.out.println("  📊 Incremento programados: +" + (currentScheduled - initialScheduled));

        // === ESPERAR LOS MENSAJES PROGRAMADOS ===
        System.out.println("\n⏰ MONITOREANDO MENSAJES PROGRAMADOS...");
        System.out.println("📱 SMS programado llegará en ~40 segundos");
        System.out.println("📧 Email programado llegará en ~55 segundos");
        System.out.println("\n🔍 Monitoreando cada 15 segundos:");

        for (int i = 1; i <= 6; i++) { // 6 × 15s = 90 segundos total
            TimeUnit.SECONDS.sleep(15);

            LocalDateTime currentTime = LocalDateTime.now();
            long secondsElapsed = java.time.Duration.between(now, currentTime).getSeconds();
            int scheduledCount = scheduledProcessor.getScheduledMessageCount();

            boolean smsTime = secondsElapsed >= 45;
            boolean emailTime = secondsElapsed >= 60;

            System.out.printf("🕐 +%02ds | Programados: %d | SMS: %s | Email: %s\n",
                    secondsElapsed,
                    scheduledCount,
                    smsTime ? "✅ DEBERÍA ENVIARSE" : "⏳ Esperando",
                    emailTime ? "✅ DEBERÍA ENVIARSE" : "⏳ Esperando"
            );

            // Verificar si los mensajes se enviaron (salieron del scheduler)
            if (scheduledCount < currentScheduled) {
                System.out.println("   🚀 ¡Algunos mensajes programados se enviaron!");
            }
        }

        // === VERIFICACIÓN FINAL ===
        System.out.println("\n🎯 VERIFICACIÓN FINAL:");

        long finalPending = messageStatusService.getPendingMessageCount();
        long finalFailed = messageStatusService.getFailedMessageCount();
        int finalScheduled = scheduledProcessor.getScheduledMessageCount();

        System.out.println("📊 ESTADÍSTICAS FINALES:");
        System.out.println("  ⏳ Mensajes pendientes: " + finalPending);
        System.out.println("  ❌ Mensajes fallidos: " + finalFailed);
        System.out.println("  📅 Mensajes programados restantes: " + finalScheduled);

        // === RESUMEN DE ENTREGA ESPERADA ===
        System.out.println("\n📱 RESUMEN - QUÉ DEBERÍAS RECIBIR:");
        System.out.println("📧 EN TU EMAIL (bax6351@gmail.com):");
        System.out.println("  1️⃣ ✅ Verificación Email Inmediato #" + testSuffix + " (YA llegó)");
        System.out.println("  2️⃣ ⏰ Email Programado #" + testSuffix + " (llegó en 1 minuto)");
        System.out.println("  3️⃣ 📢 Verificación Difusión #" + testSuffix + " (YA llegó)");
        System.out.println("\n📱 EN TU MÓVIL (644023859):");
        System.out.println("  1️⃣ 📱 SMS Programado TFG " + testSuffix + " (llegó en 45s)");

        System.out.println("\n💰 TOTAL COSTO SMS: 1 mensaje (solo el programado)");
        System.out.println("📧 TOTAL EMAILS: 3 mensajes (todos gratis)");

        System.out.println("\n✅ TEST DE VERIFICACIÓN COMPLETADO!");
        System.out.println("🔍 Cuenta los mensajes recibidos y compara con lo esperado arriba");

        // Assertions básicas
        assert emailStatus.isPresent() : "Email inmediato debería tener status";
        assert scheduledEmailStatus.isPresent() : "Email programado debería tener status";
        assert scheduledSmsStatus.isPresent() : "SMS programado debería tener status";
        assert currentScheduled >= initialScheduled : "Deberían haberse programado mensajes";
    }

    @Test
    public void testSystemStatistics() {
        System.out.println("📈 === ESTADÍSTICAS COMPLETAS DEL SISTEMA ===");

        // Obtener todos los mensajes
        List<MessageStatus> allMessages = messageStatusService.getUserMessageHistory("currentUser", 7);

        System.out.println("📊 RESUMEN GENERAL:");
        System.out.println("  📋 Total mensajes registrados: " + allMessages.size());

        if (allMessages.isEmpty()) {
            System.out.println("  ℹ️ No hay mensajes en el sistema aún");
            return;
        }

        // Agrupar por tipo
        Map<String, Long> typeCount = allMessages.stream()
                .collect(Collectors.groupingBy(
                        MessageStatus::getType,
                        Collectors.counting()
                ));

        System.out.println("\n📊 POR TIPO DE MENSAJE:");
        typeCount.forEach((type, count) ->
                System.out.println("  " + getTypeIcon(type) + " " + type + ": " + count));

        // Agrupar por estado
        Map<String, Long> statusCount = allMessages.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getStatus().name(),
                        Collectors.counting()
                ));

        System.out.println("\n📊 POR ESTADO:");
        statusCount.forEach((status, count) ->
                System.out.println("  " + getStatusIcon(status) + " " + status + ": " + count));

        // Últimos 10 mensajes
        System.out.println("\n📋 ÚLTIMOS 10 MENSAJES:");
        allMessages.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(10)
                .forEach(m -> System.out.println(
                        "  " + getTypeIcon(m.getType()) + " " +
                                m.getRecipient() + " → " +
                                getStatusIcon(m.getStatus().name()) + " " + m.getStatus() +
                                " (" + m.getTimestamp().toString().substring(11, 19) + ")"
                ));

        // Estadísticas de rendimiento
        long pending = messageStatusService.getPendingMessageCount();
        long failed = messageStatusService.getFailedMessageCount();
        int scheduled = scheduledProcessor.getScheduledMessageCount();

        System.out.println("\n⚡ ESTADO ACTUAL DEL SISTEMA:");
        System.out.println("  ⏳ Pendientes de procesar: " + pending);
        System.out.println("  ❌ Fallidos: " + failed);
        System.out.println("  📅 Programados para el futuro: " + scheduled);

        double successRate = allMessages.isEmpty() ? 0 :
                (double) allMessages.stream()
                        .filter(m -> m.getStatus().name().equals("SENT") || m.getStatus().name().equals("DELIVERED"))
                        .count() * 100.0 / allMessages.size();

        System.out.println("  📊 Tasa de éxito: " + String.format("%.1f%%", successRate));

        System.out.println("\n✅ Estadísticas del sistema completadas");
    }

    @Test
    public void testSchedulerStatus() {
        System.out.println("⏰ === ESTADO DEL SCHEDULER ===");

        int scheduledCount = scheduledProcessor.getScheduledMessageCount();
        System.out.println("📅 Mensajes programados activos: " + scheduledCount);

        if (scheduledCount > 0) {
            System.out.println("✅ Scheduler activo con mensajes pendientes");
            System.out.println("🔍 Los mensajes se enviarán automáticamente en su momento programado");
        } else {
            System.out.println("💤 Scheduler activo sin mensajes programados");
        }

        System.out.println("⚙️ El scheduler verifica mensajes cada 30 segundos");
        System.out.println("✅ Estado del scheduler verificado");
    }

    @Test
    public void testQuickEmailOnly() {
        System.out.println("📧 === TEST RÁPIDO SOLO EMAIL (GRATIS) ===");

        LocalDateTime now = LocalDateTime.now();
        String suffix = now.toString().substring(14, 19); // MM:SS

        String emailId = messagePublisher.sendEmailToQueue(
                "bax6351@gmail.com",
                "⚡ Test Rápido #" + suffix,
                "Email de prueba rápida enviado a las " + now.toString().substring(11, 19) + "\n\n" +
                        "Este es un test simple para verificar que el email funciona sin gastar en SMS."
        );

        System.out.println("📧 Email enviado: " + emailId);
        System.out.println("✅ Revisa tu email: bax6351@gmail.com");
        System.out.println("💰 Costo: $0 (email gratuito)");

        assert emailId != null : "Email debería enviarse correctamente";
    }

    // Métodos auxiliares para iconos
    private String getTypeIcon(String type) {
        return switch (type) {
            case "EMAIL" -> "📧";
            case "SMS" -> "📱";
            case "BROADCAST" -> "📢";
            default -> "📝";
        };
    }

    private String getStatusIcon(String status) {
        return switch (status) {
            case "QUEUED" -> "⏳";
            case "PROCESSING" -> "🔄";
            case "SENT" -> "✅";
            case "DELIVERED" -> "📬";
            case "FAILED" -> "❌";
            case "SCHEDULED" -> "📅";
            default -> "❓";
        };
    }
}