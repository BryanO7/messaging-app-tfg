package com.tfgproject.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.tfgproject.model.EmailMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailCommand {
    private String to;
    private String subject;
    private String text;
    private String sender;

    public static SendEmailCommand fromEmailMessage(EmailMessage emailMessage) {
        SendEmailCommand command = new SendEmailCommand();
        command.setTo(emailMessage.getTo());
        command.setSubject(emailMessage.getSubject());
        command.setText(emailMessage.getText());
        return command;
    }
}