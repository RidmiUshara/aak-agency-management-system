package lk.aak.agency.service;

import lk.aak.agency.model.Product;
import lk.aak.agency.model.StockMovement;
import lk.aak.agency.model.SupplierReturn;
import lk.aak.agency.model.SupplierReturnItem;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.repository.PurchaseInvoiceRepository;
import lk.aak.agency.repository.StockMovementRepository;
import lk.aak.agency.repository.SupplierReturnItemRepository;
import lk.aak.agency.repository.SupplierReturnRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierReturnServiceTest {

    @Mock
    private SupplierReturnRepository supplierReturnRepository;
    @Mock
    private SupplierReturnItemRepository supplierReturnItemRepository;
    @Mock
    private PurchaseInvoiceRepository purchaseInvoiceRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private InventoryService inventoryService;

    private SupplierReturnService newService() {
        return new SupplierReturnService(
                supplierReturnRepository, supplierReturnItemRepository, purchaseInvoiceRepository,
                productRepository, stockMovementRepository, inventoryService
        );
    }

    @Test
    void addItem_removesStockWithANegativeStockMovement() {

        SupplierReturnService service = newService();

        SupplierReturn supplierReturn = new SupplierReturn();
        supplierReturn.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setUnit("PKT");

        when(supplierReturnRepository.findById(1L)).thenReturn(Optional.of(supplierReturn));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryService.getCurrentStock(10L)).thenReturn(new BigDecimal("50"));
        when(supplierReturnItemRepository.save(any(SupplierReturnItem.class)))
                .thenAnswer(invocation -> {
                    SupplierReturnItem item = invocation.getArgument(0);
                    item.setId(100L);
                    return item;
                });

        service.addItem(1L, 10L, new BigDecimal("5"), new BigDecimal("50"));

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());

        StockMovement movement = movementCaptor.getValue();
        assertThat(movement.getMovementType()).isEqualTo("SUPPLIER_RETURN");
        assertThat(movement.getQuantityChange()).isEqualByComparingTo("-5");
    }

    @Test
    void addItem_rejectsReturningMoreThanAvailableStock() {

        SupplierReturnService service = newService();

        SupplierReturn supplierReturn = new SupplierReturn();
        supplierReturn.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setUnit("PKT");

        when(supplierReturnRepository.findById(1L)).thenReturn(Optional.of(supplierReturn));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(inventoryService.getCurrentStock(10L)).thenReturn(new BigDecimal("3"));

        assertThatThrownBy(() -> service.addItem(1L, 10L, new BigDecimal("5"), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("available warehouse stock");
    }
}
