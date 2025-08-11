// src/main/java/com/tfgproject/infrastructure/adapter/in/web/CategoryController.java
package com.tfgproject.infrastructure.adapter.in.web;

import com.tfgproject.application.dto.request.CategoryRequest;
import com.tfgproject.application.dto.request.CategoryMessageRequest;
import com.tfgproject.domain.model.Category;
import com.tfgproject.domain.service.CategoryService;
import com.tfgproject.infrastructure.service.AsyncMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@Validated // ✅ Agregado para validaciones
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AsyncMessagePublisher messagePublisher;

    // === CREAR CATEGORÍA CON VALIDACIONES ===
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest request) {
        logger.info("📁 Creando categoría: {}", request.getName());

        try {
            Category category = categoryService.createCategory(
                    request.getName(),
                    request.getDescription(),
                    request.getParentId()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Categoría creada exitosamente",
                    "category", category
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("❌ Error creando categoría: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    // === OBTENER TODAS LAS CATEGORÍAS ===
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        logger.info("📋 Obteniendo todas las categorías");
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // === OBTENER CATEGORÍAS RAÍZ ===
    @GetMapping("/root")
    public ResponseEntity<List<Category>> getRootCategories() {
        logger.info("🌳 Obteniendo categorías raíz");
        List<Category> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    // === OBTENER CATEGORÍA POR ID ===
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        logger.info("📂 Obteniendo categoría ID: {}", id);

        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // === OBTENER CATEGORÍA CON CONTACTOS ===
    @GetMapping("/{id}/with-contacts")
    public ResponseEntity<?> getCategoryWithContacts(@PathVariable Long id) {
        logger.info("👥 Obteniendo categoría con contactos ID: {}", id);

        Optional<Category> category = categoryService.getCategoryWithContacts(id);
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // === ACTUALIZAR CATEGORÍA CON VALIDACIONES ===
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        logger.info("✏️ Actualizando categoría ID: {}", id);

        try {
            Category category = categoryService.updateCategory(
                    id,
                    request.getName(),
                    request.getDescription()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Categoría actualizada exitosamente",
                    "category", category
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("❌ Error actualizando categoría: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    // === ELIMINAR CATEGORÍA ===
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        logger.info("🗑️ Eliminando categoría ID: {}", id);

        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Categoría eliminada exitosamente"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // === OBTENER SUBCATEGORÍAS ===
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubcategories(@PathVariable Long id) {
        logger.info("📁 Obteniendo subcategorías de: {}", id);
        List<Category> subcategories = categoryService.getSubcategories(id);
        return ResponseEntity.ok(subcategories);
    }

    // === BUSCAR CATEGORÍAS ===
    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String query) {
        logger.info("🔍 Buscando categorías: {}", query);
        List<Category> categories = categoryService.searchCategories(query);
        return ResponseEntity.ok(categories);
    }

    // === OBTENER ESTADÍSTICAS DE CATEGORÍA ===
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getCategoryStats(@PathVariable Long id) {
        logger.info("📊 Obteniendo estadísticas de categoría: {}", id);

        try {
            Map<String, Object> stats = categoryService.getCategoryStats(id);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // === 🚀 ENVIAR MENSAJE A CATEGORÍA CON VALIDACIONES ===
    @PostMapping("/{id}/send-message")
    public ResponseEntity<?> sendMessageToCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryMessageRequest request) {

        logger.info("📢 Enviando mensaje a categoría: {}", id);

        try {
            // Obtener emails de la categoría
            Set<String> emails = categoryService.getAllEmailsFromCategory(id);
            Set<String> phones = categoryService.getAllPhonesFromCategory(id);

            if (emails.isEmpty() && phones.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "La categoría no tiene contactos con información de contacto válida"
                ));
            }

            String messageId = null;
            int totalSent = 0;

            // Enviar por EMAIL si se solicita
            if (request.isSendEmail() && !emails.isEmpty()) {
                messageId = messagePublisher.broadcastMessage(
                        emails.stream().toList(),
                        request.getContent(),
                        request.getSubject()
                );
                totalSent += emails.size();
                logger.info("📧 Emails enviados: {}", emails.size());
            }

            // Enviar por SMS si se solicita
            if (request.isSendSms() && !phones.isEmpty()) {
                for (String phone : phones) {
                    messagePublisher.sendSmsToQueue(phone, request.getContent(), "TFG-App");
                    totalSent++;
                }
                logger.info("📱 SMS enviados: {}", phones.size());
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mensaje enviado a la categoría exitosamente",
                    "messageId", messageId,
                    "totalRecipients", totalSent,
                    "emailRecipients", emails.size(),
                    "smsRecipients", phones.size()
            ));

        } catch (Exception e) {
            logger.error("❌ Error enviando mensaje a categoría: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error enviando mensaje: " + e.getMessage()
            ));
        }
    }

    // === OBTENER CANALES DISPONIBLES EN CATEGORÍA ===
    @GetMapping("/{id}/channels")
    public ResponseEntity<Map<String, Object>> getCategoryChannels(@PathVariable Long id) {
        logger.info("📡 Obteniendo canales de categoría: {}", id);

        try {
            // Usar los métodos existentes del CategoryService
            Set<String> emails = categoryService.getAllEmailsFromCategory(id);
            Set<String> phones = categoryService.getAllPhonesFromCategory(id);

            Map<String, Object> channels = new HashMap<>();
            channels.put("emails", emails.stream().toList());
            channels.put("phones", phones.stream().toList());
            channels.put("totalEmails", emails.size());
            channels.put("totalPhones", phones.size());
            channels.put("hasEmailChannel", !emails.isEmpty());
            channels.put("hasSmsChannel", !phones.isEmpty());

            return ResponseEntity.ok(channels);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("❌ Error obteniendo canales de categoría: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }
}

// ✅ CLASES DTO ELIMINADAS - AHORA ESTÁN EN ARCHIVOS SEPARADOS