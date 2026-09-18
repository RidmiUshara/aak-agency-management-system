package lk.aak.agency.repository;

import lk.aak.agency.model.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

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