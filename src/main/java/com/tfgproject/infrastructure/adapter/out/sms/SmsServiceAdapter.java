package com.tfgproject.infrastructure.adapter.out.sms;
import com.tfgproject.infrastructure.service.SmsService; // ¿Esta ruta es correcta?
import com.tfgproject.application.command.SendSmsCommand;
import com.tfgproject.domain.port.out.SmsServicePort;
import com.tfgproject.shared.model.SmsMessage; // ¿Esta ruta es correcta?
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmsServiceAdapter implements SmsServicePort {

    @Autowired
    private SmsService smsService;

    @Override
    public boolean sendSms(SendSmsCommand command) {
        System.out.println("🔍 SmsServiceAdapter: Enviando SMS a " + command.getTo()); // Log de debug

        SmsMessage smsMessage = new SmsMessage();
        smsMessage.setTo(command.getTo());
        smsMessage.setText(command.getText());
        smsMessage.setSender(command.getSender());

        boolean result = smsService.sendSms(smsMessage);
        System.out.println("🔍 SmsServiceAdapter: Resultado = " + result); // Log de debug

        return result;
    }
}