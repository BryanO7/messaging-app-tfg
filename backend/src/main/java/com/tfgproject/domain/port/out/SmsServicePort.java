package com.tfgproject.domain.port.out;

import com.tfgproject.application.command.SendSmsCommand;

public interface SmsServicePort {
    boolean sendSms(SendSmsCommand command);
}