package lk.aak.agency.service;

import lk.aak.agency.model.AuditLog;
import lk.aak.agency.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /** Records who did what to which record - failures here must never block the action itself. */
    public void record(String action, String entityType, Long entityId, String details) {

        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUsername(currentUsername());
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setDetails(details);

            auditLogRepository.save(auditLog);

        } catch (RuntimeException exception) {
            // Audit logging is best-effort - a failure to record history must not fail the
            // underlying business operation (e.g. a delete or approval) that triggered it.
        }
    }

    private String currentUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication == null ? "SYSTEM" : authentication.getName();
    }

    public Page<AuditLog> getLogPage(String search, int page, int size) {

        return auditLogRepository.search(
                search == null ? "" : search.trim(),
                PageRequest.of(
                        Math.max(page, 0),
                        Math.max(size, 1),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
    }
}
