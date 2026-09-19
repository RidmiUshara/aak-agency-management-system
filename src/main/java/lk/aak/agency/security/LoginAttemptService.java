package lk.aak.agency.security;

import lk.aak.agency.model.SystemUser;
import lk.aak.agency.repository.SystemUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Tracks failed login attempts per username and temporarily locks accounts after too many.
 * Persisted on the SystemUser row itself, so a lock survives app restarts and works correctly
 * even if there is more than one app instance.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final SystemUserRepository systemUserRepository;

    public LoginAttemptService(SystemUserRepository systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    @Transactional
    public void recordFailure(String username) {

        findUser(username).ifPresent(user -> {

            int failures = user.getFailedAttemptCount() + 1;
            user.setFailedAttemptCount(failures);

            if (failures >= MAX_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plus(LOCK_DURATION));
            }

            systemUserRepository.save(user);
        });
    }

    @Transactional
    public void recordSuccess(String username) {

        findUser(username).ifPresent(user -> {

            user.setFailedAttemptCount(0);
            user.setLockedUntil(null);

            systemUserRepository.save(user);
        });
    }

    @Transactional
    public boolean isLocked(String username) {

        return findUser(username)
                .map(user -> {

                    LocalDateTime lockedUntil = user.getLockedUntil();

                    if (lockedUntil == null) {
                        return false;
                    }

                    if (LocalDateTime.now().isAfter(lockedUntil)) {
                        // Lock has expired - reset so the user can try again.
                        user.setFailedAttemptCount(0);
                        user.setLockedUntil(null);
                        systemUserRepository.save(user);
                        return false;
                    }

                    return true;
                })
                .orElse(false);
    }

    private Optional<SystemUser> findUser(String username) {

        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        return systemUserRepository.findByUsername(username.trim());
    }
}
