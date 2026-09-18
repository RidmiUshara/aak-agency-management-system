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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ShopReturnService {

    private final ShopReturnRepository shopReturnRepository;
    private final ShopReturnItemRepository shopReturnItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public ShopReturnService(
            ShopReturnRepository shopReturnRepository,
            ShopReturnItemRepository shopReturnItemRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            StockMovementRepository stockMovementRepository) {

        this.shopReturnRepository = shopReturnRepository;
        this.shopReturnItemRepository = shopReturnItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<ShopReturn> getAllReturns() {
        return shopReturnRepository.findAllByOrderByReturnDateDesc();
    }

    public Optional<ShopReturn> getReturnById(Long id) {
        return shopReturnRepository.findById(id);
    }

    public List<ShopReturnItem> getItemsForReturn(Long shopReturnId) {
        return shopReturnItemRepository.findByShopReturnIdOrderByIdAsc(shopReturnId);
    }

    @Transactional
    public ShopReturn saveReturn(ShopReturn shopReturn) {

        Customer customer = customerRepository.findById(shopReturn.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Selected shop was not found."));

        shopReturn.setCustomerName(customer.getCustomerName());

        return shopReturnRepository.save(shopReturn);
    }

    @Transactional
    public ShopReturnItem addItem(
            Long shopReturnId, Long productId, BigDecimal quantity, BigDecimal unitPrice, String category) {

        ShopReturn shopReturn = shopReturnRepository.findById(shopReturnId)
                .orElseThrow(() -> new IllegalArgumentException("Shop return was not found."));

        if (!"PENDING".equalsIgnoreCase(shopReturn.getStatus())) {
            throw new IllegalArgumentException("Items can only be added to a pending return.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Selected product was not found."));

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Return quantity must be greater than zero.");
        }

        String normalizedCategory = category == null ? "SALEABLE" : category.trim().toUpperCase();

        if (!java.util.Set.of("SALEABLE", "DAMAGED", "EXPIRED").contains(normalizedCategory)) {
            throw new IllegalArgumentException("Selected return category is invalid.");
        }

        ShopReturnItem item = new ShopReturnItem();
        item.setShopReturnId(shopReturn.getId());
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnit(product.getUnit());
        item.setUnitPrice(unitPrice == null ? BigDecimal.ZERO : unitPrice);
        item.setCategory(normalizedCategory);

        return shopReturnItemRepository.save(item);
    }

    /**
     * Owner/office approval - only now does stock actually move, and only saleable items go
     * back into sellable warehouse stock. Damaged/expired items stay recorded but written off.
     */
    @Transactional
    public void approveReturn(Long shopReturnId) {

        ShopReturn shopReturn = shopReturnRepository.findById(shopReturnId)
                .orElseThrow(() -> new IllegalArgumentException("Shop return was not found."));

        if (!"PENDING".equalsIgnoreCase(shopReturn.getStatus())) {
            throw new IllegalArgumentException("Only a pending return can be approved.");
        }

        List<ShopReturnItem> items = getItemsForReturn(shopReturnId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Add at least one item before approving this return.");
        }

        for (ShopReturnItem item : items) {

            if (!"SALEABLE".equalsIgnoreCase(item.getCategory())) {
                continue;
            }

            StockMovement stockMovement = new StockMovement();
            stockMovement.setProduct(item.getProduct());
            stockMovement.setMovementType("SHOP_RETURN");
            stockMovement.setQuantityChange(item.getQuantity());
            stockMovement.setStockUnit(item.getUnit());
            stockMovement.setReferenceType("SHOP_RETURN_ITEM");
            stockMovement.setReferenceItemId(item.getId());
            stockMovement.setReferenceNumber("SR-" + shopReturn.getId());
            stockMovement.setNotes(
                    "Shop return from " + shopReturn.getCustomerName() + " - restocked to warehouse."
            );

            stockMovementRepository.save(stockMovement);
        }

        shopReturn.setStatus("APPROVED");
        shopReturnRepository.save(shopReturn);
    }

    @Transactional
    public void rejectReturn(Long shopReturnId) {

        ShopReturn shopReturn = shopReturnRepository.findById(shopReturnId)
                .orElseThrow(() -> new IllegalArgumentException("Shop return was not found."));

        if (!"PENDING".equalsIgnoreCase(shopReturn.getStatus())) {
            throw new IllegalArgumentException("Only a pending return can be rejected.");
        }

        shopReturn.setStatus("REJECTED");
        shopReturnRepository.save(shopReturn);
    }
}
