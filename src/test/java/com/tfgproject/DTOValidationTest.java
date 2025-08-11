// src/test/java/com/tfgproject/DTOValidationTest.java
package com.tfgproject;

import com.tfgproject.application.dto.request.ContactRequest;
import com.tfgproject.application.dto.request.CategoryRequest;
import com.tfgproject.application.dto.request.CategoryMessageRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties") // ✅ Usar perfil de test
public class DTOValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testContactRequestValidations() {
        System.out.println("🧪 === TEST VALIDACIONES ContactRequest ===");

        // === TEST 1: ContactRequest válido (EMAILS Y MÓVIL REALES) ===
        ContactRequest validContact = new ContactRequest();
        validContact.setName("Juan Pérez");
        validContact.setEmail("rybantfg@gmail.com"); // ✅ Email real
        validContact.setPhone("644023859"); // ✅ Móvil real español
        validContact.setNotes("Contacto de prueba");

        Set<ConstraintViolation<ContactRequest>> violations = validator.validate(validContact);
        System.out.println("✅ ContactRequest válido - Violaciones: " + violations.size());
        assert violations.isEmpty() : "ContactRequest válido no debería tener violaciones";

        // === TEST 2: Nombre vacío ===
        ContactRequest invalidName = new ContactRequest();
        invalidName.setName(""); // Inválido
        invalidName.setEmail("bax6351@gmail.com"); // ✅ Email real
        invalidName.setPhone("644023859"); // ✅ Móvil real

        violations = validator.validate(invalidName);
        System.out.println("❌ Nombre vacío - Violaciones: " + violations.size());
        violations.forEach(v -> System.out.println("   " + v.getPropertyPath() + ": " + v.getMessage()));
        assert !violations.isEmpty() : "Nombre vacío debería ser inválido";

        // === TEST 3: Email inválido ===
        ContactRequest invalidEmail = new ContactRequest();
        invalidEmail.setName("Test User");
        invalidEmail.setEmail("email-invalido"); // Inválido
        invalidEmail.setPhone("644023859"); // ✅ Móvil real

        violations = validator.validate(invalidEmail);
        System.out.println("❌ Email inválido - Violaciones: " + violations.size());
        violations.forEach(v -> System.out.println("   " + v.getPropertyPath() + ": " + v.getMessage()));
        assert !violations.isEmpty() : "Email inválido debería ser inválido";

        // === TEST 4: Teléfono inválido ===
        ContactRequest invalidPhone = new ContactRequest();
        invalidPhone.setName("Test User");
        invalidPhone.setEmail("bryanoyonate07@gmail.com"); // ✅ Email real
        invalidPhone.setPhone("123"); // Inválido (muy corto)

        violations = validator.validate(invalidPhone);
        System.out.println("❌ Teléfono inválido - Violaciones: " + violations.size());
        violations.forEach(v -> System.out.println("   " + v.getPropertyPath() + ": " + v.getMessage()));
        assert !violations.isEmpty() : "Teléfono inválido debería ser inválido";

        // === TEST 5: Validación personalizada - sin canales de comunicación ===
        ContactRequest noChannels = new ContactRequest();
        noChannels.setName("Test User");
        noChannels.setNotes("Usuario sin email ni teléfono");

        violations = validator.validate(noChannels);
        System.out.println("✅ Sin canales - Violaciones Bean Validation: " + violations.size());

        // Probar validación personalizada
        try {
            noChannels.validateContactChannels();
            System.out.println("❌ ERROR: Debería haber lanzado excepción");
            assert false : "Debería haber lanzado excepción para contacto sin canales";
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Validación personalizada funcionó: " + e.getMessage());
        }

