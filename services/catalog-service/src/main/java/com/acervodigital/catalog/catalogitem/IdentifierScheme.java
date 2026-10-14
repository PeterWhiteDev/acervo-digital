package com.acervodigital.catalog.catalogitem;

import java.util.Locale;

public enum IdentifierScheme {
    ISBN_10,
    ISBN_13,
    EAN_13,
    EDITION_CODE;

    String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador no puede estar vacío");
        }

        return switch (this) {
            case ISBN_10 -> normalizeIsbn10(value);
            case ISBN_13 -> normalizeIsbn13(value);
            case EAN_13 -> normalizeEan13(value);
            case EDITION_CODE -> normalizeEditionCode(value);
        };
    }

    private static String normalizeIsbn10(String value) {
        String normalized = removeNumericSeparators(value).toUpperCase(Locale.ROOT);
        if (!normalized.matches("[0-9]{9}[0-9X]") || !hasValidIsbn10CheckDigit(normalized)) {
            throw new IllegalArgumentException("ISBN-10 inválido");
        }
        return normalized;
    }

    private static String normalizeIsbn13(String value) {
        String normalized = removeNumericSeparators(value);
        if (!normalized.matches("97[89][0-9]{10}") || !hasValidEanCheckDigit(normalized)) {
            throw new IllegalArgumentException("ISBN-13 inválido");
        }
        return normalized;
    }

    private static String normalizeEan13(String value) {
        String normalized = removeNumericSeparators(value);
        if (!normalized.matches("[0-9]{13}") || !hasValidEanCheckDigit(normalized)) {
            throw new IllegalArgumentException("EAN-13 inválido");
        }
        return normalized;
    }

    private static String normalizeEditionCode(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("El código de edición supera los 100 caracteres");
        }
        return normalized;
    }

    private static String removeNumericSeparators(String value) {
        return value.replace("-", "").replace(" ", "").trim();
    }

    private static boolean hasValidIsbn10CheckDigit(String value) {
        int sum = 0;
        for (int index = 0; index < 10; index++) {
            int digit = value.charAt(index) == 'X' ? 10 : Character.digit(value.charAt(index), 10);
            sum += digit * (10 - index);
        }
        return sum % 11 == 0;
    }

    private static boolean hasValidEanCheckDigit(String value) {
        int sum = 0;
        for (int index = 0; index < 12; index++) {
            int digit = Character.digit(value.charAt(index), 10);
            sum += digit * (index % 2 == 0 ? 1 : 3);
        }
        int expectedCheckDigit = (10 - sum % 10) % 10;
        return expectedCheckDigit == Character.digit(value.charAt(12), 10);
    }
}