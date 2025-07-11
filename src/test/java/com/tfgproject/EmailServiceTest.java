package com.tfgproject;

import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.domain.model.MessageResult;
import com.tfgproject.domain.port.in.SendMessageUseCase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailServiceTest {



    @Autowired
    private SendMessageUseCase sendMessageUseCase; // Test de la arquitectura hexagonal



    @Test
    public void testSendEmailHexagonal() {
        // Test de la nueva arquitectura hexagonal
        SendEmailCommand command = new SendEmailCommand();
        command.setTo("bax6351@gmail.com");
        command.setSubject("Test arquitectura hexagonal");
        command.setText("Este es un mensaje de prueba de la arquitectura hexagonal.");

        MessageResult result = sendMessageUseCase.sendEmail(command);

        System.out.println("Resultado hexagonal: " + result.getMessage());
        System.out.println("Éxito: " + result.isSuccess());
    }

    @Test
    public void testEmailValidations() {
        System.out.println("=== TESTS DE VALIDACIÓN ===");

        // Test 1: Email vacío
        SendEmailCommand command1 = new SendEmailCommand();
        command1.setTo(""); // Vacío
        command1.setSubject("Test");
        command1.setText("Contenido");

        MessageResult result1 = sendMessageUseCase.sendEmail(command1);
        System.out.println("1. Email vacío - " + result1.getMessage() + " | Falló: " + !result1.isSuccess());

        // Test 2: Email null
        SendEmailCommand command2 = new SendEmailCommand();
        command2.setTo(null); // Null
        command2.setSubject("Test");
        command2.setText("Contenido");

        MessageResult result2 = sendMessageUseCase.sendEmail(command2);
        System.out.println("2. Email null - " + result2.getMessage() + " | Falló: " + !result2.isSuccess());

        // Test 3: Asunto vacío
        SendEmailCommand command3 = new SendEmailCommand();
        command3.setTo("test@example.com");
        command3.setSubject(""); // Vacío
        command3.setText("Contenido");

        MessageResult result3 = sendMessageUseCase.sendEmail(command3);
        System.out.println("3. Asunto vacío - " + result3.getMessage() + " | Falló: " + !result3.isSuccess());

        // Test 4: Contenido vacío
        SendEmailCommand command4 = new SendEmailCommand();
        command4.setTo("test@example.com");
        command4.setSubject("Test");
        command4.setText(""); // Vacío

        MessageResult result4 = sendMessageUseCase.sendEmail(command4);
        System.out.println("4. Contenido vacío - " + result4.getMessage() + " | Falló: " + !result4.isSuccess());

        // Test 5: Todo correcto (pero email fake para no enviar realmente)
        SendEmailCommand command5 = new SendEmailCommand();
        command5.setTo("test@fakeemail.com");
        command5.setSubject("Test válido");
        command5.setText("Contenido válido");

        MessageResult result5 = sendMessageUseCase.sendEmail(command5);
        System.out.println("5. Datos válidos - " + result5.getMessage() + " | Éxito: " + result5.isSuccess());
    }
}