        System.out.println("✅ Test ContactRequest validaciones completado\n");
    }

    @Test
    public void testCategoryRequestValidations() {
        System.out.println("🧪 === TEST VALIDACIONES CategoryRequest ===");

        // === TEST 1: CategoryRequest válido ===
        CategoryRequest validCategory = new CategoryRequest();
        validCategory.setName("Grupo de Pesca");
        validCategory.setDescription("Aficionados a la pesca deportiva");

        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(validCategory);
        System.out.println("✅ CategoryRequest válido - Violaciones: " + violations.size());
        assert violations.isEmpty() : "CategoryRequest válido no debería tener violaciones";

        // === TEST 2: Nombre vacío ===
        CategoryRequest invalidName = new CategoryRequest();
        invalidName.setName(""); // Inválido
        invalidName.setDescription("Descripción válida");

        violations = validator.validate(invalidName);
        System.out.println("❌ Nombre vacío - Violaciones: " + violations.size());
        violations.forEach(v -> System.out.println("   " + v.getPropertyPath() + ": " + v.getMessage()));
        assert !violations.isEmpty() : "Nombre vacío debería ser inválido";

        // === TEST 3: Descripción muy larga ===
        CategoryRequest longDescription = new CategoryRequest();
        longDescription.setName("Categoría válida");
        longDescription.setDescription("A".repeat(400)); // Excede 300 caracteres

        violations = validator.validate(longDescription);
        System.out.println("❌ Descripción larga - Violaciones: " + violations.size());
        violations.forEach(v -> System.out.println("   " + v.getPropertyPath() + ": " + v.getMessage()));
        assert !violations.isEmpty() : "Descripción muy larga debería ser inválida";

        // === TEST 4: Subcategoría válida ===
        CategoryRequest subcategory = new CategoryRequest();
        subcategory.setName("Pesca de Río");
        subcategory.setDescription("Especializada en pesca fluvial");
        subcategory.setParentId(1L);

        violations = validator.validate(subcategory);
        System.out.println("✅ Subcategoría válida - Violaciones: " + violations.size());
        System.out.println("✅ Es subcategoría: " + subcategory.isSubcategory());
        assert violations.isEmpty() : "Subcategoría válida no debería tener violaciones";

        System.out.println("✅ Test CategoryRequest validaciones completado\n");
    }

    @Test
    public void testCategoryMessageRequestValidations() {
        System.out.println("🧪 === TEST VALIDACIONES CategoryMessageRequest ===");

        // === TEST 1: Mensaje válido ===
        CategoryMessageRequest validMessage = new CategoryMessageRequest();
        validMessage.setSubject("Convocatoria de Pesca");
        validMessage.setContent("Salida programada para el sábado a las 6:00 AM");
        validMessage.setSendEmail(true);
        validMessage.setSendSms(false);

        Set<ConstraintViolation<CategoryMessageRequest>> violations = validator.validate(validMessage);
        System.out.println("✅ Mensaje válido - Violaciones: " + violations.size());
        assert violations.isEmpty() : "Mensaje válido no debería tener violaciones";

        // === TEST 2: Contenido vacío ===
        CategoryMessageRequest emptyContent = new CategoryMessageRequest();
        emptyContent.setSubject("Asunto válido");
        emptyContent.setContent(""); // Inválido
        emptyContent.setSendEmail(true);

        violations = validator.validate(emptyContent);
        System.out.println("❌ Contenido vacío - Violaciones: " + violations.size());
        violations.forEach(v -> System.out.println("   " + v.getPropertyPath() + ": " + v.getMessage()));
        assert !violations.isEmpty() : "Contenido vacío debería ser inválido";

        // === TEST 3: Asunto muy largo ===
        CategoryMessageRequest longSubject = new CategoryMessageRequest();
        longSubject.setSubject("A".repeat(250)); // Excede 200 caracteres
        longSubject.setContent("Contenido válido");
        longSubject.setSendEmail(true);

        violations = validator.validate(longSubject);
        System.out.println("❌ Asunto largo - Violaciones: " + violations.size());
        violations.forEach(v -> System.out.println("   " + v.getPropertyPath() + ": " + v.getMessage()));
        assert !violations.isEmpty() : "Asunto muy largo debería ser inválido";

        // === TEST 4: Sin canales seleccionados ===
        CategoryMessageRequest noChannels = new CategoryMessageRequest();
        noChannels.setSubject("Asunto válido");
        noChannels.setContent("Contenido válido");
        noChannels.setSendEmail(false);
        noChannels.setSendSms(false);

        violations = validator.validate(noChannels);
        System.out.println("❌ Sin canales - Violaciones: " + violations.size());
        violations.forEach(v -> System.out.println("   " + v.getPropertyPath() + ": " + v.getMessage()));
        assert !violations.isEmpty() : "Sin canales debería ser inválido";

        // === TEST 5: Métodos de conveniencia ===
        CategoryMessageRequest allChannels = new CategoryMessageRequest();
        allChannels.setSendEmail(true);
        allChannels.setSendSms(true);

        System.out.println("✅ Enviar a todos los canales: " + allChannels.shouldSendToAllChannels());
        assert allChannels.shouldSendToAllChannels() : "Debería enviar a todos los canales";

        System.out.println("✅ Test CategoryMessageRequest validaciones completado\n");
    }

    @Test
    public void testAllDTOsIntegration() {
        System.out.println("🚀 === TEST INTEGRACIÓN TODOS LOS DTOs ===");

        // Crear objetos válidos de todos los DTOs (EMAILS Y MÓVIL REALES)
        ContactRequest contact = new ContactRequest("Juan Pescador", "rybantfg@gmail.com", "644023859", null, "Pescador experto");
        CategoryRequest category = new CategoryRequest("Grupo de Pesca", "Aficionados a la pesca", null);
        CategoryMessageRequest message = new CategoryMessageRequest("Convocatoria", "Salida de pesca el sábado", true, true);

        // Validar todos
        Set<ConstraintViolation<ContactRequest>> contactViolations = validator.validate(contact);
        Set<ConstraintViolation<CategoryRequest>> categoryViolations = validator.validate(category);
        Set<ConstraintViolation<CategoryMessageRequest>> messageViolations = validator.validate(message);

        System.out.println("📊 Resultados de validación:");
        System.out.println("  👤 ContactRequest: " + contactViolations.size() + " violaciones");
        System.out.println("  📁 CategoryRequest: " + categoryViolations.size() + " violaciones");
        System.out.println("  📧 MessageRequest: " + messageViolations.size() + " violaciones");

        // Verificar validaciones personalizadas
        System.out.println("🔧 Validaciones personalizadas:");
        System.out.println("  👤 Contacto tiene canales válidos: " + contact.hasValidContactInfo());
        System.out.println("  📁 Categoría es subcategoría: " + category.isSubcategory());
        System.out.println("  📧 Mensaje a todos los canales: " + message.shouldSendToAllChannels());

        // Assertions
        assert contactViolations.isEmpty() : "ContactRequest debería ser válido";
        assert categoryViolations.isEmpty() : "CategoryRequest debería ser válido";
        assert messageViolations.isEmpty() : "MessageRequest debería ser válido";

        System.out.println("✅ Integración de todos los DTOs exitosa");
        System.out.println("🎉 ¡Estructura de DTOs lista para producción!");
    }
}