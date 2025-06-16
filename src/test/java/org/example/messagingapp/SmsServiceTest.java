package org.example.messagingapp;

import org.example.messagingapp.model.SmsMessage;
import org.example.messagingapp.Service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SmsServiceTest {

    @Autowired
    private SmsService smsService;

    @Test
    public void testSendSingleSms() {
        System.out.println("Iniciando prueba de envío de SMS único...");

        // Configurar el mensaje - reemplaza con tu número
        SmsMessage message = new SmsMessage();
        message.setTo("644023859");
        message.setText("Prueba única de SMS desde TFG Mensajería Unificada.");
        message.setSender("TFG-App");

        // Enviar SMS
        boolean result = smsService.sendSms(message);

        // Mostrar resultado
        System.out.println("Resultado del envío: " + (result ? "ÉXITO ✓" : "FALLO ✗"));
        System.out.println("Si el envío fue exitoso, deberías recibir el SMS en tu teléfono pronto.");
    }
}