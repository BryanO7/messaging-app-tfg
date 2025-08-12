// src/main/java/com/tfgproject/infrastructure/config/DataLoader.java
package com.tfgproject.infrastructure.config;

import com.tfgproject.domain.model.Category;
import com.tfgproject.domain.model.Contact;
import com.tfgproject.domain.service.CategoryService;
import com.tfgproject.domain.service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "!test"}) // ✅ Solo ejecutar en perfil 'dev', NO en tests
public class DataLoader implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ContactService contactService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("🚀 Cargando datos de desarrollo (perfil: dev)...");

        try {
            // Verificar si ya existen datos
            if (!contactService.getAllContacts().isEmpty()) {
                logger.info("ℹ️ Ya existen datos en la base de datos, saltando carga inicial");
                return;
            }

            createSampleData();

        } catch (Exception e) {
            logger.error("❌ Error cargando datos de desarrollo: {}", e.getMessage(), e);
        }
    }

    private void createSampleData() {
        // === CREAR CATEGORÍAS ===
        logger.info("📁 Creando categorías...");

        // Categorías principales
        Category grupoPesca = createCategoryIfNotExists("Grupo de Pesca", "Aficionados a la pesca deportiva", null);
        Category trabajo = createCategoryIfNotExists("Trabajo", "Contactos laborales", null);
        Category familia = createCategoryIfNotExists("Familia", "Familiares cercanos", null);
        Category amigos = createCategoryIfNotExists("Amigos", "Amigos personales", null);

        // Subcategorías
        Category pescaRio = createCategoryIfNotExists("Pesca de Río", "Especialistas en pesca fluvial", grupoPesca.getId());
        Category pescaMar = createCategoryIfNotExists("Pesca de Mar", "Especialistas en pesca marina", grupoPesca.getId());

        Category equipoDesarrollo = createCategoryIfNotExists("Equipo Desarrollo", "Desarrolladores del equipo", trabajo.getId());
        Category gerencia = createCategoryIfNotExists("Gerencia", "Personal gerencial", trabajo.getId());

        logger.info("✅ Categorías creadas: {}", categoryService.getAllCategories().size());

        // === CREAR CONTACTOS ===
        logger.info("👥 Creando contactos...");

        // Contactos para Grupo de Pesca (EMAILS REALES)
        Contact juan = createContactIfNotExists("Juan Pérez", "rybantfg@gmail.com", "644023859", null, "Experto en pesca de río");
        Contact maria = createContactIfNotExists("Maria González", "bax6351@gmail.com", null, null, "Le gusta la pesca marina");
        Contact carlos = createContactIfNotExists("Carlos Ruiz", "bryanoyonate07@gmail.com", null, null, "Organizador de expediciones");
        Contact ana = createContactIfNotExists("Ana López", "rybantfg+ana@gmail.com", null, null, "Fotógrafa de pesca");

        // Contactos para Trabajo (EMAILS REALES)
        Contact pedro = createContactIfNotExists("Pedro Martín", "bax6351+pedro@gmail.com", null, null, "Desarrollador Senior");
        Contact lucia = createContactIfNotExists("Lucía Fernández", "bryanoyonate07+lucia@gmail.com", null, null, "Project Manager");
        Contact diego = createContactIfNotExists("Diego Torres", "rybantfg+diego@gmail.com", null, null, "Gerente Técnico");

        // Contactos para Familia (EMAILS REALES)
        Contact madre = createContactIfNotExists("Carmen Sánchez", "bax6351+madre@gmail.com", null, null, "Mi madre");
        Contact hermano = createContactIfNotExists("Miguel Sánchez", "bryanoyonate07+hermano@gmail.com", null, null, "Mi hermano");

        // Contactos para Amigos (EMAILS REALES)
        Contact alberto = createContactIfNotExists("Alberto Vega", "rybantfg+alberto@gmail.com", null, null, "Amigo de la universidad");
        Contact sofia = createContactIfNotExists("Sofía Morales", "bax6351+sofia@gmail.com", null, null, "Amiga de la infancia");

        logger.info("✅ Contactos creados: {}", contactService.getAllContacts().size());

        // === ASIGNAR CONTACTOS A CATEGORÍAS ===
        logger.info("🏷️ Asignando contactos a categorías...");

        // Grupo de Pesca
        addContactToCategory(juan, grupoPesca);
        addContactToCategory(maria, grupoPesca);
        addContactToCategory(carlos, grupoPesca);
        addContactToCategory(ana, grupoPesca);

        // Especialización en subcategorías
        addContactToCategory(juan, pescaRio);
        addContactToCategory(maria, pescaMar);
        addContactToCategory(carlos, pescaRio);
        addContactToCategory(carlos, pescaMar);

        // Trabajo
        addContactToCategory(pedro, trabajo);
        addContactToCategory(lucia, trabajo);
        addContactToCategory(diego, trabajo);

        // Subcategorías de trabajo
        addContactToCategory(pedro, equipoDesarrollo);
        addContactToCategory(diego, gerencia);
        addContactToCategory(lucia, gerencia);

        // Familia
        addContactToCategory(madre, familia);
        addContactToCategory(hermano, familia);

        // Amigos
        addContactToCategory(alberto, amigos);
        addContactToCategory(sofia, amigos);

        logger.info("✅ Contactos asignados a categorías");

        // === MOSTRAR RESUMEN ===
        logger.info("📊 === RESUMEN DE DATOS CARGADOS (DEV) ===");
        logger.info("📁 Categorías totales: {}", categoryService.getAllCategories().size());
        logger.info("👥 Contactos totales: {}", contactService.getAllContacts().size());

        // Mostrar estadísticas de cada categoría principal
        logger.info("📈 Estadísticas por categoría:");
        logger.info("  🎣 Grupo de Pesca: {} contactos", contactService.getContactsByCategory(grupoPesca.getId()).size());
        logger.info("  💼 Trabajo: {} contactos", contactService.getContactsByCategory(trabajo.getId()).size());
        logger.info("  👨‍👩‍👧‍👦 Familia: {} contactos", contactService.getContactsByCategory(familia.getId()).size());
        logger.info("  👫 Amigos: {} contactos", contactService.getContactsByCategory(amigos.getId()).size());

        logger.info("🎉 ¡Datos de desarrollo cargados exitosamente!");
        logger.info("🌐 Puedes acceder a H2 Console en: http://localhost:8080/h2-console");
        logger.info("📡 API Endpoints disponibles:");
        logger.info("  GET  /api/categories - Ver todas las categorías");
        logger.info("  GET  /api/contacts - Ver todos los contactos");
        logger.info("  POST /api/categories/{id}/send-message - Enviar mensaje a categoría");
    }

    // === MÉTODOS DE UTILIDAD ===
    private Category createCategoryIfNotExists(String name, String description, Long parentId) {
        try {
            return categoryService.createCategory(name, description, parentId);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Categoría ya existe: {}", name);
            return categoryService.searchCategories(name).stream()
                    .filter(cat -> (parentId == null && cat.getParent() == null) ||
                            (parentId != null && cat.getParent() != null && cat.getParent().getId().equals(parentId)))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se pudo crear ni encontrar categoría: " + name));
        }
    }

    private Contact createContactIfNotExists(String name, String email, String phone, String whatsapp, String notes) {
        try {
            return contactService.createContact(name, email, phone, whatsapp, notes);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Contacto ya existe: {}", email != null ? email : phone);
            // Buscar contacto existente por email o teléfono
            return contactService.searchContacts(email != null ? email : phone).stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se pudo crear ni encontrar contacto: " + name));
        }
    }

    private void addContactToCategory(Contact contact, Category category) {
        try {
            contactService.addContactToCategory(contact.getId(), category.getId());
        } catch (Exception e) {
            logger.debug("ℹ️ Contacto {} ya está en categoría {}", contact.getName(), category.getName());
        }
    }
}