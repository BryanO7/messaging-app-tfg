package com.tfgproject.infrastructure.adapter.in.web;

import com.tfgproject.domain.model.MessageStatus;
import com.tfgproject.domain.model.MessageStatusEnum;
import com.tfgproject.domain.model.SystemStatusReport; // ✅ IMPORT CORRECTO
import com.tfgproject.domain.service.MessageStatusService;
import com.tfgproject.infrastructure.service.AsyncScheduledMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageStatusController {
    private static final Logger logger = LoggerFactory.getLogger(MessageStatusController.class);

    @Autowired
    private MessageStatusService messageStatusService;

    @Autowired
    private AsyncScheduledMessageProcessor scheduledProcessor;

    /**
     * CASO DE USO 05: Obtener historial de mensajes del usuario
     * GET /api/messages/history?days=7&userId=currentUser
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getMessageHistory(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "currentUser") String userId) {

        logger.info("📋 Obteniendo historial de mensajes para usuario: {} (últimos {} días)", userId, days);

        List<MessageStatus> messages = messageStatusService.getUserMessageHistory(userId, days);

        // Agrupar por estado para estadísticas
        Map<String, Long> statusCounts = messages.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getStatus().name(),
                        Collectors.counting()
                ));

        // Agrupar por tipo (EMAIL, SMS, BROADCAST)
        Map<String, Long> typeCounts = messages.stream()
                .collect(Collectors.groupingBy(
                        MessageStatus::getType,
                        Collectors.counting()
                ));

        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);
        response.put("totalCount", messages.size());
        response.put("periodDays", days);
        response.put("userId", userId);
        response.put("statusBreakdown", statusCounts);
        response.put("typeBreakdown", typeCounts);
        response.put("timestamp", LocalDateTime.now());

        logger.info("📊 Historial obtenido: {} mensajes encontrados", messages.size());

        return ResponseEntity.ok(response);
    }

    /**
     * CASO DE USO 05: Obtener estado específico de un mensaje
     * GET /api/messages/{messageId}/status
     */
    @GetMapping("/{messageId}/status")
    public ResponseEntity<Map<String, Object>> getMessageStatus(@PathVariable String messageId) {

        logger.info("🔍 Consultando estado del mensaje: {}", messageId);

        Optional<MessageStatus> statusOpt = messageStatusService.getMessageStatus(messageId);

        Map<String, Object> response = new HashMap<>();

        if (statusOpt.isPresent()) {
            MessageStatus status = statusOpt.get();

            response.put("found", true);
            response.put("messageId", status.getMessageId());
            response.put("status", status.getStatus().name());
            response.put("statusDisplay", status.getStatus().getDisplayName());
            response.put("recipient", status.getRecipient());
            response.put("type", status.getType());
            response.put("subject", status.getSubject());
            response.put("timestamp", status.getTimestamp());
            response.put("errorMessage", status.getErrorMessage());

            // Estados booleanos para facilitar UI
            response.put("isSuccess", status.isSuccess());
            response.put("isFailed", status.isFailed());
            response.put("isPending", status.isPending());

            // Información adicional
            response.put("canRetry", status.isFailed());
            response.put("elapsedTime", calculateElapsedTime(status.getTimestamp()));

            logger.info("✅ Estado encontrado: {} - {}", messageId, status.getStatus());
            return ResponseEntity.ok(response);

        } else {
            response.put("found", false);
            response.put("messageId", messageId);
            response.put("error", "Mensaje no encontrado en el sistema");

            logger.warn("❌ Mensaje no encontrado: {}", messageId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * CASO DE USO 05: Estadísticas generales del sistema
     * GET /api/messages/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {

        logger.info("📊 Generando estadísticas del sistema");

        SystemStatusReport report = messageStatusService.getSystemStatusReport(); // ✅ CORREGIDO
        int scheduledCount = scheduledProcessor.getScheduledMessageCount();

        Map<String, Object> response = new HashMap<>();

        // Estadísticas básicas
        response.put("totalMessages", report.getTotalMessages()); // ✅ USAR GETTER
        response.put("successfulMessages", report.getSuccessfulMessages());
        response.put("failedMessages", report.getFailedMessages());
        response.put("pendingMessages", report.getPendingMessages());
        response.put("scheduledMessages", scheduledCount);

        // Métricas calculadas
        response.put("successRate", Math.round(report.getSuccessRate() * 100.0) / 100.0);
        response.put("failureRate", report.getTotalMessages() > 0 ?
                Math.round(report.getFailureRate() * 100.0) / 100.0 : 0.0); // ✅ USAR MÉTODO DE LA CLASE

        // Estado del sistema
        String systemStatus = determineSystemStatus(report.getPendingMessages(), report.getFailedMessages(), scheduledCount);
        response.put("systemStatus", systemStatus);
        response.put("systemHealth", calculateSystemHealth(report));

        // Actividad reciente
        response.put("recentActivity24h", report.getRecentMessages24h());
        response.put("timestamp", report.getTimestamp());

        // Información adicional para dashboard
        response.put("recommendations", generateRecommendations(report, scheduledCount));

        logger.info("📈 Estadísticas generadas - Total: {}, Éxito: {}%, Pendientes: {}",
                report.getTotalMessages(), Math.round(report.getSuccessRate()), report.getPendingMessages());

        return ResponseEntity.ok(response);
    }

    /**
     * CASO DE USO 05: Filtrar mensajes por estado
     * GET /api/messages/by-status/{status}
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<Map<String, Object>> getMessagesByStatus(@PathVariable String status) {

        logger.info("🔍 Filtrando mensajes por estado: {}", status);

        try {
            MessageStatusEnum statusEnum = MessageStatusEnum.valueOf(status.toUpperCase());
            List<MessageStatus> messages = messageStatusService.getMessagesByStatus(statusEnum);

            Map<String, Object> response = new HashMap<>();
            response.put("status", status.toUpperCase());
            response.put("statusDisplay", statusEnum.getDisplayName());
            response.put("messages", messages);
            response.put("count", messages.size());
            response.put("timestamp", LocalDateTime.now());

            // Información adicional
            if (statusEnum == MessageStatusEnum.FAILED) {
                response.put("canRetryAll", true);
                response.put("retryAllEndpoint", "/api/messages/retry-all-failed");
            }

            logger.info("📋 Encontrados {} mensajes con estado {}", messages.size(), status);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Estado inválido: " + status);
            response.put("validStates", List.of("QUEUED", "PROCESSING", "SENT", "DELIVERED", "FAILED", "SCHEDULED", "CANCELLED"));

            logger.warn("❌ Estado inválido solicitado: {}", status);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * CASO DE USO 05: Obtener mensajes por destinatario
     * GET /api/messages/by-recipient?recipient=example@email.com
     */
    @GetMapping("/by-recipient")
    public ResponseEntity<Map<String, Object>> getMessagesByRecipient(@RequestParam String recipient) {

        logger.info("👤 Obteniendo mensajes para destinatario: {}", recipient);

        List<MessageStatus> messages = messageStatusService.getMessagesByRecipient(recipient);
        Optional<MessageStatus> lastMessage = messageStatusService.getLastMessageToRecipient(recipient);

        Map<String, Object> response = new HashMap<>();
        response.put("recipient", recipient);
        response.put("messages", messages);
        response.put("totalCount", messages.size());
        response.put("lastMessage", lastMessage.orElse(null));
        response.put("timestamp", LocalDateTime.now());

        // Estadísticas del destinatario
        Map<String, Long> statusBreakdown = messages.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getStatus().name(),
                        Collectors.counting()
                ));
        response.put("statusBreakdown", statusBreakdown);

        logger.info("📊 Encontrados {} mensajes para {}", messages.size(), recipient);
        return ResponseEntity.ok(response);
    }

    /**
     * CASO DE USO 05: Obtener mensajes en un rango de fechas
     * GET /api/messages/by-date-range?start=2025-01-01T00:00:00&end=2025-01-31T23:59:59
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<Map<String, Object>> getMessagesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        logger.info("📅 Obteniendo mensajes entre {} y {}", start, end);

        List<MessageStatus> messages = messageStatusService.getMessagesBetweenDates(start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("startDate", start);
        response.put("endDate", end);
        response.put("messages", messages);
        response.put("totalCount", messages.size());
        response.put("timestamp", LocalDateTime.now());

        // Análisis por días
        Map<String, Long> messagesByDay = messages.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getTimestamp().toLocalDate().toString(),
                        Collectors.counting()
                ));
        response.put("dailyBreakdown", messagesByDay);

        logger.info("📈 Encontrados {} mensajes en el rango especificado", messages.size());
        return ResponseEntity.ok(response);
    }

    /**
     * CASO DE USO 05: Reintentar envío de mensaje fallido
     * POST /api/messages/{messageId}/retry
     */
    @PostMapping("/{messageId}/retry")
    public ResponseEntity<Map<String, Object>> retryMessage(@PathVariable String messageId) {

        logger.info("🔄 Solicitando reintento para mensaje: {}", messageId);

        // Verificar que el mensaje existe y está fallido
        Optional<MessageStatus> statusOpt = messageStatusService.getMessageStatus(messageId);

        Map<String, Object> response = new HashMap<>();

        if (!statusOpt.isPresent()) {
            response.put("success", false);
            response.put("error", "Mensaje no encontrado");
            return ResponseEntity.notFound().build();
        }

        MessageStatus status = statusOpt.get();

        if (!status.isFailed()) {
            response.put("success", false);
            response.put("error", "Solo se pueden reintentar mensajes fallidos");
            response.put("currentStatus", status.getStatus().name());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // TODO: Implementar lógica real de reintento
            // Por ahora, cambiar estado a QUEUED
            messageStatusService.updateMessageStatus(messageId, MessageStatusEnum.QUEUED, "Reintento solicitado por usuario");

            response.put("success", true);
            response.put("messageId", messageId);
            response.put("action", "retry");
            response.put("newStatus", "QUEUED");
            response.put("message", "Mensaje marcado para reintento");
            response.put("timestamp", LocalDateTime.now());

            logger.info("✅ Mensaje {} marcado para reintento", messageId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error al procesar reintento: " + e.getMessage());

            logger.error("❌ Error al reintentar mensaje {}: {}", messageId, e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * CASO DE USO 05: Dashboard con resumen rápido
     * GET /api/messages/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {

        logger.info("🎛️ Generando datos para dashboard");

        SystemStatusReport report = messageStatusService.getSystemStatusReport(); // ✅ CORREGIDO
        List<MessageStatus> recentMessages = messageStatusService.getRecentMessages();
        int scheduledCount = scheduledProcessor.getScheduledMessageCount();

        // Últimos 5 mensajes
        List<MessageStatus> latest5 = recentMessages.stream()
                .sorted((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()))
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();

        // Resumen ejecutivo
        response.put("summary", Map.of(
                "totalMessages", report.getTotalMessages(),
                "successRate", Math.round(report.getSuccessRate() * 100.0) / 100.0,
                "pendingMessages", report.getPendingMessages(),
                "failedMessages", report.getFailedMessages(),
                "scheduledMessages", scheduledCount
        ));

        // Actividad reciente
        response.put("recentActivity", Map.of(
                "last24Hours", report.getRecentMessages24h(),
                "latest5Messages", latest5
        ));

        // Estado del sistema
        response.put("systemHealth", Map.of(
                "status", determineSystemStatus(report.getPendingMessages(), report.getFailedMessages(), scheduledCount),
                "health", calculateSystemHealth(report),
                "uptime", "99.9%", // TODO: Calcular real uptime
                "lastCheck", LocalDateTime.now()
        ));

        // Alertas y recomendaciones
        response.put("alerts", generateAlerts(report, scheduledCount));
        response.put("quickActions", generateQuickActions(report));

        response.put("timestamp", LocalDateTime.now());

        logger.info("📊 Dashboard generado - {} mensajes totales, {}% éxito",
                report.getTotalMessages(), Math.round(report.getSuccessRate()));

        return ResponseEntity.ok(response);
    }

    // === MÉTODOS AUXILIARES ===

    private String calculateElapsedTime(LocalDateTime timestamp) {
        long minutes = java.time.Duration.between(timestamp, LocalDateTime.now()).toMinutes();
        if (minutes < 60) return minutes + " minutos";
        long hours = minutes / 60;
        if (hours < 24) return hours + " horas";
        long days = hours / 24;
        return days + " días";
    }

    private String determineSystemStatus(long pending, long failed, int scheduled) {
        if (failed > 10) return "WARNING";
        if (pending > 50) return "BUSY";
        if (scheduled > 0 || pending > 0) return "ACTIVE";
        return "IDLE";
    }

    private String calculateSystemHealth(SystemStatusReport report) { // ✅ CORREGIDO
        if (report.getSuccessRate() >= 95) return "EXCELLENT"; // ✅ USAR GETTER
        if (report.getSuccessRate() >= 85) return "GOOD";
        if (report.getSuccessRate() >= 70) return "FAIR";
        return "POOR";
    }

    private List<String> generateRecommendations(SystemStatusReport report, int scheduled) { // ✅ CORREGIDO
        List<String> recommendations = new java.util.ArrayList<>();

        if (report.getFailedMessages() > 5) { // ✅ USAR GETTER
            recommendations.add("Revisar mensajes fallidos y reintentar envío");
        }
        if (report.getSuccessRate() < 90) {
            recommendations.add("Verificar configuración de servicios de email/SMS");
        }
        if (scheduled > 20) {
            recommendations.add("Gran cantidad de mensajes programados - verificar capacidad");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Sistema funcionando correctamente");
        }

        return recommendations;
    }

    private List<Map<String, String>> generateAlerts(SystemStatusReport report, int scheduled) { // ✅ CORREGIDO
        List<Map<String, String>> alerts = new java.util.ArrayList<>();

        if (report.getFailedMessages() > 10) { // ✅ USAR GETTER
            alerts.add(Map.of(
                    "level", "ERROR",
                    "message", report.getFailedMessages() + " mensajes fallidos requieren atención",
                    "action", "Revisar y reintentar"
            ));
        }

        if (scheduled > 50) {
            alerts.add(Map.of(
                    "level", "WARNING",
                    "message", scheduled + " mensajes programados pendientes",
                    "action", "Verificar capacidad del sistema"
            ));
        }

        return alerts;
    }

    private List<Map<String, String>> generateQuickActions(SystemStatusReport report) { // ✅ CORREGIDO
        List<Map<String, String>> actions = new java.util.ArrayList<>();

        actions.add(Map.of(
                "label", "Enviar Mensaje",
                "endpoint", "/api/messaging/send",
                "icon", "send"
        ));

        if (report.getFailedMessages() > 0) { // ✅ USAR GETTER
            actions.add(Map.of(
                    "label", "Reintentar Fallidos",
                    "endpoint", "/api/messages/retry-all-failed",
                    "icon", "refresh"
            ));
        }

        actions.add(Map.of(
                "label", "Ver Historial",
                "endpoint", "/api/messages/history",
                "icon", "history"
        ));

        return actions;
    }
}