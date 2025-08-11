// src/test/java/com/tfgproject/TestConstants.java
package com.tfgproject;

import java.util.Arrays;
import java.util.List;

/**
 * Constantes para testing con datos reales
 */
public class TestConstants {

    // === EMAILS REALES PARA TESTING ===
    public static final String EMAIL_1 = "rybantfg@gmail.com";
    public static final String EMAIL_2 = "bax6351@gmail.com";
    public static final String EMAIL_3 = "bryanoyonate07@gmail.com";

    public static final List<String> ALL_TEST_EMAILS = Arrays.asList(
            EMAIL_1, EMAIL_2, EMAIL_3
    );

    // === MÓVIL ESPAÑOL REAL PARA TESTING ===
    public static final String SPANISH_MOBILE = "644023859";

    // === DATOS DE CONTACTOS DE PRUEBA ===
    public static final String[] CONTACT_NAMES = {
            "Juan el Pescador",
            "María la Pescadora",
            "Carlos el Capitán",
            "Ana la Fotógrafa",
            "Pedro el Desarrollador",
            "Lucía la Manager"
    };

    // === MENSAJES DE PRUEBA ===
    public static final String FISHING_GROUP_MESSAGE =
            "🎣 ¡CONVOCATORIA GRUPO DE PESCA! 🎣\n\n" +
                    "Estimados pescadores,\n\n" +
                    "Se convoca a todos los miembros del grupo para la próxima salida de pesca:\n\n" +
                    "📅 Fecha: Sábado 10 de Febrero\n" +
                    "🕕 Hora: 06:00 AM\n" +
                    "📍 Lugar: Embalse de Santillana\n" +
                    "🎯 Modalidad: Pesca de carpa\n\n" +
                    "Equipamiento necesario:\n" +
                    "• Caña de pescar (3-4 metros)\n" +
                    "• Cebo (maíz y lombrices)\n" +
                    "• Silla plegable\n" +
                    "• Termo con café ☕\n\n" +
                    "¡Confirmar asistencia por favor!\n\n" +
                    "Tight lines! 🎣\n" +
                    "Grupo de Pesca TFG";

    public static final String SMS_SHORT_MESSAGE =
            "🎣 GRUPO PESCA: Salida Sábado 10/Feb 06:00 AM - Embalse Santillana. " +
                    "Confirmar asistencia. Info completa en email. ¡Nos vemos!";

    // === CONFIGURACIÓN PARA TESTING ===
    public static final String TEST_SENDER = "TFG-Pesca";
    public static final String TEST_SUBJECT_PREFIX = "🎣 TFG Test - ";

    // === CATEGORÍAS DE PRUEBA ===
    public static final String FISHING_CATEGORY = "Grupo de Pesca Test";
    public static final String WORK_CATEGORY = "Trabajo Test";
    public static final String FAMILY_CATEGORY = "Familia Test";

    // === MÉTODOS DE UTILIDAD ===
    public static String getTestSubject(String suffix) {
        return TEST_SUBJECT_PREFIX + suffix + " - " + System.currentTimeMillis();
    }

    public static String getTestMessage(String baseMessage) {
        return baseMessage + "\n\n🕐 Enviado: " + java.time.LocalDateTime.now() +
                "\n🧪 Test ID: " + System.currentTimeMillis();
    }

    public static void printTestHeader(String testName) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🧪 " + testName.toUpperCase());
        System.out.println("=".repeat(60));
        System.out.println("📧 Emails de prueba: " + String.join(", ", ALL_TEST_EMAILS));
        System.out.println("📱 Móvil de prueba: " + SPANISH_MOBILE);
        System.out.println("=".repeat(60));
    }

    public static void printTestFooter() {
        System.out.println("=".repeat(60));
        System.out.println("✅ TEST COMPLETADO - Revisa tus emails y móvil");
        System.out.println("=".repeat(60) + "\n");
    }
}