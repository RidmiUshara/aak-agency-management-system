package lk.aak.agency.service;

import lk.aak.agency.model.Customer;
import lk.aak.agency.model.Product;
import lk.aak.agency.model.ShopReturn;
import lk.aak.agency.model.ShopReturnItem;
import lk.aak.agency.model.StockMovement;
import lk.aak.agency.repository.CustomerRepository;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.repository.ShopReturnItemRepository;
import lk.aak.agency.repository.ShopReturnRepository;
import lk.aak.agency.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopReturnServiceTest {

    @Mock
    private ShopReturnRepository shopReturnRepository;
    @Mock
    private ShopReturnItemRepository shopReturnItemRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private AuditLogService auditLogService;

    private ShopReturnService newService() {
        return new ShopReturnService(
                shopReturnRepository, shopReturnItemRepository,
                customerRepository, productRepository, stockMovementRepository, auditLogService
        );
    }

    @Test
    void addItem_doesNotTouchStockUntilApproved() {

        ShopReturnService service = newService();

        ShopReturn shopReturn = new ShopReturn();
        shopReturn.setId(1L);
        shopReturn.setCustomerName("Test Shop");
        shopReturn.setStatus("PENDING");

        Product product = new Product();
        product.setId(10L);
        product.setUnit("PKT");

        when(shopReturnRepository.findById(1L)).thenReturn(Optional.of(shopReturn));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(shopReturnItemRepository.save(any(ShopReturnItem.class)))
                .thenAnswer(invocation -> {
                    ShopReturnItem item = invocation.getArgument(0);
                    item.setId(100L);
                    return item;
                });

        ShopReturnItem savedItem =
                service.addItem(1L, 10L, new BigDecimal("5"), new BigDecimal("50"), "SALEABLE");

        assertThat(savedItem.getCategory()).isEqualTo("SALEABLE");
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void approveReturn_restocksOnlySaleableItemsAndMarksApproved() {

        ShopReturnService service = newService();

        ShopReturn shopReturn = new ShopReturn();
        shopReturn.setId(1L);
        shopReturn.setCustomerName("Test Shop");
        shopReturn.setStatus("PENDING");

        Product product = new Product();
        product.setId(10L);
        product.setUnit("PKT");

        ShopReturnItem saleableItem = new ShopReturnItem();
        saleableItem.setId(100L);
        saleableItem.setProduct(product);
        saleableItem.setQuantity(new BigDecimal("5"));
        saleableItem.setUnit("PKT");
        saleableItem.setCategory("SALEABLE");

        ShopReturnItem damagedItem = new ShopReturnItem();
        damagedItem.setId(101L);
        damagedItem.setProduct(product);
        damagedItem.setQuantity(new BigDecimal("2"));
        damagedItem.setUnit("PKT");
        damagedItem.setCategory("DAMAGED");

        when(shopReturnRepository.findById(1L)).thenReturn(Optional.of(shopReturn));
        when(shopReturnItemRepository.findByShopReturnIdOrderByIdAsc(1L))
                .thenReturn(List.of(saleableItem, damagedItem));

        service.approveReturn(1L);

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());

        StockMovement movement = movementCaptor.getValue();
        assertThat(movement.getMovementType()).isEqualTo("SHOP_RETURN");
        assertThat(movement.getQuantityChange()).isEqualByComparingTo("5");
        assertThat(shopReturn.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void saveReturn_resolvesCustomerNameFromCustomerId() {

        ShopReturnService service = newService();

        Customer customer = new Customer();
        customer.setId(2L);
        customer.setCustomerName("Resolved Shop Name");

        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(shopReturnRepository.save(any(ShopReturn.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShopReturn shopReturn = new ShopReturn();
        shopReturn.setCustomerId(2L);

        ShopReturn saved = service.saveReturn(shopReturn);

        assertThat(saved.getCustomerName()).isEqualTo("Resolved Shop Name");
    }
}
