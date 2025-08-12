package com.tfgproject.application.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastRequest {

    @NotEmpty(message = "La lista de destinatarios no puede estar vacía")
    @Size(min = 1, max = 100, message = "Entre 1 y 100 destinatarios permitidos")
    private List<String> recipients;

    @NotNull(message = "El contenido no puede ser nulo")
    @Size(min = 1, max = 2000, message = "Contenido debe tener entre 1 y 2000 caracteres")
    private String content;

    @Size(max = 200, message = "El asunto no puede exceder 200 caracteres")
    private String subject;

    private String sender;
    private boolean isHtml = false;
    private String attachmentPath;

    // Método de conveniencia para validar
    public boolean isValid() {
        return recipients != null && !recipients.isEmpty() &&
                content != null && !content.trim().isEmpty();
    }
}
