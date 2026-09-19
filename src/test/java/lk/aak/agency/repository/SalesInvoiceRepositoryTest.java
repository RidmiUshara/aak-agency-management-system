package lk.aak.agency.repository;

import lk.aak.agency.model.Customer;
import lk.aak.agency.model.SalesInvoice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SalesInvoiceRepositoryTest {

    @Autowired
    private SalesInvoiceRepository salesInvoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private SalesInvoice saveInvoice(String invoiceNumber, Customer customer) {

        SalesInvoice invoice = new SalesInvoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setSaleType("CREDIT");
        invoice.setStatus("DRAFT");
        invoice.setGrossAmount(BigDecimal.TEN);
        invoice.setNetAmount(BigDecimal.TEN);

        return salesInvoiceRepository.save(invoice);
    }

    private Customer saveCustomer(String name, String area) {

        Customer customer = new Customer();
        customer.setCustomerCode("CUST-" + name);
        customer.setCustomerName(name);
        customer.setArea(area);

        return customerRepository.save(customer);
    }

    @Test
    void search_withBlankKeyword_returnsEverythingPaged() {

        Customer customer = saveCustomer("Colombo Mart", "Colombo");
        saveInvoice("INV-001", customer);
        saveInvoice("INV-002", customer);

        Page<SalesInvoice> result = salesInvoiceRepository.search(
                "", PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "invoiceNumber"))
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_matchesByInvoiceNumber() {

        Customer customer = saveCustomer("Kandy Shop", "Kandy");
        saveInvoice("INV-100", customer);
        saveInvoice("INV-200", customer);

        Page<SalesInvoice> result = salesInvoiceRepository.search(
                "inv-100", PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(SalesInvoice::getInvoiceNumber)
                .containsExactly("INV-100");
    }

    @Test
    void search_matchesByCustomerNameOrArea_caseInsensitive() {

        Customer galle = saveCustomer("Galle Supermarket", "Galle");
        Customer matara = saveCustomer("Matara Traders", "Matara");
        saveInvoice("INV-301", galle);
        saveInvoice("INV-302", matara);

        Page<SalesInvoice> byName = salesInvoiceRepository.search(
                "galle", PageRequest.of(0, 10)
        );
        Page<SalesInvoice> byArea = salesInvoiceRepository.search(
                "MATARA", PageRequest.of(0, 10)
        );

        assertThat(byName.getContent())
                .extracting(SalesInvoice::getInvoiceNumber)
                .containsExactly("INV-301");
        assertThat(byArea.getContent())
                .extracting(SalesInvoice::getInvoiceNumber)
                .containsExactly("INV-302");
    }

    @Test
    void search_withNoMatch_returnsEmptyPage() {

        Customer customer = saveCustomer("Negombo Shop", "Negombo");
        saveInvoice("INV-500", customer);

        Page<SalesInvoice> result = salesInvoiceRepository.search(
                "nonexistent-term", PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
