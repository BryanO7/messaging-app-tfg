// src/main/java/com/tfgproject/domain/model/SystemStatusReport.java
package com.tfgproject.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Reporte completo del estado del sistema de mensajería
 * Modelo de dominio para representar métricas y estadísticas del sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatusReport {

    private long totalMessages;
    private long successfulMessages;
    private long failedMessages;
    private long pendingMessages;
    private double successRate;
    private int recentMessages24h;
    private LocalDateTime timestamp;

    // === MÉTODOS DE CÁLCULO ===

    /**
     * Calcula la tasa de fallos
     */
    public double getFailureRate() {
        if (totalMessages == 0) return 0.0;
        return (double) failedMessages / totalMessages * 100.0;
    }

    /**
     * Calcula el porcentaje de mensajes pendientes
     */
    public double getPendingRate() {
        if (totalMessages == 0) return 0.0;
        return (double) pendingMessages / totalMessages * 100.0;
    }

    /**
     * Determina la salud del sistema basado en métricas
     */
    public SystemHealth getSystemHealth() {
        if (failedMessages > 20) return SystemHealth.CRITICAL;
        if (successRate < 70) return SystemHealth.POOR;
        if (successRate < 85) return SystemHealth.FAIR;
        if (successRate < 95) return SystemHealth.GOOD;
        return SystemHealth.EXCELLENT;
    }

    /**
     * Determina el estado operacional del sistema
     */
    public SystemStatus getSystemStatus() {
        if (failedMessages > 10) return SystemStatus.WARNING;
        if (pendingMessages > 50) return SystemStatus.BUSY;
        if (pendingMessages > 0 || recentMessages24h > 0) return SystemStatus.ACTIVE;
        return SystemStatus.IDLE;
    }

    /**
     * Verifica si el sistema está saludable
     */
    public boolean isHealthy() {
        return getSystemHealth() != SystemHealth.CRITICAL &&
                getSystemHealth() != SystemHealth.POOR;
    }

    /**
     * Verifica si hay alertas críticas
     */
    public boolean hasCriticalAlerts() {
        return failedMessages > 20 || successRate < 70;
    }

    // === FACTORY METHODS ===

    /**
     * Crea un reporte con datos básicos
     */
    public static SystemStatusReport create(long total, long successful, long failed, long pending) {
        SystemStatusReport report = new SystemStatusReport();
        report.totalMessages = total;
        report.successfulMessages = successful;
        report.failedMessages = failed;
        report.pendingMessages = pending;
        report.successRate = total > 0 ? (double) successful / total * 100.0 : 0.0;
        report.timestamp = LocalDateTime.now();
        return report;
    }

    /**
     * Crea un reporte completo
     */
    public static SystemStatusReport createComplete(long total, long successful, long failed,
                                                    long pending, int recent24h) {
        SystemStatusReport report = create(total, successful, failed, pending);
        report.recentMessages24h = recent24h;
        return report;
    }

    /**
     * Crea un reporte vacío (sistema sin actividad)
     */
    public static SystemStatusReport empty() {
        SystemStatusReport report = new SystemStatusReport();
        report.timestamp = LocalDateTime.now();
        return report;
    }

    @Override
    public String toString() {
        return String.format(
                "SystemStatusReport{total=%d, successful=%d, failed=%d, pending=%d, " +
                        "successRate=%.2f%%, health=%s, recent24h=%d}",
                totalMessages, successfulMessages, failedMessages, pendingMessages,
                successRate, getSystemHealth(), recentMessages24h
        );
    }

    // === ENUMS RELACIONADOS ===

    /**
     * Estados de salud del sistema
     */
    public enum SystemHealth {
        EXCELLENT("Excelente", "🟢"),
        GOOD("Bueno", "🟡"),
        FAIR("Regular", "🟠"),
        POOR("Pobre", "🔴"),
        CRITICAL("Crítico", "🚨");

        private final String displayName;
        private final String emoji;

        SystemHealth(String displayName, String emoji) {
            this.displayName = displayName;
            this.emoji = emoji;
        }

        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
    }

    /**
     * Estados operacionales del sistema
     */
    public enum SystemStatus {
        IDLE("Inactivo", "💤"),
        ACTIVE("Activo", "🟢"),
        BUSY("Ocupado", "🟡"),
        WARNING("Advertencia", "🟠");

        private final String displayName;
        private final String emoji;

        SystemStatus(String displayName, String emoji) {
            this.displayName = displayName;
            this.emoji = emoji;
        }

        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
    }
}