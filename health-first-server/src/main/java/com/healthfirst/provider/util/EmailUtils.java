package com.healthfirst.provider.util;

import java.security.SecureRandom;
import java.util.Base64;

public class EmailUtils {
    private static final SecureRandom random = new SecureRandom();

    public static String generateVerificationToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
} 