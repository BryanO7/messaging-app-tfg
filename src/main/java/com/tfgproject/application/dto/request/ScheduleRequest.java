package com.tfgproject.application.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    @NotEmpty(message = "El destinatario no puede estar vacío")
    @Email(message = "El destinatario debe ser un email válido")
    private String to;

    @Size(max = 200, message = "El asunto no puede exceder 200 caracteres")
    private String subject;

    @NotNull(message = "El contenido no puede ser nulo")
    @Size(min = 1, max = 2000, message = "Contenido debe tener entre 1 y 2000 caracteres")
    private String content;

    @NotNull(message = "La fecha programada no puede ser nula")
    @Future(message = "La fecha programada debe ser en el futuro")
    private LocalDateTime scheduledTime;

    private String sender;
    private boolean isHtml = false;
    private String attachmentPath;

    // Método de conveniencia para validar
    public boolean isValid() {
        return to != null && !to.trim().isEmpty() &&
                content != null && !content.trim().isEmpty() &&
                scheduledTime != null && scheduledTime.isAfter(LocalDateTime.now());
    }
}
