package lk.aak.agency.repository;

import lk.aak.agency.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /*
     * Paginated list, optionally filtered by username, action, entity type or details.
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE :keyword IS NULL OR :keyword = ''
                OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(a.action) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(a.entityType) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(a.details) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<AuditLog> search(@Param("keyword") String keyword, Pageable pageable);
}
