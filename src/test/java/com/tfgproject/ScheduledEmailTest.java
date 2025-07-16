// src/test/java/com/tfgproject/ScheduledEmailTest.java
package com.tfgproject;

import com.tfgproject.infrastructure.service.MessagePublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class ScheduledEmailTest {

    @Autowired
    private MessagePublisher messagePublisher;

    @Test
    public void testScheduledEmailIn10Minutes() throws InterruptedException {
        System.out.println("⏰ === PROGRAMANDO EMAIL PARA 10 MINUTOS ===");

        // Programar para 10 minutos
        LocalDateTime in10Minutes = LocalDateTime.now().plusMinutes(10);

        String messageId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⏰ Email programado para 10 minutos - " + LocalDateTime.now(),
                "¡Este email fue programado para enviarse en 10 minutos! \n\n" +
                        "Hora de programación: " + LocalDateTime.now() + "\n" +
                        "Hora de envío: " + in10Minutes + "\n\n" +
                        "Si recibes este email, ¡el sistema de programación funciona perfectamente!",
                in10Minutes
        );

        System.out.println("✅ Email programado exitosamente");
        System.out.println("📧 ID del mensaje: " + messageId);
        System.out.println("⏰ Programado para: " + in10Minutes);
        System.out.println("🎯 El email se enviará automáticamente en 10 minutos");

        assert messageId != null : "Debería retornar un ID de mensaje";
    }

    @Test
    public void testScheduledEmailTomorrow() {
        System.out.println("🌅 === PROGRAMANDO EMAIL PARA MAÑANA ===");

        // Programar para mañana a las 9:00 AM
        LocalDateTime tomorrowAt9AM = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0);

        String messageId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "🌅 Buenos días - Email programado para mañana",
                "¡Buenos días! \n\n" +
                        "Este email fue programado ayer para enviarse hoy a las 9:00 AM. \n\n" +
                        "Hora de programación: " + LocalDateTime.now() + "\n" +
                        "Hora de envío: " + tomorrowAt9AM + "\n\n" +
                        "¡El sistema de programación funciona perfectamente!",
                tomorrowAt9AM
        );

        System.out.println("✅ Email programado para mañana");
        System.out.println("📧 ID del mensaje: " + messageId);
        System.out.println("⏰ Programado para: " + tomorrowAt9AM);
        System.out.println("🌅 Te llegará mañana a las 9:00 AM");

        assert messageId != null : "Debería retornar un ID de mensaje";
    }

    @Test
    public void testScheduledEmailIn2Minutes() throws InterruptedException {
        System.out.println("⚡ === PROGRAMANDO EMAIL PARA 2 MINUTOS (PARA PROBAR RÁPIDO) ===");

        // Programar para 2 minutos (para probar rápido)
        LocalDateTime in2Minutes = LocalDateTime.now().plusMinutes(1);

        String messageId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⚡ Test rápido - Email en 2 minutos",
                "¡Este email fue programado para enviarse en 2 minutos! \n\n" +
                        "Hora de programación: " + LocalDateTime.now() + "\n" +
                        "Hora de envío: " + in2Minutes + "\n\n" +
                        "¡Perfecto para probar que el sistema funciona!",
                in2Minutes
        );

        System.out.println("✅ Email programado para 2 minutos");
        System.out.println("📧 ID del mensaje: " + messageId);
        System.out.println("⏰ Programado para: " + in2Minutes);
        System.out.println("⚡ Espera 2 minutos y revisa tu email!");

        // Opcional: esperar y ver si llega
        System.out.println("⏳ Esperando 2 minutos para confirmar envío...");
        TimeUnit.MINUTES.sleep(1);
        TimeUnit.SECONDS.sleep(10); // 10 segundos adicionales para procesamiento

        System.out.println("📧 ¡El email debería haber llegado! Revisa tu bandeja: bax6351@gmail.com");

        assert messageId != null : "Debería retornar un ID de mensaje";
    }

    @Test
    public void testMultipleScheduledEmails() {
        System.out.println("📅 === PROGRAMANDO MÚLTIPLES EMAILS ===");

        // Email en 5 minutos
        LocalDateTime in5Minutes = LocalDateTime.now().plusMinutes(5);
        String id1 = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "📅 Email #1 - En 5 minutos",
                "Este es el primer email programado para 5 minutos",
                in5Minutes
        );

        // Email en 1 hora
        LocalDateTime in1Hour = LocalDateTime.now().plusHours(1);
        String id2 = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "📅 Email #2 - En 1 hora",
                "Este es el segundo email programado para 1 hora",
                in1Hour
        );

        // Email mañana
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        String id3 = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "📅 Email #3 - Mañana",
                "Este es el tercer email programado para mañana",
                tomorrow
        );

        System.out.println("✅ Tres emails programados:");
        System.out.println("📧 ID 1: " + id1 + " - En 5 minutos");
        System.out.println("📧 ID 2: " + id2 + " - En 1 hora");
        System.out.println("📧 ID 3: " + id3 + " - Mañana");

        System.out.println("⏰ Todos se enviarán automáticamente en su momento programado");

        assert id1 != null && id2 != null && id3 != null : "Todos deberían retornar IDs";
    }
}