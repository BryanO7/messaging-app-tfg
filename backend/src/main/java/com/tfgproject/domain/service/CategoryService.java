package com.tfgproject.domain.service;

import com.tfgproject.domain.model.Category;
import com.tfgproject.domain.model.Contact;
import com.tfgproject.infrastructure.adapter.out.persistence.CategoryRepository;
import com.tfgproject.infrastructure.adapter.out.persistence.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ContactRepository contactRepository;

    // === CRUD BÁSICO ===

    public Category createCategory(String name, String description, Long parentId) {
        // Validar nombre duplicado en el mismo nivel
        if (parentId != null) {
            if (categoryRepository.existsByNameAndParentId(name, parentId)) {
                throw new IllegalArgumentException("Ya existe una subcategoría con este nombre en la categoría padre");
            }
        } else {
            if (categoryRepository.existsByName(name)) {
                throw new IllegalArgumentException("Ya existe una categoría raíz con este nombre: " + name);
            }
        }

        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();

        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría padre no encontrada: " + parentId));
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategoriesOrderByName();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> getCategoryWithContacts(Long id) {
        return categoryRepository.findByIdWithContacts(id);
    }

    public Category updateCategory(Long id, String name, String description) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));

        // Validar nombre duplicado
        if (!name.equals(category.getName())) {
            if (category.getParent() != null) {
                if (categoryRepository.existsByNameAndParentId(name, category.getParent().getId())) {
                    throw new IllegalArgumentException("Ya existe una subcategoría con este nombre");
                }
            } else {
                if (categoryRepository.existsByName(name)) {
                    throw new IllegalArgumentException("Ya existe una categoría raíz con este nombre");
                }
            }
        }

        category.setName(name);
        category.setDescription(description);

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));

        // Remover contactos de la categoría
        for (Contact contact : category.getContacts()) {
            contact.getCategories().remove(category);
        }

        // Mover subcategorías al padre (si existe) o convertirlas en raíz
        for (Category subcategory : category.getSubcategories()) {
            subcategory.setParent(category.getParent());
        }

        categoryRepository.delete(category);
    }

    // === GESTIÓN JERÁRQUICA ===

    public List<Category> getSubcategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    public boolean hasSubcategories(Long categoryId) {
        return !categoryRepository.findByParentId(categoryId).isEmpty();
    }

    // === BÚSQUEDA ===

    public List<Category> searchCategories(String search) {
        if (search == null || search.trim().isEmpty()) {
            return getAllCategories();
        }
        return categoryRepository.findByNameContainingIgnoreCase(search.trim());
    }

    // === UTILIDADES PARA MENSAJERÍA ===

    public Set<String> getAllEmailsFromCategory(Long categoryId) {
        Category category = categoryRepository.findByIdWithContacts(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + categoryId));

        return category.getAllEmails();
    }

    public Set<String> getAllPhonesFromCategory(Long categoryId) {
        Category category = categoryRepository.findByIdWithContacts(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + categoryId));

        return category.getAllPhones();
    }

    public Map<String, Object> getCategoryStats(Long categoryId) {
        Category category = categoryRepository.findByIdWithContacts(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + categoryId));

        Set<Contact> allContacts = category.getAllContacts();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalContacts", allContacts.size());
        stats.put("contactsWithEmail", allContacts.stream().filter(Contact::hasEmail).count());
        stats.put("contactsWithPhone", allContacts.stream().filter(Contact::hasPhone).count());
        stats.put("subcategories", category.getSubcategories().size());

        return stats;
    }
}