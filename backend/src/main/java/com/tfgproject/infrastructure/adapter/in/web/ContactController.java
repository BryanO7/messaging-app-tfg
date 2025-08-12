// src/main/java/com/tfgproject/infrastructure/adapter/in/web/ContactController.java
package com.tfgproject.infrastructure.adapter.in.web;

import com.tfgproject.application.dto.request.ContactRequest;
import com.tfgproject.domain.model.Contact;
import com.tfgproject.domain.service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
@Validated // ✅ Agregado para validaciones
public class ContactController {
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactService contactService;

    // === CREAR CONTACTO CON VALIDACIONES ===
    @PostMapping
    public ResponseEntity<?> createContact(@Valid @RequestBody ContactRequest request) {
        logger.info("📝 Creando contacto: {}", request.getName());

        try {
            // Validación personalizada
            request.validateContactChannels();

            Contact contact = contactService.createContact(
                    request.getName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getWhatsappId(),
                    request.getNotes()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contacto creado exitosamente",
                    "contact", contact
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("❌ Error creando contacto: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    // === OBTENER TODOS LOS CONTACTOS ===
    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        logger.info("📋 Obteniendo todos los contactos");
        List<Contact> contacts = contactService.getAllContacts();
        return ResponseEntity.ok(contacts);
    }

    // === BUSCAR CONTACTOS ===
    @GetMapping("/search")
    public ResponseEntity<List<Contact>> searchContacts(@RequestParam String query) {
        logger.info("🔍 Buscando contactos: {}", query);
        List<Contact> contacts = contactService.searchContacts(query);
        return ResponseEntity.ok(contacts);
    }

    // === OBTENER CONTACTO POR ID ===
    @GetMapping("/{id}")
    public ResponseEntity<?> getContactById(@PathVariable Long id) {
        logger.info("👤 Obteniendo contacto ID: {}", id);

        Optional<Contact> contact = contactService.getContactById(id);
        if (contact.isPresent()) {
            return ResponseEntity.ok(contact.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // === ACTUALIZAR CONTACTO CON VALIDACIONES ===
    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(@PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        logger.info("✏️ Actualizando contacto ID: {}", id);

        try {
            // Validación personalizada
            request.validateContactChannels();

            Contact contact = contactService.updateContact(
                    id,
                    request.getName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getWhatsappId(),
                    request.getNotes()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contacto actualizado exitosamente",
                    "contact", contact
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("❌ Error actualizando contacto: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    // === ELIMINAR CONTACTO ===
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {
        logger.info("🗑️ Eliminando contacto ID: {}", id);

        try {
            contactService.deleteContact(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contacto eliminado exitosamente"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // === AGREGAR CONTACTO A CATEGORÍA ===
    @PostMapping("/{contactId}/categories/{categoryId}")
    public ResponseEntity<?> addContactToCategory(@PathVariable Long contactId, @PathVariable Long categoryId) {
        logger.info("🏷️ Agregando contacto {} a categoría {}", contactId, categoryId);

        try {
            Contact contact = contactService.addContactToCategory(contactId, categoryId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contacto agregado a la categoría exitosamente",
                    "contact", contact
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // === REMOVER CONTACTO DE CATEGORÍA ===
    @DeleteMapping("/{contactId}/categories/{categoryId}")
    public ResponseEntity<?> removeContactFromCategory(@PathVariable Long contactId, @PathVariable Long categoryId) {
        logger.info("🏷️ Removiendo contacto {} de categoría {}", contactId, categoryId);

        try {
            Contact contact = contactService.removeContactFromCategory(contactId, categoryId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contacto removido de la categoría exitosamente",
                    "contact", contact
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // === OBTENER CONTACTOS POR CATEGORÍA ===
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Contact>> getContactsByCategory(@PathVariable Long categoryId) {
        logger.info("📂 Obteniendo contactos de categoría: {}", categoryId);
        List<Contact> contacts = contactService.getContactsByCategory(categoryId);
        return ResponseEntity.ok(contacts);
    }
}

// ✅ CLASES DTO ELIMINADAS - AHORA ESTÁN EN ARCHIVOS SEPARADOS