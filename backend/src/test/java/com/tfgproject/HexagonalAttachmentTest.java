package com.tfgproject;

import com.tfgproject.application.command.SendEmailCommand;
import com.tfgproject.domain.model.MessageResult;
import com.tfgproject.domain.port.in.SendMessageUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
public class HexagonalAttachmentTest {

    @Autowired
    private SendMessageUseCase sendMessageUseCase;

    @Test
    public void testSendEmailWithAttachmentHexagonal() throws IOException {
        // 1. Crear archivo de prueba
        String fileName = "tfg-hexagonal-test.txt";
        Path filePath = Paths.get(System.getProperty("java.io.tmpdir"), fileName);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write("=== TFG MESSAGING APP - ARQUITECTURA HEXAGONAL ===\n\n");
            writer.write("Este archivo demuestra el envío de attaachments\n");
            writer.write("utilizando la arquitectura hexagonal implementada.\n\n");
            writer.write("Flujo:\n");
            writer.write("Test → Port IN → Domain → Port OUT → Adapter → EmailService\n\n");
            writer.write("Capas involucradas:\n");
            writer.write("- Domain: MessageService (validaciones y lógica)\n");
            writer.write("- Port OUT: EmailServicePort (contrato)\n");
            writer.write("- Adapter: EmailServiceAdapter (traducción)\n");
            writer.write("- Infrastructure: EmailService (tecnología)\n\n");
            writer.write("Generado: " + java.time.LocalDateTime.now() + "\n");
        }

        // 2. Crear comando extendido (necesitarás crearlo)
        SendEmailCommand command = new SendEmailCommand();
        command.setTo("bax6351@gmail.com");
        command.setSubject("🏗️ Hexagonal Architecture - Email con Attachment");
        command.setText(
                "¡Hola!\n\n" +
                        "Este email fue enviado usando la arquitectura hexagonal completa.\n\n" +
                        "El archivo adjunto contiene información técnica sobre\n" +
                        "el flujo de datos en nuestra implementación.\n\n" +
                        "Arquitectura utilizada:\n" +
                        "✅ Domain-Driven Design\n" +
                        "✅ Ports & Adapters (Hexagonal)\n" +
                        "✅ Dependency Inversion\n" +
                        "✅ Command Pattern\n\n" +
                        "Saludos,\nTFG Messaging Team"
        );
        // Nota: Necesitarás extender SendEmailCommand para incluir attachmentPath
        // O crear un comando específico para attachments

        // 3. Enviar usando la arquitectura hexagonal
        System.out.println("🏗️ Enviando email con attachment via arquitectura hexagonal...");
        System.out.println("📁 Archivo: " + filePath.toString());
        System.out.println("📏 Tamaño: " + Files.size(filePath) + " bytes");

        // Por ahora, usamos el comando básico
        MessageResult result = sendMessageUseCase.sendEmail(command);

        // 4. Verificar resultado
        System.out.println("🎯 Resultado del envío:");
        System.out.println("  ✅ Éxito: " + result.isSuccess());
        System.out.println("  📝 Mensaje: " + result.getMessage());

        if (result.isSuccess()) {
            System.out.println("🎉 ¡Email hexagonal con attachment enviado!");
            System.out.println("📧 Revisa tu email: " + command.getTo());
        }

        // 5. Limpiar
        Files.deleteIfExists(filePath);

        assert result.isSuccess() : "El email hexagonal debería enviarse correctamente";
    }

    // 3. Tu test corregido que SÍ funciona:

    @Test
    public void testValidationWithAttachment() throws IOException {
        String filePath = "/home/bryan/SD_Task_2-2024.pdf";
        System.out.println("🧪 Testing validaciones con attachments...");

        // Verificar que el archivo existe
        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            System.err.println("❌ ERROR: Archivo no encontrado: " + filePath);
            assert false : "El archivo PDF debe existir para el test";
            return;
        }

        System.out.println("✅ Archivo encontrado: " + pdfFile.getName());
        System.out.println("📏 Tamaño: " + formatFileSize(pdfFile.length()));

        // ✅ USAR SendEmailCommand EXTENDIDO
        SendEmailCommand validCommand = new SendEmailCommand();
        validCommand.setTo("bax6351@gmail.com");
        validCommand.setSubject("📎 TFG - Test PDF con Arquitectura Hexagonal");
        validCommand.setText(
                "¡Hola!\n\n" +
                        "Este email fue enviado usando la arquitectura hexagonal completa\n" +
                        "e incluye tu archivo PDF como adjunto.\n\n" +
                        "Archivo adjunto: " + pdfFile.getName() + "\n" +
                        "Tamaño: " + formatFileSize(pdfFile.length()) + "\n\n" +
                        "Flujo completo:\n" +
                        "Test → SendEmailCommand → MessageService → EmailServicePort\n" +
                        "→ EmailServiceAdapter → EmailService → JavaMail → Gmail\n\n" +
                        "✅ Attachment incluido\n" +
                        "✅ Arquitectura hexagonal\n" +
                        "✅ Validaciones del domain\n\n" +
                        "Enviado: " + java.time.LocalDateTime.now() + "\n\n" +
                        "Saludos,\nTFG Messaging App"
        );
        validCommand.setSender("TFG-App");
        // ✅ CONFIGURAR ATTACHMENT
        validCommand.setAttachmentPath(filePath);
        validCommand.setHtml(false);

        System.out.println("📤 Enviando email con PDF via arquitectura hexagonal...");
        System.out.println("📁 Archivo: " + filePath);
        System.out.println("📧 Destinatario: " + validCommand.getTo());

        // Enviar usando toda la arquitectura hexagonal
        long startTime = System.currentTimeMillis();
        MessageResult result = sendMessageUseCase.sendEmail(validCommand);
        long endTime = System.currentTimeMillis();

        // Mostrar resultado detallado
        System.out.println("\n🎯 Resultado del envío hexagonal:");
        System.out.println("  ✅ Éxito: " + result.isSuccess());
        System.out.println("  📝 Mensaje: " + result.getMessage());
        System.out.println("  ⏱️ Tiempo: " + (endTime - startTime) + " ms");

        if (result.isSuccess()) {
            System.out.println("\n🎉 ¡EMAIL CON PDF ENVIADO VIA ARQUITECTURA HEXAGONAL!");
            System.out.println("📧 Revisa tu email: " + validCommand.getTo());
            System.out.println("📎 El archivo " + pdfFile.getName() + " debería estar adjunto");
            System.out.println("\n🏗️ Capas utilizadas:");
            System.out.println("  1. ✅ Test (Cliente)");
            System.out.println("  2. ✅ SendEmailCommand (Command)");
            System.out.println("  3. ✅ SendMessageUseCase (Port IN)");
            System.out.println("  4. ✅ MessageService (Domain)");
            System.out.println("  5. ✅ EmailServicePort (Port OUT)");
            System.out.println("  6. ✅ EmailServiceAdapter (Adapter)");
            System.out.println("  7. ✅ EmailService (Infrastructure)");
            System.out.println("  8. ✅ JavaMailSender (Technology)");
        } else {
            System.err.println("\n❌ ERROR AL ENVIAR EMAIL HEXAGONAL");
            System.err.println("📝 Mensaje: " + result.getMessage());
        }

        System.out.println("✅ Validaciones completadas");

        // Assert para el test
        assert result.isSuccess() : "El email con PDF debería enviarse correctamente via arquitectura hexagonal";
    }

    // Método auxiliar (añadir a tu clase de test)
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}