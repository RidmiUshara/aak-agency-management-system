package lk.aak.agency.repository;

import lk.aak.agency.model.PurchaseInvoice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PurchaseInvoiceRepositoryTest {

    @Autowired
    private PurchaseInvoiceRepository purchaseInvoiceRepository;

    private PurchaseInvoice saveInvoice(
            String documentNumber, String supplierName, String territory) {

        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setDocumentNumber(documentNumber);
        invoice.setSupplierName(supplierName);
        invoice.setTerritory(territory);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setPlaceOfSupply("Colombo");
        invoice.setStatus("DRAFT");

        return purchaseInvoiceRepository.save(invoice);
    }

    @Test
    void search_withBlankKeyword_returnsEverythingPaged() {

        saveInvoice("PO-001", "CBL", "Colombo");
        saveInvoice("PO-002", "CBL", "Kandy");

        Page<PurchaseInvoice> result = purchaseInvoiceRepository.search(
                "", PageRequest.of(0, 1)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_matchesByDocumentNumberOrTerritory_caseInsensitive() {

        saveInvoice("PO-100", "CBL", "Galle");
        saveInvoice("PO-200", "CBL", "Matara");

        Page<PurchaseInvoice> byDocument = purchaseInvoiceRepository.search(
                "po-100", PageRequest.of(0, 10)
        );
        Page<PurchaseInvoice> byTerritory = purchaseInvoiceRepository.search(
                "MATARA", PageRequest.of(0, 10)
        );

        assertThat(byDocument.getContent())
                .extracting(PurchaseInvoice::getDocumentNumber)
                .containsExactly("PO-100");
        assertThat(byTerritory.getContent())
                .extracting(PurchaseInvoice::getDocumentNumber)
                .containsExactly("PO-200");
    }

    @Test
    void search_withNoMatch_returnsEmptyPage() {

        saveInvoice("PO-500", "CBL", "Negombo");

        Page<PurchaseInvoice> result = purchaseInvoiceRepository.search(
                "nonexistent-term", PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }
}
