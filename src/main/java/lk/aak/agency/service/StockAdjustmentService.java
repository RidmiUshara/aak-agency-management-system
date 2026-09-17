package lk.aak.agency.service;

import lk.aak.agency.model.Product;
import lk.aak.agency.model.StockAdjustment;
import lk.aak.agency.model.StockMovement;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.repository.StockAdjustmentRepository;
import lk.aak.agency.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockAdjustmentService {

    private final StockAdjustmentRepository
            stockAdjustmentRepository;

    private final StockMovementRepository
            stockMovementRepository;

    private final ProductRepository
            productRepository;

    private final InventoryService
            inventoryService;

    public StockAdjustmentService(
            StockAdjustmentRepository stockAdjustmentRepository,
            StockMovementRepository stockMovementRepository,
            ProductRepository productRepository,
            InventoryService inventoryService) {

        this.stockAdjustmentRepository =
                stockAdjustmentRepository;

        this.stockMovementRepository =
                stockMovementRepository;

        this.productRepository =
                productRepository;

        this.inventoryService =
                inventoryService;
    }

    public List<StockAdjustment> getAllAdjustments() {

        return stockAdjustmentRepository
                .findAllByOrderByAdjustmentDateDesc();
    }

    public List<StockAdjustment> getAdjustmentsByProduct(
            Long productId) {

        return stockAdjustmentRepository
                .findByProductIdOrderByAdjustmentDateDesc(
                        productId
                );
    }

    public StockAdjustment getAdjustmentById(
            Long adjustmentId) {

        return stockAdjustmentRepository
                .findById(adjustmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Stock adjustment was not found."
                        )
                );
    }

    @Transactional
    public StockAdjustment saveAdjustment(
            StockAdjustment adjustment,
            Long productId,
            String username) {

        if (adjustment == null) {

            throw new IllegalArgumentException(
                    "Stock adjustment information is required."
            );
        }

        if (productId == null) {

            throw new IllegalArgumentException(
                    "Please select a product."
            );
        }

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Selected product was not found."
                                )
                        );

        String adjustmentType =
                normalizeAdjustmentType(
                        adjustment.getAdjustmentType()
                );

        String direction =
                determineDirection(
                        adjustmentType,
                        adjustment.getDirection()
                );

        BigDecimal quantity =
                adjustment.getQuantity();

        if (quantity == null
                || quantity.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Adjustment quantity must be greater than zero."
            );
        }

        if ("OUT".equals(direction)) {

            BigDecimal currentStock =
                    inventoryService.getCurrentStock(
                            product.getId()
                    );

            if (currentStock.compareTo(quantity) < 0) {

                throw new IllegalArgumentException(
                        "Adjustment quantity cannot be greater "
                                + "than the available stock. "
                                + "Available stock: "
                                + currentStock
                                .stripTrailingZeros()
                                .toPlainString()
                                + " "
                                + getProductUnit(product)
                                + "."
                );
            }
        }

        adjustment.setProduct(product);
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.setDirection(direction);
        adjustment.setQuantity(quantity);

        if (adjustment.getAdjustmentDate() == null) {

            adjustment.setAdjustmentDate(
                    LocalDateTime.now()
            );
        }

        if (username == null
                || username.isBlank()) {

            adjustment.setCreatedBy("SYSTEM");

        } else {

            adjustment.setCreatedBy(
                    username.trim()
            );
        }

        StockAdjustment savedAdjustment =
                stockAdjustmentRepository.save(
                        adjustment
                );

        stockAdjustmentRepository.flush();

        StockMovement stockMovement =
                createStockMovement(
                        savedAdjustment
                );

        stockMovementRepository.save(
                stockMovement
        );

        return savedAdjustment;
    }

    private StockMovement createStockMovement(
            StockAdjustment adjustment) {

        StockMovement stockMovement =
                new StockMovement();

        BigDecimal quantityChange =
                adjustment.getQuantity();

        if ("OUT".equals(
                adjustment.getDirection())) {

            quantityChange =
                    quantityChange.negate();
        }

        stockMovement.setProduct(
                adjustment.getProduct()
        );

        stockMovement.setMovementType(
                "IN".equals(
                        adjustment.getDirection()
                )
                        ? "ADJUSTMENT_IN"
                        : "ADJUSTMENT_OUT"
        );

        stockMovement.setQuantityChange(
                quantityChange
        );

        stockMovement.setStockUnit(
                getProductUnit(
                        adjustment.getProduct()
                )
        );

        stockMovement.setReferenceType(
                "STOCK_ADJUSTMENT"
        );

        stockMovement.setReferenceItemId(
                adjustment.getId()
        );

        String referenceNumber =
                adjustment.getReferenceNumber();

        if (referenceNumber == null
                || referenceNumber.isBlank()) {

            referenceNumber =
                    "ADJ-" + adjustment.getId();
        }

        stockMovement.setReferenceNumber(
                referenceNumber
        );

        stockMovement.setMovementDate(
                adjustment.getAdjustmentDate()
        );

        stockMovement.setNotes(
                buildMovementNotes(adjustment)
        );

        return stockMovement;
    }

    private String buildMovementNotes(
            StockAdjustment adjustment) {

        StringBuilder movementNotes =
                new StringBuilder();

        movementNotes
                .append("Stock adjustment: ")
                .append(
                        adjustment
                                .getAdjustmentType()
                                .replace("_", " ")
                );

        if (adjustment.getNotes() != null
                && !adjustment.getNotes().isBlank()) {

            movementNotes
                    .append(" - ")
                    .append(
                            adjustment.getNotes()
                    );
        }

        return movementNotes.toString();
    }

    private String normalizeAdjustmentType(
            String adjustmentType) {

        if (adjustmentType == null
                || adjustmentType.isBlank()) {

            throw new IllegalArgumentException(
                    "Please select an adjustment reason."
            );
        }

        String normalizedType =
                adjustmentType
                        .trim()
                        .toUpperCase();

        boolean validType =
                normalizedType.equals("DAMAGED")
                        || normalizedType.equals("EXPIRED")
                        || normalizedType.equals("MISSING")
                        || normalizedType.equals(
                        "STOCK_CORRECTION_OUT"
                )
                        || normalizedType.equals(
                        "STOCK_CORRECTION_IN"
                )
                        || normalizedType.equals("OTHER");

        if (!validType) {

            throw new IllegalArgumentException(
                    "Selected adjustment reason is invalid."
            );
        }

        return normalizedType;
    }

    private String determineDirection(
            String adjustmentType,
            String selectedDirection) {

        if ("STOCK_CORRECTION_IN".equals(
                adjustmentType)) {

            return "IN";
        }

        if ("DAMAGED".equals(adjustmentType)
                || "EXPIRED".equals(adjustmentType)
                || "MISSING".equals(adjustmentType)
                || "STOCK_CORRECTION_OUT".equals(
                adjustmentType)) {

            return "OUT";
        }

        if (selectedDirection == null
                || selectedDirection.isBlank()) {

            throw new IllegalArgumentException(
                    "Please select whether the other "
                            + "adjustment adds or removes stock."
            );
        }

        String normalizedDirection =
                selectedDirection
                        .trim()
                        .toUpperCase();

        if (!normalizedDirection.equals("IN")
                && !normalizedDirection.equals("OUT")) {

            throw new IllegalArgumentException(
                    "Selected adjustment direction is invalid."
            );
        }

        return normalizedDirection;
    }

    private String getProductUnit(
            Product product) {

        if (product.getUnit() == null
                || product.getUnit().isBlank()) {

            return "PKT";
        }

        return product
                .getUnit()
                .trim()
                .toUpperCase();
    }
}