package lk.aak.agency.repository;

import lk.aak.agency.model.SalesInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalesInvoiceRepository
        extends JpaRepository<SalesInvoice, Long> {

    Optional<SalesInvoice> findByInvoiceNumber(
            String invoiceNumber
    );

    boolean existsByInvoiceNumber(
            String invoiceNumber
    );

    List<SalesInvoice>
    findAllByOrderByInvoiceDateDesc();

    /*
     * Paginated list, optionally filtered by invoice number, customer name/area,
     * route code, sale type or status - keeps the list screen usable at scale.
     */
    @Query("""
            SELECT i FROM SalesInvoice i
            LEFT JOIN i.customer c
            WHERE :keyword IS NULL OR :keyword = ''
                OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.area) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.routeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.saleType) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.status) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<SalesInvoice> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /*
     * Returns all sales invoices belonging to one customer.
     */
    List<SalesInvoice>
    findByCustomerIdOrderByInvoiceDateDesc(
            Long customerId
    );

    /*
     * Completed bills not yet assigned to any delivery trip - eligible for loading.
     */
    List<SalesInvoice> findByStatusAndDeliveryTripIdIsNullOrderByInvoiceDateAsc(String status);

    /*
     * Bills currently assigned to a given delivery trip.
     */
    List<SalesInvoice> findByDeliveryTripIdOrderByIdAsc(Long deliveryTripId);
}