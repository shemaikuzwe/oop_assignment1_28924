package com.auca.interfaces;

import com.auca.exceptions.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;


public interface Registrable {
    int MIN_NAME_LENGTH = 2;
    int MAX_NAME_LENGTH = 60;
    int MAX_EMAIL_LENGTH = 100;
    int MIN_PHONE_LENGTH = 10;
    int MAX_PHONE_LENGTH = 15;
   
    String DATE_PATTERN = "uuuu-MM-dd";

    void validate() throws ValidationException;

    void printDetails();

    String getRegistrationSummary();

    static String text(String value, String field, int min, int max) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " is required.");
        }
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new ValidationException(field + " must be " + min + "-" + max + " characters.");
        }
        return trimmed;
    }

    /** IDs and codes: 4-30 characters without spaces. */
    static String id(String value, String field) throws ValidationException {
        String id = text(value, field, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH);
        if (id.contains(" ")) {
            throw new ValidationException(field + " must not contain spaces.");
        }
        return id;
    }

    /** Names: 2-60 characters containing at least one letter. */
    static String name(String value, String field) throws ValidationException {
        String name = text(value, field, MIN_NAME_LENGTH, MAX_NAME_LENGTH);
        if (name.chars().noneMatch(Character::isLetter)) {
            throw new ValidationException(field + " must contain letters.");
        }
        return name;
    }

    static String email(String value, String field) throws ValidationException {
        String email = text(value, field, 6, MAX_EMAIL_LENGTH);
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new ValidationException(field + " is not a valid email (example: name@auca.ac.rw).");
        }
        return email;
    }

    /** 10-15 digits with an optional leading +. */
    static String phone(String value, String field) throws ValidationException {
        String phone = text(value, field, MIN_PHONE_LENGTH, MAX_PHONE_LENGTH + 1);
        String digits = phone.startsWith("+") ? phone.substring(1) : phone;
        if (!digits.matches("\\d{" + MIN_PHONE_LENGTH + "," + MAX_PHONE_LENGTH + "}")) {
            throw new ValidationException(field + " must contain 10-15 digits with an optional leading +.");
        }
        return phone;
    }

   
    static String isbn(String value, String field) throws ValidationException {
        if (value == null || !value.trim().replace("-", "").matches("\\d{9}[\\dX]|\\d{13}")) {
            throw new ValidationException(field + " must have 10 or 13 digits (hyphens allowed).");
        }
        return value.trim();
    }

    static LocalDate parseDate(String raw) {
        return LocalDate.parse(raw.trim(), DateTimeFormatter.ofPattern(DATE_PATTERN).withResolverStyle(ResolverStyle.STRICT));
    }

    static LocalDate past(LocalDate date, String field) throws ValidationException {
        required(date, field);
        if (!date.isBefore(LocalDate.now())) {
            throw new ValidationException(field + " must be in the past.");
        }
        return date;
    }

    static LocalDate notFuture(LocalDate date, String field) throws ValidationException {
        required(date, field);
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(field + " must not be in the future.");
        }
        return date;
    }

    static int range(int value, int min, int max, String field) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(field + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    static BigDecimal positive(BigDecimal value, String field) throws ValidationException {
        required(value, field);
        if (value.signum() <= 0) {
            throw new ValidationException(field + " must be greater than zero.");
        }
        return value;
    }

    static String oneOf(String value, List<String> allowed, String field) throws ValidationException {
        if (value == null || !allowed.contains(value)) {
            throw new ValidationException(field + " must be one of " + allowed + ".");
        }
        return value;
    }

    static <X> X required(X value, String field) throws ValidationException {
        if (value == null) {
            throw new ValidationException(field + " is required.");
        }
        return value;
    }
}
