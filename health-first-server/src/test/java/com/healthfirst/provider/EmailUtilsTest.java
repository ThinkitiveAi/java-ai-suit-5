package com.healthfirst.provider;

import com.healthfirst.provider.util.EmailUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

public class EmailUtilsTest {
    @Test
    void testTokenGenerationUniquenessAndLength() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String token = EmailUtils.generateVerificationToken();
            assertNotNull(token);
            assertTrue(token.length() >= 43); // 32 bytes base64url
            assertTrue(tokens.add(token)); // unique
        }
    }
} 