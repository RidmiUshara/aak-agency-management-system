package lk.aak.agency.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Tracks failed login attempts per username and temporarily locks accounts after too many. */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private static class Attempts {
        final AtomicInteger count = new AtomicInteger(0);
        volatile Instant lockedUntil;
    }

    private final ConcurrentHashMap<String, Attempts> attemptsByUsername = new ConcurrentHashMap<>();

    public void recordFailure(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        Attempts attempts = attemptsByUsername.computeIfAbsent(normalize(username), key -> new Attempts());
        int failures = attempts.count.incrementAndGet();

        if (failures >= MAX_ATTEMPTS) {
            attempts.lockedUntil = Instant.now().plus(LOCK_DURATION);
        }
    }

    public void recordSuccess(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        attemptsByUsername.remove(normalize(username));
    }

    public boolean isLocked(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        Attempts attempts = attemptsByUsername.get(normalize(username));

        if (attempts == null || attempts.lockedUntil == null) {
            return false;
        }

        if (Instant.now().isAfter(attempts.lockedUntil)) {
            // Lock has expired - reset so the user can try again.
            attemptsByUsername.remove(normalize(username));
            return false;
        }

        return true;
    }

    private String normalize(String username) {
        return username.trim().toLowerCase();
    }
}
