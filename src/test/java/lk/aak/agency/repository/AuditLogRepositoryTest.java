package lk.aak.agency.repository;

import lk.aak.agency.model.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    private AuditLog saveLog(String username, String action, String entityType, String details) {

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(1L);
        log.setDetails(details);

        return auditLogRepository.save(log);
    }

    @Test
    void search_matchesByUsernameOrActionOrDetails_caseInsensitive() {

        saveLog("admin", "CUSTOMER_DELETED", "Customer", "Deleted customer \"Galle Mart\"");
        saveLog("office1", "PRODUCT_DELETED", "Product", "Deleted product \"Soap\"");

        Page<AuditLog> byUsername = auditLogRepository.search("ADMIN", PageRequest.of(0, 10));
        Page<AuditLog> byAction = auditLogRepository.search("product_deleted", PageRequest.of(0, 10));
        Page<AuditLog> byDetails = auditLogRepository.search("galle", PageRequest.of(0, 10));

        assertThat(byUsername.getContent()).extracting(AuditLog::getUsername).containsExactly("admin");
        assertThat(byAction.getContent()).extracting(AuditLog::getAction).containsExactly("PRODUCT_DELETED");
        assertThat(byDetails.getContent()).hasSize(1);
    }

    @Test
    void search_withBlankKeyword_returnsEverythingPaged() {

        saveLog("admin", "CUSTOMER_DELETED", "Customer", "one");
        saveLog("admin", "PRODUCT_DELETED", "Product", "two");

        Page<AuditLog> result = auditLogRepository.search("", PageRequest.of(0, 1));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(1);
    }
}
