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
    public ShopReturnItem addItem(Long shopReturnId, Long productId, BigDecimal quantity, BigDecimal unitPrice) {

        ShopReturn shopReturn = shopReturnRepository.findById(shopReturnId)
                .orElseThrow(() -> new IllegalArgumentException("Shop return was not found."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Selected product was not found."));

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Return quantity must be greater than zero.");
        }

        ShopReturnItem item = new ShopReturnItem();
        item.setShopReturnId(shopReturn.getId());
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnit(product.getUnit());
        item.setUnitPrice(unitPrice == null ? BigDecimal.ZERO : unitPrice);

        ShopReturnItem savedItem = shopReturnItemRepository.save(item);
        shopReturnItemRepository.flush();

        StockMovement stockMovement = new StockMovement();
        stockMovement.setProduct(product);
        stockMovement.setMovementType("SHOP_RETURN");
        stockMovement.setQuantityChange(quantity);
        stockMovement.setStockUnit(product.getUnit());
        stockMovement.setReferenceType("SHOP_RETURN_ITEM");
        stockMovement.setReferenceItemId(savedItem.getId());
        stockMovement.setReferenceNumber("SR-" + shopReturn.getId());
        stockMovement.setNotes(
                "Shop return from " + shopReturn.getCustomerName() + " - restocked to warehouse."
        );

        stockMovementRepository.save(stockMovement);

        return savedItem;
    }
}
