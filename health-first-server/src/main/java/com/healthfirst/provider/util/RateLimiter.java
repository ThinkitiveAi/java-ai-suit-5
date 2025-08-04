package com.healthfirst.provider.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MILLIS = 60 * 60 * 1000; // 1 hour

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        Attempt attempt = attempts.getOrDefault(ip, new Attempt(0, Instant.now().toEpochMilli()));
        long now = Instant.now().toEpochMilli();
        if (now - attempt.windowStart > WINDOW_MILLIS) {
            attempt = new Attempt(0, now);
        }
        if (attempt.count >= MAX_ATTEMPTS) {
            return false;
        }
        attempt.count++;
        attempts.put(ip, attempt);
        return true;
    }

    private static class Attempt {
        int count;
        long windowStart;
        Attempt(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
} 