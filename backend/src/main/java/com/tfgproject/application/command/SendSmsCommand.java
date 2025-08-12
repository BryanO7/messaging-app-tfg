package com.tfgproject.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.tfgproject.shared.model.SmsMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendSmsCommand {
    private String to;
    private String text;
    private String sender;

    public static SendSmsCommand fromSmsMessage(SmsMessage smsMessage) {
        SendSmsCommand command = new SendSmsCommand();
        command.setTo(smsMessage.getTo());
        command.setText(smsMessage.getText());
        command.setSender(smsMessage.getSender());
        return command;
    }
}