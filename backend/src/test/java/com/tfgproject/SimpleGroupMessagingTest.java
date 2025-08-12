// src/test/java/com/tfgproject/SimpleGroupMessagingTest.java
package com.tfgproject;

import com.tfgproject.domain.service.ContactService;
import com.tfgproject.domain.service.CategoryService;
import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import com.tfgproject.domain.model.Contact;
import com.tfgproject.domain.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")  // ✅ ESTA es la forma correcta de activar el perfil
public class SimpleGroupMessagingTest {

    @Autowired
    private ContactService contactService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AsyncMessagePublisher messagePublisher;

    @Test
    public void testSimpleGroupMessaging() {
        System.out.println("🧪 === TEST BÁSICO DE MENSAJERÍA GRUPAL ===");

        try {
            // PASO 1: Crear categoría de prueba
            Category grupoTest = categoryService.createCategory(
                    "Grupo Test",
                    "Grupo para pruebas de mensajería",
                    null
            );
            System.out.println("✅ Categoría creada: " + grupoTest.getName());

            // PASO 2: Crear contactos de prueba
            Contact contacto1 = contactService.createContact(
                    "Usuario Test 1",
                    "bax6351@gmail.com",  // Tu email real
                    "644023859",          // Tu teléfono real
                    null,
                    "Contacto de prueba principal"
            );

            Contact contacto2 = contactService.createContact(
                    "Usuario Test 2",
                    "rybantfg@gmail.com", // Tu otro email
                    "600000000",          // Número ficticio
                    null,
                    "Contacto de prueba secundario"
            );

            System.out.println("✅ Contactos creados:");
            System.out.println("  👤 " + contacto1.getName() + " - " + contacto1.getEmail());
            System.out.println("  👤 " + contacto2.getName() + " - " + contacto2.getEmail());

            // PASO 3: Agregar contactos a la categoría
            contactService.addContactToCategory(contacto1.getId(), grupoTest.getId());
            contactService.addContactToCategory(contacto2.getId(), grupoTest.getId());

            System.out.println("✅ Contactos asignados a la categoría");

            // PASO 4: Obtener emails de la categoría
            Set<String> emailsGrupo = categoryService.getAllEmailsFromCategory(grupoTest.getId());
            System.out.println("📧 Emails encontrados: " + emailsGrupo);

            // PASO 5: Enviar mensaje al grupo (CASO DE USO 02: Difusión)
            if (!emailsGrupo.isEmpty()) {
                String messageId = messagePublisher.broadcastMessage(
                        List.copyOf(emailsGrupo),
                        "🎯 ¡Mensaje de prueba de la aplicación TFG!\n\n" +
                                "Este es un test del sistema de mensajería grupal.\n" +
                                "Si recibes este email, el sistema funciona correctamente.\n\n" +
                                "Características probadas:\n" +
                                "✅ Gestión de contactos\n" +
                                "✅ Gestión de categorías\n" +
                                "✅ Envío por grupos\n" +
                                "✅ RabbitMQ asíncrono\n" +
                                "✅ Arquitectura hexagonal\n\n" +
                                "Enviado desde: SimpleGroupMessagingTest\n" +
                                "Hora: " + java.time.LocalDateTime.now(),
                        "🎯 Test TFG - Mensaje Grupal Exitoso"
                );

                System.out.println("📢 ¡DIFUSIÓN ENVIADA!");
                System.out.println("  🆔 Message ID: " + messageId);
                System.out.println("  👥 Destinatarios: " + emailsGrupo.size());
                System.out.println("  📧 Revisa tus emails: " + emailsGrupo);

                // Verificaciones
                assert messageId != null : "El ID del mensaje no puede ser null";
                assert emailsGrupo.size() >= 2 : "Debería haber al menos 2 emails en el grupo";

            } else {
                System.err.println("❌ No se encontraron emails en la categoría");
            }

            // PASO 6: Mostrar estadísticas
            var stats = categoryService.getCategoryStats(grupoTest.getId());
            System.out.println("\n📊 ESTADÍSTICAS DEL GRUPO:");
            System.out.println("  👥 Total contactos: " + stats.get("totalContacts"));
            System.out.println("  📧 Con email: " + stats.get("contactsWithEmail"));
            System.out.println("  📱 Con teléfono: " + stats.get("contactsWithPhone"));

            System.out.println("\n🎉 ¡TEST COMPLETADO EXITOSAMENTE!");
            System.out.println("📧 Revisa tus emails para confirmar la recepción");

        } catch (Exception e) {
            System.err.println("❌ Error en el test: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testContactManagement() {
        System.out.println("👥 === TEST GESTIÓN DE CONTACTOS ===");

        // Test de creación básica
        Contact nuevoContacto = contactService.createContact(
                "Test Manager",
                "test.manager@tfg.com",
                "666777888",
                null,
                "Contacto para testing"
        );

        System.out.println("✅ Contacto creado: " + nuevoContacto.getName());

        // Test de búsqueda
        List<Contact> encontrados = contactService.searchContacts("Manager");
        System.out.println("🔍 Búsqueda 'Manager': " + encontrados.size() + " resultados");

        // Test de contactos con email
        List<Contact> conEmail = contactService.getContactsWithEmail();
        System.out.println("📧 Contactos con email: " + conEmail.size());

        // Verificaciones
        assert nuevoContacto.getId() != null : "El contacto debe tener ID";
        assert encontrados.size() >= 1 : "Debería encontrar el contacto por búsqueda";
        assert conEmail.size() >= 1 : "Debería haber contactos con email";

        System.out.println("✅ Test de gestión de contactos completado");
    }

    @Test
    public void testCategoryHierarchy() {
        System.out.println("📁 === TEST JERARQUÍA DE CATEGORÍAS ===");

        // Crear categoría padre
        Category empresa = categoryService.createCategory(
                "Empresa",
                "Categoría raíz para la empresa",
                null
        );

        // Crear subcategorías
        Category desarrollo = categoryService.createCategory(
                "Desarrollo",
                "Equipo de desarrollo",
                empresa.getId()
        );

        Category marketing = categoryService.createCategory(
                "Marketing",
                "Equipo de marketing",
                empresa.getId()
        );

        System.out.println("✅ Jerarquía creada:");
        System.out.println("  📁 " + empresa.getName() + " (Padre)");
        System.out.println("    📁 " + desarrollo.getName() + " (Hijo)");
        System.out.println("    📁 " + marketing.getName() + " (Hijo)");

        // Verificar subcategorías
        List<Category> subcategorias = categoryService.getSubcategories(empresa.getId());
        System.out.println("📊 Subcategorías encontradas: " + subcategorias.size());

        // Verificaciones
        assert desarrollo.getParent().getId().equals(empresa.getId()) : "Desarrollo debe ser hijo de Empresa";
        assert marketing.getParent().getId().equals(empresa.getId()) : "Marketing debe ser hijo de Empresa";
        assert subcategorias.size() == 2 : "Empresa debe tener 2 subcategorías";

        System.out.println("✅ Test de jerarquía completado");
    }
}