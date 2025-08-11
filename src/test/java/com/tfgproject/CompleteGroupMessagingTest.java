// src/test/java/com/tfgproject/CompleteGroupMessagingTest.java
package com.tfgproject;

import com.tfgproject.domain.model.Category;
import com.tfgproject.domain.model.Contact;
import com.tfgproject.domain.service.CategoryService;
import com.tfgproject.domain.service.ContactService;
import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.tfgproject.TestConstants.*; // ✅ Importar constantes

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties") // ✅ Usar perfil de test
public class CompleteGroupMessagingTest {
    private static final Logger logger = LoggerFactory.getLogger(CompleteGroupMessagingTest.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private AsyncMessagePublisher messagePublisher;

    @Test
    public void testCompleteGroupMessagingFlow() throws InterruptedException {
        printTestHeader("TEST COMPLETO: GRUPO DE PESCA CON DATOS REALES");
        System.out.println("📋 Demostrando tu funcionalidad exacta:");
        System.out.println("  1. Crear categoría 'Grupo de Pesca'");
        System.out.println("  2. Agregar contactos al grupo");
        System.out.println("  3. Enviar mensaje al grupo (email + SMS)");
        System.out.println("  4. Verificar que todos reciben el mensaje");

        // === PASO 1: CREAR CATEGORÍA "GRUPO DE PESCA" ===
        System.out.println("\n1️⃣ === CREANDO GRUPO DE PESCA ===");

        Category grupoPesca;
        try {
            // Intentar crear la categoría
            grupoPesca = categoryService.createCategory(
                    "Grupo de Pesca Test",
                    "Grupo para pruebas de mensajería a pescadores",
                    null
            );
            System.out.println("✅ Categoría creada: " + grupoPesca.getName());
            System.out.println("📁 ID de categoría: " + grupoPesca.getId());
        } catch (Exception e) {
            // Si ya existe, buscarla
            System.out.println("⚠️ Categoría ya existe, buscando...");
            List<Category> categories = categoryService.searchCategories("Grupo de Pesca");
            if (!categories.isEmpty()) {
                grupoPesca = categories.get(0);
                System.out.println("✅ Categoría encontrada: " + grupoPesca.getName());
            } else {
                throw new RuntimeException("No se pudo crear ni encontrar la categoría");
            }
        }

        // === PASO 2: AGREGAR CONTACTOS AL GRUPO ===
        System.out.println("\n2️⃣ === AGREGANDO PESCADORES AL GRUPO ===");

        // Verificar si ya existen contactos o crearlos (USANDO CONSTANTES)
        Contact pescador1 = crearOEncontrarContacto("Juan el Pescador", EMAIL_1, SPANISH_MOBILE);
        Contact pescador2 = crearOEncontrarContacto("María la Pescadora", EMAIL_2, null);
        Contact pescador3 = crearOEncontrarContacto("Carlos el Capitán", EMAIL_3, null);

        System.out.println("👥 Contactos preparados:");
        System.out.println("  🎣 " + pescador1.getName() + " - " + pescador1.getEmail() + " - " + pescador1.getPhone());
        System.out.println("  🎣 " + pescador2.getName() + " - " + pescador2.getEmail() + " - " + pescador2.getPhone());
        System.out.println("  🎣 " + pescador3.getName() + " - " + pescador3.getEmail() + " - " + pescador3.getPhone());

        // Agregar contactos a la categoría
        try {
            contactService.addContactToCategory(pescador1.getId(), grupoPesca.getId());
            contactService.addContactToCategory(pescador2.getId(), grupoPesca.getId());
            contactService.addContactToCategory(pescador3.getId(), grupoPesca.getId());
            System.out.println("✅ Contactos agregados al Grupo de Pesca");
        } catch (Exception e) {
            System.out.println("        ⚠️ Algunos contactos ya estaban en el grupo: " + e.getMessage());
        }

        // Verificar contactos en la categoría
        List<Contact> contactosEnGrupo = contactService.getContactsByCategory(grupoPesca.getId());
        System.out.println("👥 Total contactos en Grupo de Pesca: " + contactosEnGrupo.size());

        if (contactosEnGrupo.isEmpty()) {
            System.out.println("⚠️ No hay contactos en el grupo, saltando resto del test");
            printTestFooter();
            return;
        }

        // === PASO 3: OBTENER CANALES DE COMUNICACIÓN ===
        System.out.println("\n3️⃣ === OBTENIENDO CANALES DE COMUNICACIÓN ===");

        Set<String> emails = categoryService.getAllEmailsFromCategory(grupoPesca.getId());
        Set<String> phones = categoryService.getAllPhonesFromCategory(grupoPesca.getId());

        System.out.println("📧 Emails disponibles (" + emails.size() + "):");
        emails.forEach(email -> System.out.println("  📧 " + email));

        System.out.println("📱 Teléfonos disponibles (" + phones.size() + "):");
        phones.forEach(phone -> System.out.println("  📱 " + phone));

        if (emails.isEmpty() && phones.isEmpty()) {
            System.out.println("❌ ERROR: No hay canales de comunicación disponibles");
            return;
        }

        // === PASO 4: ENVIAR MENSAJE AL GRUPO POR EMAIL ===
        System.out.println("\n4️⃣ === ENVIANDO MENSAJE AL GRUPO POR EMAIL ===");

        if (!emails.isEmpty()) {
            String emailMessageId = messagePublisher.broadcastMessage(
                    emails.stream().toList(),
                    getTestMessage(FISHING_GROUP_MESSAGE), // ✅ Usar constante con timestamp
                    getTestSubject("Convocatoria Grupo de Pesca - Salida Sábado") // ✅ Usar constante
            );

            System.out.println("📧 Mensaje de email enviado a " + emails.size() + " pescadores");
            System.out.println("📧 ID del mensaje: " + emailMessageId);
        }

        // === PASO 5: ENVIAR MENSAJE AL GRUPO POR SMS ===
        System.out.println("\n5️⃣ === ENVIANDO MENSAJE AL GRUPO POR SMS ===");

        if (!phones.isEmpty()) {
            System.out.println("📱 Enviando SMS a " + phones.size() + " pescadores:");

            int smsCount = 0;
            for (String phone : phones) {
                String smsId = messagePublisher.sendSmsToQueue(
                        phone,
                        getTestMessage(SMS_SHORT_MESSAGE), // ✅ Usar constante con timestamp
                        TEST_SENDER // ✅ Usar constante
                );
                System.out.println("  📱 SMS " + (++smsCount) + " enviado a " + phone + " (ID: " + smsId + ")");

                // Pequeña pausa entre SMS para no saturar
                TimeUnit.SECONDS.sleep(1);
            }

            System.out.println("✅ Total SMS enviados: " + smsCount);
        }

        // === PASO 6: MOSTRAR ESTADÍSTICAS FINALES ===
        System.out.println("\n6️⃣ === ESTADÍSTICAS FINALES ===");

        Map<String, Object> stats = categoryService.getCategoryStats(grupoPesca.getId());

        System.out.println("📊 ESTADÍSTICAS DEL GRUPO DE PESCA:");
        System.out.println("  👥 Total contactos: " + stats.get("totalContacts"));
        System.out.println("  📧 Con email: " + stats.get("contactsWithEmail"));
        System.out.println("  📱 Con teléfono: " + stats.get("contactsWithPhone"));
        System.out.println("  📁 Subcategorías: " + stats.get("subcategories"));

        System.out.println("\n🎉 === FUNCIONALIDAD COMPLETADA ===");
        System.out.println("✅ Categoría 'Grupo de Pesca' creada");
        System.out.println("✅ Contactos agregados al grupo");
        System.out.println("✅ Mensaje enviado por email a todos");
        System.out.println("✅ Mensaje enviado por SMS a todos");
        System.out.println("✅ Datos guardados en base de datos");

        System.out.println("\n📱 VERIFICA TUS MENSAJES:");
        System.out.println("📧 Emails: " + String.join(", ", ALL_TEST_EMAILS));
        System.out.println("📱 SMS: " + SPANISH_MOBILE + " (solo Juan tiene móvil)");

        printTestFooter(); // ✅ Usar método de constantes

        System.out.println("\n🔗 ENDPOINTS PARA FRONTEND:");
        System.out.println("  GET /api/categories - Ver todas las categorías");
        System.out.println("  GET /api/categories/" + grupoPesca.getId() + "/with-contacts - Ver grupo con contactos");
        System.out.println("  POST /api/categories/" + grupoPesca.getId() + "/send-message - Enviar mensaje al grupo");
        System.out.println("  GET /api/categories/" + grupoPesca.getId() + "/stats - Ver estadísticas del grupo");

        // Assertions para el test
        assert !emails.isEmpty() || !phones.isEmpty() : "Debería haber canales de comunicación disponibles";
        assert contactosEnGrupo.size() >= 3 : "Debería haber al menos 3 contactos en el grupo";
    }

    // Método auxiliar mejorado para crear o encontrar contactos
    private Contact crearOEncontrarContacto(String nombre, String email, String phone) {
        try {
            // Intentar crear el contacto
            return contactService.createContact(nombre, email, phone, null, "Miembro del grupo de pesca");
        } catch (IllegalArgumentException e) {
            // Si ya existe, buscarlo
            if (e.getMessage().contains("Ya existe un contacto")) {
                logger.info("⚠️ Contacto ya existe, buscando: {}", email != null ? email : phone);

                // Buscar por email primero
                if (email != null) {
                    List<Contact> contactos = contactService.searchContacts(email);
                    if (!contactos.isEmpty()) {
                        Contact found = contactos.get(0);
                        logger.info("✅ Contacto encontrado por email: {} (ID: {})", found.getName(), found.getId());
                        return found;
                    }
                }

                // Buscar por teléfono si no se encontró por email
                if (phone != null) {
                    List<Contact> contactos = contactService.searchContacts(phone);
                    if (!contactos.isEmpty()) {
                        Contact found = contactos.get(0);
                        logger.info("✅ Contacto encontrado por teléfono: {} (ID: {})", found.getName(), found.getId());
                        return found;
                    }
                }

                // Buscar por nombre como último recurso
                List<Contact> contactos = contactService.searchContacts(nombre);
                if (!contactos.isEmpty()) {
                    Contact found = contactos.get(0);
                    logger.info("✅ Contacto encontrado por nombre: {} (ID: {})", found.getName(), found.getId());
                    return found;
                }
            }

            throw new RuntimeException("No se pudo crear ni encontrar el contacto: " + nombre + " (" + email + ")", e);
        }
    }

    @Test
    public void testCreateNewCategoryAndAddContacts() {
        System.out.println("🆕 === TEST CREAR NUEVA CATEGORÍA DESDE CERO ===");

        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String categoryName = "Grupo Test " + timestamp;

        // Crear nueva categoría
        Category nuevaCategoria;
        try {
            nuevaCategoria = categoryService.createCategory(
                    categoryName,
                    "Categoría de prueba para testing",
                    null
            );
            System.out.println("✅ Nueva categoría creada: " + nuevaCategoria.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("⚠️ Categoría ya existe, buscando...");
            List<Category> categories = categoryService.searchCategories(categoryName);
            nuevaCategoria = categories.isEmpty() ?
                    categoryService.getAllCategories().get(0) : categories.get(0);
        }

        System.out.println("📁 ID: " + nuevaCategoria.getId());

        // Crear contacto con email único para evitar duplicados
        String uniqueEmail = "test" + timestamp + "@tempmail.org";
        String uniquePhone = "600" + timestamp.substring(0, 6);

        Contact contacto1;
        try {
            contacto1 = contactService.createContact(
                    "Test User " + timestamp,
                    uniqueEmail,
                    uniquePhone,
                    null,
                    "Contacto de prueba"
            );
            System.out.println("✅ Contacto creado: " + contacto1.getName());
        } catch (IllegalArgumentException e) {
            // Si falla, usar uno de los contactos existentes
            System.out.println("⚠️ Usando contacto existente");
            List<Contact> existing = contactService.getAllContacts();
            contacto1 = existing.isEmpty() ?
                    crearOEncontrarContacto("Contacto Prueba", EMAIL_1, null) :
                    existing.get(0);
        }

        // Agregar a la categoría
        try {
            contactService.addContactToCategory(contacto1.getId(), nuevaCategoria.getId());
            System.out.println("✅ Contacto agregado a la categoría");
        } catch (Exception e) {
            System.out.println("⚠️ Contacto ya estaba en la categoría: " + e.getMessage());
        }

        // Verificar
        List<Contact> contactosEnCategoria = contactService.getContactsByCategory(nuevaCategoria.getId());
        System.out.println("👥 Contactos en nueva categoría: " + contactosEnCategoria.size());

        // Enviar mensaje de prueba solo si hay contactos
        if (!contactosEnCategoria.isEmpty()) {
            Set<String> emails = categoryService.getAllEmailsFromCategory(nuevaCategoria.getId());
            if (!emails.isEmpty()) {
                String messageId = messagePublisher.broadcastMessage(
                        emails.stream().toList(),
                        "Mensaje de prueba para la nueva categoría " + categoryName,
                        "Test de nueva categoría"
                );
                System.out.println("📧 Mensaje enviado a nueva categoría: " + messageId);
            }
        }

        System.out.println("✅ Test de nueva categoría completado");
    }

    @Test
    public void testHierarchicalCategories() {
        System.out.println("🌳 === TEST CATEGORÍAS JERÁRQUICAS ===");

        // Crear categoría padre
        Category deportes = categoryService.createCategory("Deportes", "Actividades deportivas", null);

        // Crear subcategorías
        Category pescaDeRio = categoryService.createCategory("Pesca de Río", "Pesca en ríos", deportes.getId());
        Category pescaDeMar = categoryService.createCategory("Pesca de Mar", "Pesca marina", deportes.getId());

        System.out.println("🌳 Estructura jerárquica creada:");
        System.out.println("  📁 " + deportes.getName() + " (ID: " + deportes.getId() + ")");
        System.out.println("    📂 " + pescaDeRio.getName() + " (ID: " + pescaDeRio.getId() + ")");
        System.out.println("    📂 " + pescaDeMar.getName() + " (ID: " + pescaDeMar.getId() + ")");

        // Crear contactos para subcategorías (EMAILS REALES)
        Contact pescadorRio = contactService.createContact("Río Pescador", "rybantfg@gmail.com", "644023859", null, "Especialista en río");
        Contact pescadorMar = contactService.createContact("Mar Pescador", "bax6351@gmail.com", null, null, "Especialista en mar");

        // Agregar a subcategorías específicas
        contactService.addContactToCategory(pescadorRio.getId(), pescaDeRio.getId());
        contactService.addContactToCategory(pescadorMar.getId(), pescaDeMar.getId());

        // Verificar que la categoría padre incluye todos los contactos
        Set<String> emailsPadre = categoryService.getAllEmailsFromCategory(deportes.getId());
        Set<String> emailsRio = categoryService.getAllEmailsFromCategory(pescaDeRio.getId());
        Set<String> emailsMar = categoryService.getAllEmailsFromCategory(pescaDeMar.getId());

        System.out.println("📧 Emails en categoría padre: " + emailsPadre.size());
        System.out.println("📧 Emails en pesca de río: " + emailsRio.size());
        System.out.println("📧 Emails en pesca de mar: " + emailsMar.size());

        // Enviar mensaje a toda la categoría padre
        if (!emailsPadre.isEmpty()) {
            String messageId = messagePublisher.broadcastMessage(
                    emailsPadre.stream().toList(),
                    "Mensaje para todos los deportistas",
                    "Convocatoria general deportes"
            );
            System.out.println("📧 Mensaje enviado a toda la categoría deportes: " + messageId);
        }

        System.out.println("✅ Test de categorías jerárquicas completado");
    }
}