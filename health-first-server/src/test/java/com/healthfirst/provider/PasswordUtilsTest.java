package com.healthfirst.provider;

import com.healthfirst.provider.util.PasswordUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilsTest {
    @Test
    void testPasswordHashingAndMatching() {
        String password = "SecurePassword123!";
        String hash = PasswordUtils.hashPassword(password);
        assertTrue(PasswordUtils.matches(password, hash));
        assertFalse(PasswordUtils.matches("WrongPassword", hash));
    }

    @Test
    void testPasswordValidation() {
        assertTrue(PasswordUtils.isValid("Abcdefg1!")); // 9 chars, valid
        assertTrue(PasswordUtils.isValid("StrongPassw0rd!@#"));
        assertFalse(PasswordUtils.isValid("short1!"));
        assertFalse(PasswordUtils.isValid("nouppercase1!"));
        assertFalse(PasswordUtils.isValid("NOLOWERCASE1!"));
        assertFalse(PasswordUtils.isValid("NoSpecialChar1"));
        assertFalse(PasswordUtils.isValid("NoDigit!"));
    }
} 