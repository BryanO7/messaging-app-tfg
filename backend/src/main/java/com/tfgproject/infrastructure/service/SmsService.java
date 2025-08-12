package com.tfgproject.infrastructure.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.tfgproject.shared.model.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    public boolean sendSms(SmsMessage smsMessage) {
        try {
            logger.info("Enviando SMS a {} desde {}", smsMessage.getTo(), smsMessage.getSender());

            // Formato internacional para España
            String formattedNumber = smsMessage.getTo();
            if (!formattedNumber.startsWith("+")) {
                formattedNumber = "+34" + formattedNumber;
            }

            Message message = Message.creator(
                    new PhoneNumber(formattedNumber),
                    new com.twilio.type.PhoneNumber(smsMessage.getSender()),
                    smsMessage.getText()
            ).create();

            logger.info("SMS enviado, SID: {}", message.getSid());
            return true;
        } catch (Exception e) {
            logger.error("Error enviando SMS: {}", e.getMessage(), e);
            return false;
        }
    }

    // Método de conveniencia para el modo más simple
    public boolean sendSms(String to, String text, String sender) {
        SmsMessage smsMessage = new SmsMessage(to, text, sender);
        return sendSms(smsMessage);
    }

    // Método de conveniencia con remitente por defecto
    public boolean sendSms(String to, String text) {
        SmsMessage smsMessage = new SmsMessage(to, text);
        return sendSms(smsMessage);
    }
}