package lk.aak.agency.repository;

import lk.aak.agency.model.SupplierPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {

    List<SupplierPayment> findByPurchaseInvoiceIdOrderByPaymentDateDesc(Long purchaseInvoiceId);

    @Query("""
            SELECT COALESCE(SUM(sp.amount), 0)
            FROM SupplierPayment sp
            WHERE sp.purchaseInvoiceId = :purchaseInvoiceId
            """)
    BigDecimal calculatePaidAmount(@Param("purchaseInvoiceId") Long purchaseInvoiceId);
}
