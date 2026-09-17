package lk.aak.agency.service;

import lk.aak.agency.model.Product;
import lk.aak.agency.model.StockMovement;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.repository.StockMovementRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public InventoryService(
            ProductRepository productRepository,
            StockMovementRepository stockMovementRepository) {

        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<Product> getAllProducts() {

        return productRepository.findAll(
                Sort.by("productName").ascending()
        );
    }

    public BigDecimal getCurrentStock(Long productId) {

        if (productId == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal currentStock =
                stockMovementRepository
                        .calculateCurrentStock(productId);

        return zeroIfNull(currentStock);
    }

    public Map<Long, BigDecimal> getStockByProduct() {

        Map<Long, BigDecimal> stockByProduct =
                new LinkedHashMap<>();

        List<Product> products = getAllProducts();

        for (Product product : products) {

            stockByProduct.put(
                    product.getId(),
                    getCurrentStock(product.getId())
            );
        }

        return stockByProduct;
    }

    public List<Product> getLowStockProducts() {

        List<Product> lowStockProducts =
                new ArrayList<>();

        for (Product product : getAllProducts()) {

            if (!isActiveProduct(product)) {
                continue;
            }

            BigDecimal currentStock =
                    getCurrentStock(product.getId());

            BigDecimal reorderLevel =
                    zeroIfNull(product.getReorderLevel());

            if (currentStock.compareTo(
                    reorderLevel) <= 0) {

                lowStockProducts.add(product);
            }
        }

        return lowStockProducts;
    }

    public List<Product> getOutOfStockProducts() {

        List<Product> outOfStockProducts =
                new ArrayList<>();

        for (Product product : getAllProducts()) {

            if (!isActiveProduct(product)) {
                continue;
            }

            BigDecimal currentStock =
                    getCurrentStock(product.getId());

            if (currentStock.compareTo(
                    BigDecimal.ZERO) <= 0) {

                outOfStockProducts.add(product);
            }
        }

        return outOfStockProducts;
    }

    public Map<Long, BigDecimal> getShortageByProduct() {

        Map<Long, BigDecimal> shortageByProduct =
                new LinkedHashMap<>();

        for (Product product : getLowStockProducts()) {

            BigDecimal currentStock =
                    getCurrentStock(product.getId());

            BigDecimal reorderLevel =
                    zeroIfNull(product.getReorderLevel());

            BigDecimal shortage =
                    reorderLevel.subtract(currentStock);

            if (shortage.compareTo(
                    BigDecimal.ZERO) < 0) {

                shortage = BigDecimal.ZERO;
            }

            shortageByProduct.put(
                    product.getId(),
                    shortage
            );
        }

        return shortageByProduct;
    }

    public boolean isLowStock(Product product) {

        if (product == null
                || product.getId() == null
                || !isActiveProduct(product)) {

            return false;
        }

        BigDecimal currentStock =
                getCurrentStock(product.getId());

        BigDecimal reorderLevel =
                zeroIfNull(product.getReorderLevel());

        return currentStock.compareTo(
                reorderLevel) <= 0;
    }

    public boolean isOutOfStock(Product product) {

        if (product == null
                || product.getId() == null
                || !isActiveProduct(product)) {

            return false;
        }

        return getCurrentStock(product.getId())
                .compareTo(BigDecimal.ZERO) <= 0;
    }

    public List<StockMovement> getAllMovements() {

        return stockMovementRepository.findAll(
                Sort.by("movementDate").descending()
        );
    }

    public List<StockMovement> getProductMovements(
            Long productId) {

        return stockMovementRepository
                .findByProductIdOrderByMovementDateDesc(
                        productId
                );
    }

    private boolean isActiveProduct(Product product) {

        return product != null
                && "ACTIVE".equalsIgnoreCase(
                product.getStatus()
        );
    }

    private BigDecimal zeroIfNull(
            BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}
