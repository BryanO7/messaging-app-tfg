// src/main/java/com/tfgproject/application/dto/request/CategoryRequest.java
package com.tfgproject.application.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @Size(max = 300, message = "La descripción no puede exceder 300 caracteres")
    private String description;

    private Long parentId;

    // Método de validación
    public boolean isSubcategory() {
        return parentId != null;
    }
}