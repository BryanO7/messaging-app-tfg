package com.tfgproject.domain.service;

import com.tfgproject.domain.model.Contact;
import com.tfgproject.domain.model.Category;
import com.tfgproject.infrastructure.adapter.out.persistence.ContactRepository;
import com.tfgproject.infrastructure.adapter.out.persistence.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // === CRUD BÁSICO ===

    public Contact createContact(String name, String email, String phone, String whatsappId, String notes) {
        // Validar duplicados
        if (email != null && contactRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un contacto con este email: " + email);
        }

        if (phone != null && contactRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Ya existe un contacto con este teléfono: " + phone);
        }

        Contact contact = Contact.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .whatsappId(whatsappId)
                .notes(notes)
                .build();

        return contactRepository.save(contact);
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public Optional<Contact> getContactById(Long id) {
        return contactRepository.findById(id);
    }

    public Contact updateContact(Long id, String name, String email, String phone, String whatsappId, String notes) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado: " + id));

        // Validar email duplicado (si cambió)
        if (email != null && !email.equals(contact.getEmail()) && contactRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un contacto with este email: " + email);
        }

        contact.setName(name);
        contact.setEmail(email);
        contact.setPhone(phone);
        contact.setWhatsappId(whatsappId);
        contact.setNotes(notes);

        return contactRepository.save(contact);
    }

    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado: " + id));

        // Remover de todas las categorías
        for (Category category : contact.getCategories()) {
            category.getContacts().remove(contact);
        }

        contactRepository.delete(contact);
    }

    // === BÚSQUEDA Y FILTRADO ===

    public List<Contact> searchContacts(String search) {
        if (search == null || search.trim().isEmpty()) {
            return getAllContacts();
        }
        return contactRepository.searchContacts(search.trim());
    }

    public List<Contact> getContactsByCategory(Long categoryId) {
        return contactRepository.findByCategoryId(categoryId);
    }

    public List<Contact> getContactsWithEmail() {
        return contactRepository.findContactsWithEmail();
    }

    public List<Contact> getContactsWithPhone() {
        return contactRepository.findContactsWithPhone();
    }

    // === GESTIÓN DE CATEGORÍAS ===

    public Contact addContactToCategory(Long contactId, Long categoryId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado: " + contactId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + categoryId));

        contact.getCategories().add(category);
        category.getContacts().add(contact);

        return contactRepository.save(contact);
    }

    public Contact removeContactFromCategory(Long contactId, Long categoryId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado: " + contactId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + categoryId));

        contact.getCategories().remove(category);
        category.getContacts().remove(contact);

        return contactRepository.save(contact);
    }

    // === UTILIDADES PARA MENSAJERÍA ===

    public List<String> getEmailsByCategory(Long categoryId) {
        return getContactsByCategory(categoryId).stream()
                .filter(Contact::hasEmail)
                .map(Contact::getEmail)
                .toList();
    }

    public List<String> getPhonesByCategory(Long categoryId) {
        return getContactsByCategory(categoryId).stream()
                .filter(Contact::hasPhone)
                .map(Contact::getPhone)
                .toList();
    }

    public Map<String, List<String>> getContactChannelsByCategory(Long categoryId) {
        List<Contact> contacts = getContactsByCategory(categoryId);

        Map<String, List<String>> channels = new HashMap<>();
        channels.put("emails", contacts.stream().filter(Contact::hasEmail).map(Contact::getEmail).toList());
        channels.put("phones", contacts.stream().filter(Contact::hasPhone).map(Contact::getPhone).toList());

        return channels;
    }

}
