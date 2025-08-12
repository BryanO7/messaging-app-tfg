// src/main/java/com/tfgproject/application/dto/request/ContactRequest.java
package com.tfgproject.application.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{9,15}$", message = "Formato de teléfono inválido")
    private String phone;

    @Size(max = 50, message = "WhatsApp ID no puede exceder 50 caracteres")
    private String whatsappId;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    // Método de validación personalizada
    public boolean hasValidContactInfo() {
        return (email != null && !email.trim().isEmpty()) ||
                (phone != null && !phone.trim().isEmpty());
    }

    // Método para validar que al menos tenga un canal de comunicación
    public void validateContactChannels() {
        if (!hasValidContactInfo()) {
            throw new IllegalArgumentException("El contacto debe tener al menos email o teléfono");
        }
    }
}