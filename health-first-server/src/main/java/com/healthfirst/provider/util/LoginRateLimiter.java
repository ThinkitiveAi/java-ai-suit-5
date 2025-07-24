package com.healthfirst.provider.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MILLIS = 15 * 60 * 1000; // 15 min
    private static final long LOCK_MILLIS = 30 * 60 * 1000; // 30 min

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final Map<String, Long> locked = new ConcurrentHashMap<>();

    public boolean isLocked(String key) {
        Long lockUntil = locked.get(key);
        if (lockUntil == null) return false;
        if (Instant.now().toEpochMilli() > lockUntil) {
            locked.remove(key);
            return false;
        }
        return true;
    }

    public boolean isAllowed(String key) {
        if (isLocked(key)) return false;
        Attempt attempt = attempts.getOrDefault(key, new Attempt(0, Instant.now().toEpochMilli()));
        long now = Instant.now().toEpochMilli();
        if (now - attempt.windowStart > WINDOW_MILLIS) {
            attempt = new Attempt(0, now);
        }
        if (attempt.count >= MAX_ATTEMPTS) {
            locked.put(key, now + LOCK_MILLIS);
            attempts.remove(key);
            return false;
        }
        attempt.count++;
        attempts.put(key, attempt);
        return true;
    }

    public void reset(String key) {
        attempts.remove(key);
        locked.remove(key);
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