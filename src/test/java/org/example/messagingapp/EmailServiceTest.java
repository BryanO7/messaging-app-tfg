package org.example.messagingapp.Test;

import org.example.messagingapp.model.EmailMessage;
import org.example.messagingapp.Service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void testSendEmail() {
        EmailMessage message = new EmailMessage();
        message.setTo("bax6351@outlook.es"); // Cambia por tu correo para pruebas
        message.setSubject("Prueba de envío de email");
        message.setText("Este es un mensaje de prueba desde la aplicación.");

        boolean result = emailService.sendEmail(message);
        System.out.println("Resultado del envío: " + result);
    }
}