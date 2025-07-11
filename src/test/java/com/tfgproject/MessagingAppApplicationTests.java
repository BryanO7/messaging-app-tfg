package com.tfgproject;

import com.tfgproject.Service.EmailService;
import com.tfgproject.model.EmailMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MessagingAppApplicationTests {


    @Autowired
    private EmailService emailService;

    @Test
    public void testSendEmail() {
        // Configurar el mensaje
        EmailMessage message = new EmailMessage();
        message.setTo("bax6351@gmail.com"); // Cambia por tu email real
        message.setSubject("Test desde JUnit");
        message.setText("Este es un mensaje de prueba desde una prueba JUnit.");

        // Intentar enviar el email
        boolean result = emailService.sendEmail(message);

        // Imprimir el resultado
        System.out.println("Resultado del envío: " + (result ? "ÉXITO" : "FALLO"));
    }
}
