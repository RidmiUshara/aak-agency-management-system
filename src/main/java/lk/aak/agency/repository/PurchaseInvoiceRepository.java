package lk.aak.agency.repository;

import lk.aak.agency.model.PurchaseInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseInvoiceRepository
        extends JpaRepository<PurchaseInvoice, Long> {

    Optional<PurchaseInvoice> findByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumber(String documentNumber);

    List<PurchaseInvoice> findAllByOrderByInvoiceDateDesc();

    /*
     * Paginated list, optionally filtered by document number, supplier, tax
     * invoice/PO number, territory or status - keeps the list screen usable at scale.
     */
    @Query("""
            SELECT i FROM PurchaseInvoice i
            WHERE :keyword IS NULL OR :keyword = ''
                OR LOWER(i.documentNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.taxInvoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.poNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.territory) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.status) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<PurchaseInvoice> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}