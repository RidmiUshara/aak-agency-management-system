package lk.aak.agency.repository;

import lk.aak.agency.model.Customer;
import lk.aak.agency.model.Payment;
import lk.aak.agency.model.SalesInvoice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SalesInvoiceRepository salesInvoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Payment savePayment(
            String receiptNumber, SalesInvoice invoice,
            BigDecimal amount, String status) {

        Payment payment = new Payment();
        payment.setReceiptNumber(receiptNumber);
        payment.setSalesInvoice(invoice);
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(amount);
        payment.setPaymentMethod("CASH");
        payment.setStatus(status);

        return paymentRepository.save(payment);
    }

    private SalesInvoice saveInvoice(String invoiceNumber, Customer customer) {

        SalesInvoice invoice = new SalesInvoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setSaleType("CASH");
        invoice.setStatus("COMPLETED");

        return salesInvoiceRepository.save(invoice);
    }

    private Customer saveCustomer(String name, String area) {

        Customer customer = new Customer();
        customer.setCustomerCode("PAY-" + name);
        customer.setCustomerName(name);
        customer.setArea(area);

        return customerRepository.save(customer);
    }

    @Test
    void search_matchesByReceiptOrCustomerName() {

        Customer customer = saveCustomer("Kurunegala Shop", "Kurunegala");
        SalesInvoice invoice = saveInvoice("INV-PAY-1", customer);
        savePayment("REC-001", invoice, BigDecimal.valueOf(500), "RECEIVED");
        savePayment("REC-002", invoice, BigDecimal.valueOf(300), "PENDING");

        Page<Payment> byReceipt = paymentRepository.search("rec-001", PageRequest.of(0, 10));
        Page<Payment> byCustomer = paymentRepository.search("kurunegala", PageRequest.of(0, 10));

        assertThat(byReceipt.getContent())
                .extracting(Payment::getReceiptNumber)
                .containsExactly("REC-001");
        assertThat(byCustomer.getTotalElements()).isEqualTo(2);
    }

    @Test
    void sumReceivedAmount_onlyCountsReceivedStatus() {

        Customer customer = saveCustomer("Jaffna Traders", "Jaffna");
        SalesInvoice invoice = saveInvoice("INV-PAY-2", customer);
        savePayment("REC-100", invoice, BigDecimal.valueOf(1000), "RECEIVED");
        savePayment("REC-101", invoice, BigDecimal.valueOf(2000), "PENDING");

        BigDecimal total = paymentRepository.sumReceivedAmount("");
        long receivedCount = paymentRepository.countReceived("");

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(receivedCount).isEqualTo(1);
    }
}
