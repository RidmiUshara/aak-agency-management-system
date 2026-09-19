package lk.aak.agency.security;

import lk.aak.agency.model.SystemUser;
import lk.aak.agency.repository.SystemUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private SystemUserRepository systemUserRepository;

    private SystemUser userWithAttempts(int failedAttemptCount, LocalDateTime lockedUntil) {

        SystemUser user = new SystemUser();
        user.setUsername("sales1");
        user.setFailedAttemptCount(failedAttemptCount);
        user.setLockedUntil(lockedUntil);

        return user;
    }

    @Test
    void recordFailure_locksAccountAfterFifthFailure() {

        LoginAttemptService service = new LoginAttemptService(systemUserRepository);
        SystemUser user = userWithAttempts(4, null);

        when(systemUserRepository.findByUsername("sales1")).thenReturn(Optional.of(user));

        service.recordFailure("sales1");

        assertThat(user.getFailedAttemptCount()).isEqualTo(5);
        assertThat(user.getLockedUntil()).isNotNull();
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void recordFailure_beforeFifthFailure_doesNotLock() {

        LoginAttemptService service = new LoginAttemptService(systemUserRepository);
        SystemUser user = userWithAttempts(1, null);

        when(systemUserRepository.findByUsername("sales1")).thenReturn(Optional.of(user));

        service.recordFailure("sales1");

        assertThat(user.getFailedAttemptCount()).isEqualTo(2);
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void isLocked_returnsTrue_whileLockIsStillActive() {

        LoginAttemptService service = new LoginAttemptService(systemUserRepository);
        SystemUser user = userWithAttempts(5, LocalDateTime.now().plusMinutes(10));

        when(systemUserRepository.findByUsername("sales1")).thenReturn(Optional.of(user));

        assertThat(service.isLocked("sales1")).isTrue();
    }

    @Test
    void isLocked_resetsAndReturnsFalse_onceLockHasExpired() {

        LoginAttemptService service = new LoginAttemptService(systemUserRepository);
        SystemUser user = userWithAttempts(5, LocalDateTime.now().minusMinutes(1));

        when(systemUserRepository.findByUsername("sales1")).thenReturn(Optional.of(user));

        assertThat(service.isLocked("sales1")).isFalse();
        assertThat(user.getFailedAttemptCount()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void recordSuccess_clearsFailedAttemptsAndLock() {

        LoginAttemptService service = new LoginAttemptService(systemUserRepository);
        SystemUser user = userWithAttempts(5, LocalDateTime.now().plusMinutes(10));

        when(systemUserRepository.findByUsername("sales1")).thenReturn(Optional.of(user));

        service.recordSuccess("sales1");

        assertThat(user.getFailedAttemptCount()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void unknownUsername_isNeverLocked_andDoesNotThrow() {

        LoginAttemptService service = new LoginAttemptService(systemUserRepository);

        when(systemUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        service.recordFailure("ghost");

        assertThat(service.isLocked("ghost")).isFalse();
    }
}
