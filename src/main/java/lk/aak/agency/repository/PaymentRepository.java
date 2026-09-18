package lk.aak.agency.repository;

import lk.aak.agency.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReceiptNumber(String receiptNumber);

    boolean existsByReceiptNumber(String receiptNumber);

    List<Payment> findAllByOrderByPaymentDateDesc();

    List<Payment> findBySalesInvoiceIdOrderByPaymentDateDesc(
            Long invoiceId
    );

    List<Payment> findBySalesInvoiceCustomerIdOrderByPaymentDateDesc(
            Long customerId
    );

    List<Payment> findByPaymentDateBetweenOrderByPaymentDateDesc(
            LocalDate fromDate,
            LocalDate toDate
    );

    List<Payment> findByPaymentDateOrderByPaymentDateDesc(
            LocalDate paymentDate
    );

    List<Payment> findByCollectedByEmployeeIdAndPaymentDate(
            Long collectedByEmployeeId,
            LocalDate paymentDate
    );

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.salesInvoice.id = :invoiceId
            AND UPPER(p.status) = 'RECEIVED'
            """)
    BigDecimal calculatePaidAmount(
            @Param("invoiceId") Long invoiceId
    );

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.paymentDate BETWEEN :fromDate AND :toDate
            AND UPPER(p.status) = 'RECEIVED'
            """)
    BigDecimal calculateReceivedAmountBetweenDates(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
