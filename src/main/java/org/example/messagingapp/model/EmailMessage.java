package org.example.messagingapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
    private String to;
    private String subject;
    private String text;
    private String attachmentPath;
    private boolean html = false;

    // Constructor para mensaje simple
    public EmailMessage(String to, String subject, String text) {
        this.to = to;
        this.subject = subject;
        this.text = text;
    }

    // Constructor para mensaje simple + html
    public EmailMessage(String to, String subject, String text, boolean html) {
        this.to = to;
        this.subject = subject;
        this.text = text;
        this.html = html;
    }
}