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

    // ✅ NUEVO: Campos separados para email y phone (para canal "both")
    private String email;
    private String phone;

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

    // ✅ NUEVO: Campo para el canal (email, sms, both)
    private String channel = "email";

    // Tipo de mensaje: "EMAIL", "SMS", "BROADCAST", "SCHEDULED"
    private String type = "EMAIL";

    // ✅ NUEVOS: Métodos de conveniencia para el canal
    public boolean isEmailChannel() {
        return "email".equalsIgnoreCase(channel) || "both".equalsIgnoreCase(channel);
    }

    public boolean isSmsChannel() {
        return "sms".equalsIgnoreCase(channel) || "both".equalsIgnoreCase(channel);
    }

    public boolean isBothChannels() {
        return "both".equalsIgnoreCase(channel);
    }

    // Métodos existentes
    public boolean isBroadcast() {
        return recipients != null && !recipients.isEmpty();
    }

    public boolean isScheduled() {
        return scheduledTime != null && !scheduledTime.trim().isEmpty();
    }

    // ✅ NUEVO: Validación mejorada
    public boolean isValid() {
        // Verificar datos básicos
        if (to == null || to.trim().isEmpty()) return false;
        if (content == null || content.trim().isEmpty()) return false;

        // Si es email o both, debe tener subject
        if (isEmailChannel() && (subject == null || subject.trim().isEmpty())) {
            return false;
        }

        // Verificar canal válido
        if (channel != null) {
            String channelLower = channel.toLowerCase();
            return channelLower.equals("email") || channelLower.equals("sms") || channelLower.equals("both");
        }

        return true;
    }

    // ✅ NUEVO: Método para obtener el tipo basado en el canal (para compatibilidad)
    public String getTypeFromChannel() {
        if (channel == null) return "EMAIL";

        switch (channel.toLowerCase()) {
            case "sms":
                return "SMS";
            case "email":
                return "EMAIL";
            case "both":
                return "BOTH";
            default:
                return "EMAIL";
        }
    }

    // ✅ NUEVO: ToString mejorado para debug
    @Override
    public String toString() {
        return "MessageRequest{" +
                "to='" + to + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(50, content.length())) + "..." : "null") + '\'' +
                ", channel='" + channel + '\'' +
                ", type='" + type + '\'' +
                ", sender='" + sender + '\'' +
                ", isHtml=" + isHtml +
                ", scheduledTime='" + scheduledTime + '\'' +
                ", recipientsCount=" + (recipients != null ? recipients.size() : 0) +
                '}';
    }
}