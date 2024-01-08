package com.ivantrykosh.app.budgettracker.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * Password Generator class for generate random password
 */
public class PasswordManager {
    // Chars in password
    private final String ALL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*#?&";
    Logger logger = LoggerFactory.getLogger(PasswordManager.class); // Logger

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
                .collect(Collectors.joining()) + "Aa0@";
    }

    /**
     * Hash password with salt
     * @param password password to hash
     * @param salt salt, that you with password
     * @return Hashed password
     */
    public String hashPassword(String password, String salt) {
        String hashedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] passwordWithSalt = (password + salt).getBytes();
            hashedPassword = Base64.getEncoder().encodeToString(md.digest(passwordWithSalt));
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error: " + e.getMessage());
        }
        return hashedPassword;
    }
}
