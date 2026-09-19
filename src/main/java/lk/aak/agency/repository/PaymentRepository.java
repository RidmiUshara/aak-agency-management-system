package lk.aak.agency.repository;

import lk.aak.agency.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /*
     * Paginated list, optionally filtered by receipt number, invoice number,
     * customer name/area, payment method or reference number.
     */
    @Query("""
            SELECT p FROM Payment p
            LEFT JOIN p.salesInvoice i
            LEFT JOIN i.customer c
            WHERE :keyword IS NULL OR :keyword = ''
                OR LOWER(p.receiptNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.area) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.paymentMethod) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.referenceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Payment> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /*
     * Sum of RECEIVED payments matching the same search filter, used for the
     * list screen's "Total Collected" summary card (independent of pagination).
     */
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            LEFT JOIN p.salesInvoice i
            LEFT JOIN i.customer c
            WHERE UPPER(p.status) = 'RECEIVED'
                AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(p.receiptNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.area) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.paymentMethod) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.referenceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            """)
    BigDecimal sumReceivedAmount(@Param("keyword") String keyword);

    /*
     * Count of RECEIVED payments matching the same search filter.
     */
    @Query("""
            SELECT COUNT(p) FROM Payment p
            LEFT JOIN p.salesInvoice i
            LEFT JOIN i.customer c
            WHERE UPPER(p.status) = 'RECEIVED'
                AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(p.receiptNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(c.area) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.paymentMethod) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.referenceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
            """)
    long countReceived(@Param("keyword") String keyword);

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
