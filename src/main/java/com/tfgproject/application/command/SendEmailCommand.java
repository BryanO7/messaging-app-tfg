package com.tfgproject.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.tfgproject.shared.model.EmailMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailCommand {
    private String to;
    private String subject;
    private String text;
    private String sender;
    private String attachmentPath;  // ← NUEVO
    private boolean html = false;   // ← NUEVO

    public boolean hasAttachment() {
        return attachmentPath != null && !attachmentPath.trim().isEmpty();
    }

    public static SendEmailCommand fromEmailMessage(EmailMessage emailMessage) {
        SendEmailCommand command = new SendEmailCommand();
        command.setTo(emailMessage.getTo());
        command.setSubject(emailMessage.getSubject());
        command.setText(emailMessage.getText());
        command.setAttachmentPath(emailMessage.getAttachmentPath());  // ← NUEVO
        command.setHtml(emailMessage.isHtml());  // ← NUEVO
        return command;
    }
}