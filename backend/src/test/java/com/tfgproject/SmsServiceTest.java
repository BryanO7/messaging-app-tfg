package com.tfgproject;

import com.tfgproject.application.command.SendSmsCommand;
import com.tfgproject.domain.model.MessageResult;
import com.tfgproject.domain.port.in.SendMessageUseCase;
import com.tfgproject.shared.model.SmsMessage;
import com.tfgproject.infrastructure.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SmsServiceTest {



    @Autowired
    private SendMessageUseCase sendMessageUseCase; // Agregar esta inyección

    @Test

    public void testSendSmsHexagonal() {
        SendSmsCommand command = new SendSmsCommand();
        command.setTo("644023859");
        command.setText("Test SMS hexagonal");
        command.setSender("TFG-App");

        MessageResult result = sendMessageUseCase.sendSms(command);

        System.out.println("SMS resultado: " + result.getMessage());
        System.out.println("SMS éxito: " + result.isSuccess());
    }
}