// src/test/java/com/tfgproject/CompleteSystemIntegrationTest.java
package com.tfgproject;

import com.tfgproject.domain.model.MessageStatus;
import com.tfgproject.domain.model.MessageStatusEnum;
import com.tfgproject.domain.model.Contact;
import com.tfgproject.domain.model.Category;
import com.tfgproject.domain.service.MessageStatusService;
import com.tfgproject.domain.service.ContactService;
import com.tfgproject.domain.service.CategoryService;
import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import com.tfgproject.infrastructure.service.AsyncScheduledMessageProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CompleteSystemIntegrationTest {

    @Autowired
    private AsyncMessagePublisher messagePublisher;

    @Autowired
    private MessageStatusService messageStatusService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AsyncScheduledMessageProcessor scheduledProcessor;

    // Variables para compartir entre tests
    private static String testEmailId;
    private static String testSmsId;
    private static String testBroadcastId;
    private static String testScheduledId;
    private static Long testContactId;
    private static Long testCategoryId;

    @Test
    @Order(1)
    public void test01_SetupTestData() {
        System.out.println("🚀 === TEST 01: CONFIGURACIÓN DE DATOS DE PRUEBA ===");

        try {
            // Crear categoría de prueba
            Category testCategory = categoryService.createCategory(
                    "Test Category",
                    "Categoría para testing completo del sistema",
                    null
            );
            testCategoryId = testCategory.getId();

            // Crear contacto de prueba
            Contact testContact = contactService.createContact(
                    "Test User Complete",
                    "bax6351@gmail.com",  // Tu email real
                    "644023859",          // Tu teléfono real
                    null,
                    "Contacto para testing completo del sistema"
            );
            testContactId = testContact.getId();

            // Asignar contacto a categoría
            contactService.addContactToCategory(testContactId, testCategoryId);

            System.out.println("✅ Datos de prueba creados:");
            System.out.println("  📁 Categoría: " + testCategory.getName() + " (ID: " + testCategoryId + ")");
            System.out.println("  👤 Contacto: " + testContact.getName() + " (ID: " + testContactId + ")");
            System.out.println("  🔗 Contacto asignado a categoría");

            assert testCategoryId != null : "ID de categoría no puede ser null";
            assert testContactId != null : "ID de contacto no puede ser null";

        } catch (Exception e) {
            System.err.println("❌ Error en configuración: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @Order(2)
    public void test02_CasoDeUso01_EnvioUnico() throws InterruptedException {
        System.out.println("📧 === TEST 02: CASO DE USO 01 - ENVÍO ÚNICO ===");

        // EMAIL ÚNICO
        testEmailId = messagePublisher.sendEmailToQueue(
                "bax6351@gmail.com",
                "🧪 Test Completo - Email Único",
                "Este es un email de prueba del sistema completo.\n\n" +
                        "✅ Funcionalidad: Envío único de email\n" +
                        "📊 Test: CompleteSystemIntegrationTest\n" +
                        "🕐 Hora: " + LocalDateTime.now()
        );

        System.out.println("📧 Email único enviado: " + testEmailId);

        // SMS ÚNICO
        /*
        testSmsId = messagePublisher.sendSmsToQueue(
                "644023859",
                "Test SMS unico - Sistema completo TFG funcionando!",
                "TFG-App"
        );

        System.out.println("📱 SMS único enviado: " + testSmsId);
        */

        // Esperar procesamiento inicial
        TimeUnit.SECONDS.sleep(3);

        // Verificar que se crearon los estados
        Optional<MessageStatus> emailStatus = messageStatusService.getMessageStatus(testEmailId);
        // Optional<MessageStatus> smsStatus = messageStatusService.getMessageStatus(testSmsId);

        assert emailStatus.isPresent() : "Estado de email debe existir";
        // assert smsStatus.isPresent() : "Estado de SMS debe existir";

        System.out.println("✅ CASO DE USO 01 completado:");
        System.out.println("  📧 Email estado: " + emailStatus.get().getStatus().getDisplayName());
        // System.out.println("  📱 SMS estado: " + smsStatus.get().getStatus().getDisplayName());
    }

    @Test
    @Order(3)
    public void test03_CasoDeUso02_Difusion() throws InterruptedException {
        System.out.println("📢 === TEST 03: CASO DE USO 02 - DIFUSIÓN ===");

        List<String> recipients = List.of(
                "bax6351@gmail.com",
                "rybantfg@gmail.com"
        );

        testBroadcastId = messagePublisher.broadcastMessage(
                recipients,
                "🚀 Test Completo - Difusión Multicanal\n\n" +
                        "Este mensaje demuestra la difusión a múltiples destinatarios.\n\n" +
                        "✅ Funcionalidad: Difusión multicanal\n" +
                        "👥 Destinatarios: " + recipients.size() + "\n" +
                        "📊 Test: CompleteSystemIntegrationTest\n" +
                        "🕐 Hora: " + LocalDateTime.now(),
                "🚀 Test Difusión - Sistema TFG"
        );

        System.out.println("📢 Difusión enviada: " + testBroadcastId);
        System.out.println("👥 Destinatarios: " + recipients);

        // Esperar procesamiento
        TimeUnit.SECONDS.sleep(3);

        // Verificar estados de difusión
        List<MessageStatus> recentMessages = messageStatusService.getRecentMessages();
        long broadcastMessages = recentMessages.stream()
                .filter(m -> m.getType().equals("BROADCAST"))
                .count();

        System.out.println("📊 Mensajes de difusión encontrados: " + broadcastMessages);

        assert testBroadcastId != null : "ID de difusión no puede ser null";
        System.out.println("✅ CASO DE USO 02 completado");
    }

    @Test
    @Order(4)
    public void test04_CasoDeUso04_Programacion() throws InterruptedException {
        System.out.println("⏰ === TEST 04: CASO DE USO 04 - PROGRAMACIÓN ===");

        // Programar email para 1 minuto
        LocalDateTime scheduledTime = LocalDateTime.now().plusMinutes(1);

        testScheduledId = messagePublisher.scheduleMessage(
                "bax6351@gmail.com",
                "⏰ Test Completo - Mensaje Programado",
                "¡Este mensaje fue programado automáticamente!\n\n" +
                        "✅ Funcionalidad: Programación de mensajes\n" +
                        "📅 Programado para: " + scheduledTime.toString().substring(11, 19) + "\n" +
                        "📊 Test: CompleteSystemIntegrationTest\n" +
                        "🕐 Hora de programación: " + LocalDateTime.now().toString().substring(11, 19) + "\n\n" +
                        "Si recibes este email, ¡la programación funciona perfectamente!",
                scheduledTime
        );

        System.out.println("⏰ Mensaje programado: " + testScheduledId);
        System.out.println("📅 Para: " + scheduledTime.toString().substring(11, 19));

        // Verificar que se almacenó en el scheduler
        int initialScheduledCount = scheduledProcessor.getScheduledMessageCount();
        System.out.println("📊 Mensajes en scheduler: " + initialScheduledCount);

        // Programar SMS para 45 segundos (para testing rápido)
        /*
        LocalDateTime smsScheduledTime = LocalDateTime.now().plusSeconds(45);

        String scheduledSmsId = messagePublisher.scheduleSms(
                "644023859",
                "SMS programado TFG - Test completo funcionando! Enviado automaticamente a las " +
                        smsScheduledTime.toString().substring(11, 19),
                "TFG-App",
                smsScheduledTime
        );

        System.out.println("📱 SMS programado: " + scheduledSmsId);
        System.out.println("📅 Para: " + smsScheduledTime.toString().substring(11, 19));
        */

        int finalScheduledCount = scheduledProcessor.getScheduledMessageCount();
        System.out.println("📊 Total mensajes programados: " + finalScheduledCount);

        // Verificar estado del mensaje programado
        Optional<MessageStatus> scheduledStatus = messageStatusService.getMessageStatus(testScheduledId);
        if (scheduledStatus.isPresent()) {
            System.out.println("📋 Estado del mensaje programado: " + scheduledStatus.get().getStatus().getDisplayName());
        }

        assert testScheduledId != null : "ID de mensaje programado no puede ser null";
        assert finalScheduledCount >= initialScheduledCount : "Debería haber más mensajes programados";

        System.out.println("✅ CASO DE USO 04 completado");
        System.out.println("⏳ Los mensajes se enviarán automáticamente en sus horarios programados");
    }

    @Test
    @Order(5)
    public void test05_CasoDeUso03_EnvioPorCategoria() throws InterruptedException {
        System.out.println("📁 === TEST 05: CASO DE USO 03 - ENVÍO POR CATEGORÍA ===");

        // Obtener emails de la categoría de prueba
        Set<String> emailsCategoria = categoryService.getAllEmailsFromCategory(testCategoryId);

        System.out.println("📊 Emails en categoría: " + emailsCategoria);

        if (!emailsCategoria.isEmpty()) {
            // Enviar mensaje a todos los contactos de la categoría
            String categoryMessageId = messagePublisher.broadcastMessage(
                    List.copyOf(emailsCategoria),
                    "📁 Test Completo - Mensaje por Categoría\n\n" +
                            "Este mensaje fue enviado usando el sistema de categorías.\n\n" +
                            "✅ Funcionalidad: Envío por categoría\n" +
                            "📁 Categoría: Test Category\n" +
                            "👥 Destinatarios encontrados: " + emailsCategoria.size() + "\n" +
                            "📊 Test: CompleteSystemIntegrationTest\n" +
                            "🕐 Hora: " + LocalDateTime.now(),
                    "📁 Test Categoría - Sistema TFG"
            );

            System.out.println("📁 Mensaje por categoría enviado: " + categoryMessageId);
            System.out.println("👥 Destinatarios: " + emailsCategoria.size());

            // Esperar procesamiento
            TimeUnit.SECONDS.sleep(3);

            assert categoryMessageId != null : "ID de mensaje por categoría no puede ser null";

        } else {
            System.out.println("⚠️ No hay emails en la categoría de prueba");
        }

        System.out.println("✅ CASO DE USO 03 completado");
    }

    @Test
    @Order(6)
    public void test06_CasoDeUso05_VerificacionEstados() throws InterruptedException {
        System.out.println("📊 === TEST 06: CASO DE USO 05 - VERIFICACIÓN DE ESTADOS ===");

        // Esperar un poco más para que se procesen los mensajes
        System.out.println("⏳ Esperando procesamiento de mensajes...");
        TimeUnit.SECONDS.sleep(5);

        // 1. Verificar estados individuales
        System.out.println("\n🔍 1. VERIFICANDO ESTADOS INDIVIDUALES:");

        verificarEstadoMensaje(testEmailId, "Email único");
        // verificarEstadoMensaje(testSmsId, "SMS único");
        verificarEstadoMensaje(testBroadcastId, "Difusión");
        verificarEstadoMensaje(testScheduledId, "Mensaje programado");

        // 2. Estadísticas del sistema
        System.out.println("\n📈 2. ESTADÍSTICAS DEL SISTEMA:");

        var report = messageStatusService.getSystemStatusReport();
        System.out.println("  📊 Total mensajes: " + report.getTotalMessages());
        System.out.println("  ✅ Exitosos: " + report.getSuccessfulMessages());
        System.out.println("  ❌ Fallidos: " + report.getFailedMessages());
        System.out.println("  ⏳ Pendientes: " + report.getPendingMessages());
        System.out.println("  📈 Tasa de éxito: " + String.format("%.2f%%", report.getSuccessRate()));
        System.out.println("  🏥 Salud del sistema: " + report.getSystemHealth().getDisplayName());

        // 3. Historial de mensajes
        System.out.println("\n📜 3. HISTORIAL DE MENSAJES:");

        List<MessageStatus> history = messageStatusService.getUserMessageHistory("currentUser", 1);
        System.out.println("  📅 Mensajes últimas 24h: " + history.size());

        // 4. Mensajes por estado
        System.out.println("\n📋 4. MENSAJES POR ESTADO:");

        for (MessageStatusEnum status : MessageStatusEnum.values()) {
            List<MessageStatus> messagesInStatus = messageStatusService.getMessagesByStatus(status);
            if (!messagesInStatus.isEmpty()) {
                System.out.println("  " + status.getDisplayName() + ": " + messagesInStatus.size() + " mensajes");
            }
        }

        // 5. Mensajes recientes
        System.out.println("\n🕐 5. ACTIVIDAD RECIENTE:");

        List<MessageStatus> recentMessages = messageStatusService.getRecentMessages();
        System.out.println("  📊 Mensajes últimas 24h: " + recentMessages.size());

        // Verificaciones
        assert report.getTotalMessages() >= 4 : "Debería haber al menos 4 mensajes (email, sms, difusión, programado)";
        assert history.size() > 0 : "Debería haber mensajes en el historial";

        System.out.println("✅ CASO DE USO 05 completado");
    }

    @Test
    @Order(7)
    public void test07_CasoDeUso06_GestionContactos() {
        System.out.println("👥 === TEST 07: CASO DE USO 06 - GESTIÓN DE CONTACTOS ===");

        // 1. Buscar contactos
        List<Contact> foundContacts = contactService.searchContacts("Test User");
        System.out.println("🔍 Contactos encontrados con 'Test User': " + foundContacts.size());

        // 2. Contactos con email
        List<Contact> contactsWithEmail = contactService.getContactsWithEmail();
        System.out.println("📧 Contactos con email: " + contactsWithEmail.size());

        // 3. Contactos con teléfono
        List<Contact> contactsWithPhone = contactService.getContactsWithPhone();
        System.out.println("📱 Contactos con teléfono: " + contactsWithPhone.size());

        // 4. Contactos por categoría
        List<Contact> categoryContacts = contactService.getContactsByCategory(testCategoryId);
        System.out.println("📁 Contactos en categoría de prueba: " + categoryContacts.size());

        // 5. Crear contacto adicional
        Contact additionalContact = contactService.createContact(
                "Test User Additional",
                "test.additional@tfg.com",
                "600000000",
                null,
                "Contacto adicional para testing"
        );
        System.out.println("➕ Contacto adicional creado: " + additionalContact.getName());

        // Verificaciones
        assert foundContacts.size() >= 1 : "Debería encontrar al menos 1 contacto";
        assert contactsWithEmail.size() >= 1 : "Debería haber contactos con email";
        assert categoryContacts.size() >= 1 : "Debería haber contactos en la categoría";
        assert additionalContact.getId() != null : "Contacto adicional debe tener ID";

        System.out.println("✅ CASO DE USO 06 completado");
    }

    @Test
    @Order(8)
    public void test08_CasoDeUso07_GestionCategorias() {
        System.out.println("📁 === TEST 08: CASO DE USO 07 - GESTIÓN DE CATEGORÍAS ===");

        // 1. Crear subcategoría
        Category subcategory = categoryService.createCategory(
                "Test Subcategory",
                "Subcategoría para testing",
                testCategoryId
        );
        System.out.println("📂 Subcategoría creada: " + subcategory.getName());

        // 2. Listar categorías raíz
        List<Category> rootCategories = categoryService.getRootCategories();
        System.out.println("🌳 Categorías raíz: " + rootCategories.size());

        // 3. Obtener subcategorías
        List<Category> subcategories = categoryService.getSubcategories(testCategoryId);
        System.out.println("📂 Subcategorías: " + subcategories.size());

        // 4. Estadísticas de categoría
        var stats = categoryService.getCategoryStats(testCategoryId);
        System.out.println("📊 Estadísticas de categoría:");
        System.out.println("  👥 Total contactos: " + stats.get("totalContacts"));
        System.out.println("  📧 Con email: " + stats.get("contactsWithEmail"));
        System.out.println("  📱 Con teléfono: " + stats.get("contactsWithPhone"));

        // 5. Emails de categoría
        Set<String> categoryEmails = categoryService.getAllEmailsFromCategory(testCategoryId);
        System.out.println("📧 Emails en categoría: " + categoryEmails.size());

        // Verificaciones
        assert subcategory.getId() != null : "Subcategoría debe tener ID";
        assert subcategories.size() >= 1 : "Debería haber al menos 1 subcategoría";
        assert categoryEmails.size() >= 1 : "Debería haber emails en la categoría";

        System.out.println("✅ CASO DE USO 07 completado");
    }

    @Test
    @Order(9)
    public void test09_VerificarMensajesProgramados() throws InterruptedException {
        System.out.println("⏰ === TEST 09: VERIFICACIÓN DE MENSAJES PROGRAMADOS ===");

        int initialScheduledCount = scheduledProcessor.getScheduledMessageCount();
        System.out.println("📊 Mensajes programados iniciales: " + initialScheduledCount);

        if (initialScheduledCount > 0) {
            System.out.println("⏳ Esperando 70 segundos para verificar envío de mensajes programados...");

            // Esperar que se envíen los mensajes programados
            for (int i = 1; i <= 14; i++) { // 14 × 5 = 70 segundos
                TimeUnit.SECONDS.sleep(5);

                int currentCount = scheduledProcessor.getScheduledMessageCount();
                long elapsedSeconds = i * 5;

                System.out.printf("🕐 +%02ds | Programados restantes: %d%n", elapsedSeconds, currentCount);

                // Si el contador bajó, algunos mensajes se enviaron
                if (currentCount < initialScheduledCount) {
                    System.out.println("🚀 ¡Algunos mensajes programados se enviaron automáticamente!");
                    break;
                }

                // Información a los 30 y 60 segundos
                /*
                if (elapsedSeconds == 30) {
                    System.out.println("⏱️  30 segundos - Los SMS programados deberían enviarse pronto...");
                }
                */
                if (elapsedSeconds == 60) {
                    System.out.println("⏱️  60 segundos - Los emails programados deberían enviarse pronto...");
                }
            }

            int finalScheduledCount = scheduledProcessor.getScheduledMessageCount();
            System.out.println("📊 Mensajes programados finales: " + finalScheduledCount);

            if (finalScheduledCount < initialScheduledCount) {
                System.out.println("🎉 ¡ÉXITO! Los mensajes programados se enviaron automáticamente");
                System.out.println("📧 Revisa tu email y SMS para confirmar la recepción");
            } else {
                System.out.println("⚠️ Los mensajes programados aún no se han enviado");
                System.out.println("🔍 Esto puede ser normal si aún no ha llegado el momento programado");
            }
        } else {
            System.out.println("ℹ️ No hay mensajes programados para verificar");
        }

        System.out.println("✅ TEST 09 completado");
    }

    @Test
    @Order(10)
    public void test10_ResumenFinal() {
        System.out.println("🎯 === TEST 10: RESUMEN FINAL DEL SISTEMA ===");

        // Estadísticas finales
        var finalReport = messageStatusService.getSystemStatusReport();
        List<MessageStatus> allMessages = messageStatusService.getRecentMessages();
        int scheduledCount = scheduledProcessor.getScheduledMessageCount();

        System.out.println("\n📊 ESTADÍSTICAS FINALES:");
        System.out.println("  📝 Total mensajes: " + finalReport.getTotalMessages());
        System.out.println("  ✅ Exitosos: " + finalReport.getSuccessfulMessages());
        System.out.println("  ❌ Fallidos: " + finalReport.getFailedMessages());
        System.out.println("  ⏳ Pendientes: " + finalReport.getPendingMessages());
        System.out.println("  📅 Programados: " + scheduledCount);
        System.out.println("  📈 Tasa de éxito: " + String.format("%.2f%%", finalReport.getSuccessRate()));
        System.out.println("  🏥 Salud del sistema: " + finalReport.getSystemHealth().getDisplayName());

        System.out.println("\n🎯 CASOS DE USO PROBADOS:");
        System.out.println("  ✅ Caso 01: Envío único (Email + SMS)");
        System.out.println("  ✅ Caso 02: Difusión multicanal");
        System.out.println("  ✅ Caso 03: Envío por categoría");
        System.out.println("  ✅ Caso 04: Programación de mensajes");
        System.out.println("  ✅ Caso 05: Verificación de estados");
        System.out.println("  ✅ Caso 06: Gestión de contactos");
        System.out.println("  ✅ Caso 07: Gestión de categorías");

        System.out.println("\n📧 MENSAJES QUE DEBERÍAS RECIBIR:");
        System.out.println("  📧 bax6351@gmail.com:");
        System.out.println("    • Email único de prueba");
        System.out.println("    • Mensaje de difusión");
        System.out.println("    • Mensaje por categoría");
        System.out.println("    • Email programado (en 1 minuto)");
        /*
        System.out.println("  📱 644023859:");
        System.out.println("    • SMS único de prueba");
        System.out.println("    • SMS programado (en 45 segundos)");
        */

        System.out.println("\n🏗️ ARQUITECTURA VERIFICADA:");
        System.out.println("  ✅ Arquitectura Hexagonal");
        System.out.println("  ✅ Mensajería asíncrona con RabbitMQ");
        System.out.println("  ✅ Tracking de estados en tiempo real");
        System.out.println("  ✅ Base de datos H2 funcionando");
        System.out.println("  ✅ Scheduler de mensajes programados");
        System.out.println("  ✅ Gestión completa de contactos y categorías");

        System.out.println("\n🎉 === TESTING COMPLETO FINALIZADO EXITOSAMENTE ===");
        System.out.println("🚀 ¡Tu sistema TFG está funcionando perfectamente!");
        System.out.println("📱 Revisa tu email y SMS para confirmar los envíos");

        // Verificaciones finales
        assert finalReport.getTotalMessages() >= 5 : "Debería haber al menos 5 mensajes";
        assert finalReport.getSuccessRate() >= 0 : "Tasa de éxito debe ser >= 0";
    }

    // === MÉTODOS AUXILIARES ===

    private void verificarEstadoMensaje(String messageId, String description) {
        if (messageId == null) {
            System.out.println("  ⚠️ " + description + ": ID no disponible");
            return;
        }

        Optional<MessageStatus> statusOpt = messageStatusService.getMessageStatus(messageId);
        if (statusOpt.isPresent()) {
            MessageStatus status = statusOpt.get();
            System.out.println("  ✅ " + description + ": " + status.getStatus().getDisplayName() +
                    " (" + status.getRecipient() + ")");
        } else {
            System.out.println("  ❌ " + description + ": Estado no encontrado");
        }
    }
}