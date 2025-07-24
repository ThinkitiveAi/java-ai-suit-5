package com.healthfirst.provider.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.regex.Pattern;

public class PasswordUtils {
    private static final int BCRYPT_STRENGTH = 12;
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    public static String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }

    public static boolean isValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
} 