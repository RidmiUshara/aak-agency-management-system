package lk.aak.agency.repository;

import lk.aak.agency.model.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseInvoiceRepository
        extends JpaRepository<PurchaseInvoice, Long> {

    Optional<PurchaseInvoice> findByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumber(String documentNumber);

    List<PurchaseInvoice> findAllByOrderByInvoiceDateDesc();
}