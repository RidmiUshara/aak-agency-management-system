package lk.aak.agency.service;

import lk.aak.agency.model.Employee;
import lk.aak.agency.model.Payment;
import lk.aak.agency.repository.EmployeeRepository;
import lk.aak.agency.repository.PaymentRepository;
import lk.aak.agency.repository.SalesInvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceHandoverTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private SalesInvoiceRepository salesInvoiceRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    private PaymentService newService() {
        return new PaymentService(paymentRepository, salesInvoiceRepository, employeeRepository);
    }

    @Test
    void getHandoverSummaryForDate_groupsCashAndChequeTotalsByCollector() {

        PaymentService service = newService();

        LocalDate date = LocalDate.of(2026, 9, 18);

        Payment cashPayment = new Payment();
        cashPayment.setCollectedByEmployeeId(1L);
        cashPayment.setCollectedByName("Nimal");
        cashPayment.setPaymentMethod("CASH");
        cashPayment.setAmount(new BigDecimal("500"));
        cashPayment.setHandoverStatus("PENDING");

        Payment chequePayment = new Payment();
        chequePayment.setCollectedByEmployeeId(1L);
        chequePayment.setCollectedByName("Nimal");
        chequePayment.setPaymentMethod("CHEQUE");
        chequePayment.setAmount(new BigDecimal("1000"));
        chequePayment.setHandoverStatus("PENDING");

        Payment officePayment = new Payment();
        officePayment.setCollectedByEmployeeId(null);
        officePayment.setPaymentMethod("CASH");
        officePayment.setAmount(new BigDecimal("300"));

        when(paymentRepository.findByPaymentDateOrderByPaymentDateDesc(date))
                .thenReturn(List.of(cashPayment, chequePayment, officePayment));

        List<PaymentService.HandoverSummaryRow> summary = service.getHandoverSummaryForDate(date);

        assertThat(summary).hasSize(1);
        assertThat(summary.get(0).getCollectorName()).isEqualTo("Nimal");
        assertThat(summary.get(0).getCashTotal()).isEqualByComparingTo("500");
        assertThat(summary.get(0).getChequeTotal()).isEqualByComparingTo("1000");
        assertThat(summary.get(0).isAllHandedOver()).isFalse();
    }

    @Test
    void markHandedOver_updatesStatusForAllMatchingPayments() {

        PaymentService service = newService();

        LocalDate date = LocalDate.of(2026, 9, 18);

        Payment payment1 = new Payment();
        payment1.setHandoverStatus("PENDING");
        Payment payment2 = new Payment();
        payment2.setHandoverStatus("PENDING");

        when(paymentRepository.findByCollectedByEmployeeIdAndPaymentDate(1L, date))
                .thenReturn(List.of(payment1, payment2));

        service.markHandedOver(1L, date);

        assertThat(payment1.getHandoverStatus()).isEqualTo("HANDED_OVER");
        assertThat(payment2.getHandoverStatus()).isEqualTo("HANDED_OVER");
    }
}
