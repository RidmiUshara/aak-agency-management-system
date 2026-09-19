package lk.aak.agency.service;

import lk.aak.agency.model.Customer;
import lk.aak.agency.model.Product;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.model.SalesInvoiceItem;
import lk.aak.agency.repository.PaymentRepository;
import lk.aak.agency.repository.SalesInvoiceItemRepository;
import lk.aak.agency.repository.SalesInvoiceRepository;
import lk.aak.agency.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesInvoiceServiceTest {

    @Mock
    private SalesInvoiceRepository salesInvoiceRepository;
    @Mock
    private SalesInvoiceItemRepository salesInvoiceItemRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AuditLogService auditLogService;

    private SalesInvoiceService newService() {
        return new SalesInvoiceService(
                salesInvoiceRepository, salesInvoiceItemRepository,
                stockMovementRepository, paymentRepository, auditLogService
        );
    }

    private Customer creditCustomer(BigDecimal creditLimit) {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerName("Test Shop");
        customer.setCreditLimit(creditLimit);
        return customer;
    }

    private Product product() {
        Product product = new Product();
        product.setId(10L);
        product.setProductName("Test Product");
        product.setUnit("PKT");
        return product;
    }

    @Test
    void completeInvoice_blocksCreditSaleThatWouldExceedCustomerCreditLimit() {

        SalesInvoiceService service = newService();

        Customer customer = creditCustomer(new BigDecimal("1000"));

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(5L);
        invoice.setCustomer(customer);
        invoice.setSaleType("CREDIT");
        invoice.setStatus("DRAFT");
        invoice.setNetAmount(new BigDecimal("1500"));

        SalesInvoiceItem item = new SalesInvoiceItem();
        item.setId(50L);
        item.setSalesInvoice(invoice);
        item.setProduct(product());
        item.setQuantity(new BigDecimal("5"));
        item.setUnitPrice(new BigDecimal("300"));

        when(salesInvoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));
        when(salesInvoiceItemRepository.findBySalesInvoiceIdOrderByIdAsc(5L)).thenReturn(List.of(item));
        when(salesInvoiceRepository.findByCustomerIdOrderByInvoiceDateDesc(1L)).thenReturn(List.of(invoice));

        assertThatThrownBy(() -> service.completeInvoice(5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("credit limit");

        assertThat(invoice.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void completeInvoiceWithCreditOverride_completesDespiteExceedingLimitAndRecordsApprover() {

        SalesInvoiceService service = newService();

        Customer customer = creditCustomer(new BigDecimal("1000"));

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(5L);
        invoice.setCustomer(customer);
        invoice.setSaleType("CREDIT");
        invoice.setStatus("DRAFT");
        invoice.setNetAmount(new BigDecimal("1500"));

        SalesInvoiceItem item = new SalesInvoiceItem();
        item.setId(50L);
        item.setSalesInvoice(invoice);
        item.setProduct(product());
        item.setQuantity(new BigDecimal("5"));
        item.setUnitPrice(new BigDecimal("300"));

        when(salesInvoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));
        when(salesInvoiceItemRepository.findBySalesInvoiceIdOrderByIdAsc(5L)).thenReturn(List.of(item));
        when(stockMovementRepository.calculateCurrentStock(10L)).thenReturn(new BigDecimal("100"));

        service.completeInvoiceWithCreditOverride(5L, "owner1", "Trusted long-term customer");

        assertThat(invoice.getStatus()).isEqualTo("COMPLETED");
        assertThat(invoice.getCreditOverrideApprovedBy()).isEqualTo("owner1");
        assertThat(invoice.getCreditOverrideReason()).isEqualTo("Trusted long-term customer");
        assertThat(invoice.getCreditOverrideAt()).isNotNull();
    }

    @Test
    void completeInvoiceWithCreditOverride_requiresANonBlankReason() {

        SalesInvoiceService service = newService();

        assertThatThrownBy(() -> service.completeInvoiceWithCreditOverride(5L, "owner1", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");
    }

    @Test
    void completeInvoice_allowsCashSaleRegardlessOfCreditLimit() {

        SalesInvoiceService service = newService();

        Customer customer = creditCustomer(new BigDecimal("1000"));

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(6L);
        invoice.setCustomer(customer);
        invoice.setSaleType("CASH");
        invoice.setStatus("DRAFT");
        invoice.setNetAmount(new BigDecimal("5000"));

        SalesInvoiceItem item = new SalesInvoiceItem();
        item.setId(60L);
        item.setSalesInvoice(invoice);
        item.setProduct(product());
        item.setQuantity(new BigDecimal("5"));
        item.setUnitPrice(new BigDecimal("1000"));

        when(salesInvoiceRepository.findById(6L)).thenReturn(Optional.of(invoice));
        when(salesInvoiceItemRepository.findBySalesInvoiceIdOrderByIdAsc(6L)).thenReturn(List.of(item));
        when(stockMovementRepository.calculateCurrentStock(10L)).thenReturn(new BigDecimal("100"));

        service.completeInvoice(6L);

        assertThat(invoice.getStatus()).isEqualTo("COMPLETED");
        assertThat(invoice.getPaymentStatus()).isEqualTo("PAID");
    }
}
