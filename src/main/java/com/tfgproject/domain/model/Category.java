package com.tfgproject.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> subcategories = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "category_contact",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private Set<Contact> contacts = new HashSet<>();

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Método para obtener todos los contactos incluyendo subcategorías
    public Set<Contact> getAllContacts() {
        Set<Contact> allContacts = new HashSet<>(contacts);
        for (Category subcategory : subcategories) {
            allContacts.addAll(subcategory.getAllContacts());
        }
        return allContacts;
    }

    // Verificar si es categoría raíz
    public boolean isRoot() {
        return parent == null;
    }

    // Obtener nivel jerárquico
    public int getLevel() {
        return parent == null ? 0 : parent.getLevel() + 1;
    }

    // Contar contactos totales (incluyendo subcategorías)
    public int getTotalContactCount() {
        return getAllContacts().size();
    }

    // Obtener emails de todos los contactos
    public Set<String> getAllEmails() {
        return getAllContacts().stream()
                .filter(Contact::hasEmail)
                .map(Contact::getEmail)
                .collect(java.util.stream.Collectors.toSet());
    }

    // Obtener teléfonos de todos los contactos
    public Set<String> getAllPhones() {
        return getAllContacts().stream()
                .filter(Contact::hasPhone)
                .map(Contact::getPhone)
                .collect(java.util.stream.Collectors.toSet());
    }
}