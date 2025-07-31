package com.tfgproject.domain.model;

public enum MessageStatusEnum {
    QUEUED("En Cola"),
    PROCESSING("Procesando"),
    SENT("Enviado"),
    DELIVERED("Entregado"),
    FAILED("Fallido"),
    SCHEDULED("Programado"),
    CANCELLED("Cancelado");

    private final String displayName;

    MessageStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
