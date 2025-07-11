package com.tfgproject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessage {
    private String to;
    private String text;
    private String sender;

    // Constructor básico
    public SmsMessage(String to, String text) {
        this.to = to;
        this.text = text;
        this.sender = "TFG-App"; // Remitente alfanumérico por defecto
    }
}