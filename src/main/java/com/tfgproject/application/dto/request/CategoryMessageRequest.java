// src/main/java/com/tfgproject/application/dto/request/CategoryMessageRequest.java
package com.tfgproject.application.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMessageRequest {

    @Size(max = 200, message = "El asunto no puede exceder 200 caracteres")
    private String subject;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(min = 1, max = 2000, message = "El contenido debe tener entre 1 y 2000 caracteres")
    private String content;

    private boolean sendEmail = true;
    private boolean sendSms = false;

    // Validación: al menos un canal debe estar seleccionado
    @AssertTrue(message = "Debe seleccionar al menos un canal de comunicación (email o SMS)")
    public boolean isAtLeastOneChannelSelected() {
        return sendEmail || sendSms;
    }

    // Método de conveniencia
    public boolean shouldSendToAllChannels() {
        return sendEmail && sendSms;
    }
}