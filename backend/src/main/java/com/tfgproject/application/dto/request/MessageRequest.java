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
public class MessageRequest {

    @NotEmpty(message = "El destinatario no puede estar vacío")
    private String to;

    @Size(max = 200, message = "El asunto no puede exceder 200 caracteres")
    private String subject;

    @NotNull(message = "El contenido no puede ser nulo")
    @Size(min = 1, max = 2000, message = "Contenido debe tener entre 1 y 2000 caracteres")
    private String content;

    private String sender;
    private boolean isHtml = false;
    private String attachmentPath;

    // Para envío a múltiples destinatarios
    private List<String> recipients;

    // Para programación
    private String scheduledTime; // ISO String para JSON

    // Tipo de mensaje: "EMAIL", "SMS", "BROADCAST", "SCHEDULED"
    private String type = "EMAIL";

    // Método de conveniencia
    public boolean isBroadcast() {
        return recipients != null && !recipients.isEmpty();
    }

    public boolean isScheduled() {
        return scheduledTime != null && !scheduledTime.trim().isEmpty();
    }
}