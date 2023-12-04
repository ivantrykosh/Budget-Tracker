package com.ivantrykosh.app.budgettracker.server.util;

import java.security.SecureRandom;
import java.util.stream.Collectors;

/**
 * Password Generator class for generate random password
 */
public class PasswordGenerator {
    // Chars in password
    private final String ALL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*#?&";

    /**
     * Generate random password
     * @param length length of password
     * @return Generated password
     */
    public String generatePassword(int length) {
        return new SecureRandom()
                .ints(length, 0, ALL_CHARS.length())
                .mapToObj(ALL_CHARS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
