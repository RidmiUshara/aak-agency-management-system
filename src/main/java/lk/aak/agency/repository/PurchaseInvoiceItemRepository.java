package lk.aak.agency.repository;

import lk.aak.agency.model.PurchaseInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseInvoiceItemRepository
        extends JpaRepository<PurchaseInvoiceItem, Long> {

    List<PurchaseInvoiceItem>
    findByPurchaseInvoiceIdOrderByIdAsc(Long purchaseInvoiceId);

    List<PurchaseInvoiceItem>
    findByProductIdAndExpiryDateIsNotNullOrderByExpiryDateAsc(Long productId);

    void deleteByPurchaseInvoiceId(Long purchaseInvoiceId);
}