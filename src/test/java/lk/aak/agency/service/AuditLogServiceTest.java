package lk.aak.agency.service;

import lk.aak.agency.model.AuditLog;
import lk.aak.agency.repository.AuditLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void record_capturesCurrentUsernameAndDetails() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null)
        );

        AuditLogService service = new AuditLogService(auditLogRepository);

        service.record("CUSTOMER_DELETED", "Customer", 42L, "Deleted customer \"Test Shop\"");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(saved.getAction()).isEqualTo("CUSTOMER_DELETED");
        assertThat(saved.getEntityType()).isEqualTo("Customer");
        assertThat(saved.getEntityId()).isEqualTo(42L);
        assertThat(saved.getDetails()).isEqualTo("Deleted customer \"Test Shop\"");
    }

    @Test
    void record_withNoAuthentication_fallsBackToSystemUsername() {

        AuditLogService service = new AuditLogService(auditLogRepository);

        service.record("PRODUCT_DELETED", "Product", 1L, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertThat(captor.getValue().getUsername()).isEqualTo("SYSTEM");
    }

    @Test
    void record_neverThrows_evenWhenRepositoryFails() {

        AuditLogService service = new AuditLogService(auditLogRepository);

        doThrow(new RuntimeException("DB unavailable"))
                .when(auditLogRepository).save(any());

        service.record("CUSTOMER_DELETED", "Customer", 1L, "should not throw");
    }
}
