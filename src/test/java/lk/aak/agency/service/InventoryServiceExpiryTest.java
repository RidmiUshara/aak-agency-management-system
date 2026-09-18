package lk.aak.agency.service;

import lk.aak.agency.model.Product;
import lk.aak.agency.model.PurchaseInvoiceItem;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.repository.PurchaseInvoiceItemRepository;
import lk.aak.agency.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceExpiryTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private PurchaseInvoiceItemRepository purchaseInvoiceItemRepository;

    private InventoryService newService() {
        return new InventoryService(productRepository, stockMovementRepository, purchaseInvoiceItemRepository);
    }

    private Product product(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setProductName("Test Product " + id);
        product.setStatus("ACTIVE");
        product.setUnit("PKT");
        return product;
    }

    @Test
    void getExpiringSoonProducts_flagsInStockProductWithNearExpiryBatch() {

        InventoryService service = newService();

        Product nearExpiry = product(1L);
        Product noBatchInfo = product(2L);
        Product farExpiry = product(3L);

        when(productRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(nearExpiry, noBatchInfo, farExpiry));

        when(stockMovementRepository.calculateCurrentStock(1L)).thenReturn(new BigDecimal("10"));
        when(stockMovementRepository.calculateCurrentStock(2L)).thenReturn(new BigDecimal("10"));
        when(stockMovementRepository.calculateCurrentStock(3L)).thenReturn(new BigDecimal("10"));

        PurchaseInvoiceItem nearBatch = new PurchaseInvoiceItem();
        nearBatch.setExpiryDate(LocalDate.now().plusDays(5));

        PurchaseInvoiceItem farBatch = new PurchaseInvoiceItem();
        farBatch.setExpiryDate(LocalDate.now().plusDays(90));

        when(purchaseInvoiceItemRepository.findByProductIdAndExpiryDateIsNotNullOrderByExpiryDateAsc(1L))
                .thenReturn(List.of(nearBatch));
        when(purchaseInvoiceItemRepository.findByProductIdAndExpiryDateIsNotNullOrderByExpiryDateAsc(2L))
                .thenReturn(List.of());
        when(purchaseInvoiceItemRepository.findByProductIdAndExpiryDateIsNotNullOrderByExpiryDateAsc(3L))
                .thenReturn(List.of(farBatch));

        List<InventoryService.ExpiringProductRow> result = service.getExpiringSoonProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).product().getId()).isEqualTo(1L);
        assertThat(result.get(0).isAlreadyExpired()).isFalse();
    }
}
