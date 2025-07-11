// domain/port/in/SendMessageUseCase.java
package com.tfgproject.domain.port.in;

import com.tfgproject.domain.model.MessageResult;
import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.application.command.SendSmsCommand;

public interface SendMessageUseCase {
    MessageResult sendEmail(SendEmailCommand command);
    MessageResult sendSms(SendSmsCommand command);
}