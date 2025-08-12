// src/main/java/com/tfgproject/domain/model/Contact.java
package com.tfgproject.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // ✅ Solo incluir ID para equals/hashCode
@ToString(exclude = "categories") // ✅ Excluir categories de toString
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // ✅ Solo usar ID para equals/hashCode
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    private String phone;

    private String whatsappId;

    private String notes; // Para notas adicionales

    @ManyToMany(mappedBy = "contacts", fetch = FetchType.LAZY)
    @Builder.Default // ✅ Inicializar por defecto
    private Set<Category> categories = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de utilidad
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    public boolean hasPhone() {
        return phone != null && !phone.trim().isEmpty();
    }

    public boolean hasWhatsApp() {
        return whatsappId != null && !whatsappId.trim().isEmpty();
    }

    // Obtener canales disponibles
    public Set<String> getAvailableChannels() {
        Set<String> channels = new HashSet<>();
        if (hasEmail()) channels.add("EMAIL");
        if (hasPhone()) channels.add("SMS");
        if (hasWhatsApp()) channels.add("WHATSAPP");
        return channels;
    }
}