package com.tfgproject.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResult {
    private boolean success;
    private String message;
    private String messageId;

    public static MessageResult success(String message) {
        return new MessageResult(true, message, null);
    }



    public static MessageResult failure(String message) {
        return new MessageResult(false, message, null);
    }
}