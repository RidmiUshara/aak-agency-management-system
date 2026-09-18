package lk.aak.agency.service;

import lk.aak.agency.model.PurchaseInvoice;
import lk.aak.agency.model.SupplierPayment;
import lk.aak.agency.repository.PurchaseInvoiceRepository;
import lk.aak.agency.repository.SupplierPaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierPaymentServiceTest {

    @Mock
    private SupplierPaymentRepository supplierPaymentRepository;
    @Mock
    private PurchaseInvoiceRepository purchaseInvoiceRepository;

    private SupplierPaymentService newService() {
        return new SupplierPaymentService(supplierPaymentRepository, purchaseInvoiceRepository);
    }

    private PurchaseInvoice completedInvoice(Long id, BigDecimal totalAmount) {
        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setId(id);
        invoice.setDocumentNumber("CBL-" + id);
        invoice.setStatus("COMPLETED");
        invoice.setTotalAmount(totalAmount);
        return invoice;
    }

    @Test
    void getSupplierBalanceSummary_onlyIncludesCompletedInvoicesWithOutstandingBalance() {

        SupplierPaymentService service = newService();

        PurchaseInvoice owed = completedInvoice(1L, new BigDecimal("1000"));
        PurchaseInvoice fullyPaid = completedInvoice(2L, new BigDecimal("500"));

        PurchaseInvoice draft = new PurchaseInvoice();
        draft.setId(3L);
        draft.setStatus("DRAFT");
        draft.setTotalAmount(new BigDecimal("999"));

        when(purchaseInvoiceRepository.findAllByOrderByInvoiceDateDesc())
                .thenReturn(List.of(owed, fullyPaid, draft));

        when(supplierPaymentRepository.calculatePaidAmount(1L)).thenReturn(new BigDecimal("400"));
        when(supplierPaymentRepository.calculatePaidAmount(2L)).thenReturn(new BigDecimal("500"));

        SupplierPaymentService.SupplierBalanceSummary summary = service.getSupplierBalanceSummary();

        assertThat(summary.rows()).hasSize(1);
        assertThat(summary.rows().get(0).invoice().getId()).isEqualTo(1L);
        assertThat(summary.rows().get(0).outstandingBalance()).isEqualByComparingTo("600");
        assertThat(summary.totalOwed()).isEqualByComparingTo("600");
    }

    @Test
    void recordPayment_rejectsAmountGreaterThanOutstandingBalance() {

        SupplierPaymentService service = newService();

        PurchaseInvoice invoice = completedInvoice(1L, new BigDecimal("1000"));

        when(purchaseInvoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(supplierPaymentRepository.calculatePaidAmount(1L)).thenReturn(BigDecimal.ZERO);

        SupplierPayment payment = new SupplierPayment();
        payment.setPurchaseInvoiceId(1L);
        payment.setAmount(new BigDecimal("1500"));
        payment.setPaymentDate(LocalDate.now());

        assertThatThrownBy(() -> service.recordPayment(payment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outstanding balance");
    }
}
