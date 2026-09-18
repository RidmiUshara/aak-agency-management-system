package lk.aak.agency.service;

import lk.aak.agency.model.Product;
import lk.aak.agency.model.PurchaseInvoice;
import lk.aak.agency.model.StockMovement;
import lk.aak.agency.model.SupplierReturn;
import lk.aak.agency.model.SupplierReturnItem;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.repository.PurchaseInvoiceRepository;
import lk.aak.agency.repository.StockMovementRepository;
import lk.aak.agency.repository.SupplierReturnItemRepository;
import lk.aak.agency.repository.SupplierReturnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class SupplierReturnService {

    private final SupplierReturnRepository supplierReturnRepository;
    private final SupplierReturnItemRepository supplierReturnItemRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryService inventoryService;

    public SupplierReturnService(
            SupplierReturnRepository supplierReturnRepository,
            SupplierReturnItemRepository supplierReturnItemRepository,
            PurchaseInvoiceRepository purchaseInvoiceRepository,
            ProductRepository productRepository,
            StockMovementRepository stockMovementRepository,
            InventoryService inventoryService) {

        this.supplierReturnRepository = supplierReturnRepository;
        this.supplierReturnItemRepository = supplierReturnItemRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.inventoryService = inventoryService;
    }

    public List<SupplierReturn> getAllReturns() {
        return supplierReturnRepository.findAllByOrderByReturnDateDesc();
    }

    public Optional<SupplierReturn> getReturnById(Long id) {
        return supplierReturnRepository.findById(id);
    }

    public List<SupplierReturnItem> getItemsForReturn(Long supplierReturnId) {
        return supplierReturnItemRepository.findBySupplierReturnIdOrderByIdAsc(supplierReturnId);
    }

    @Transactional
    public SupplierReturn saveReturn(SupplierReturn supplierReturn) {

        if (supplierReturn.getPurchaseInvoiceId() != null) {

            PurchaseInvoice invoice = purchaseInvoiceRepository.findById(supplierReturn.getPurchaseInvoiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Selected purchase invoice was not found."));

            supplierReturn.setPurchaseInvoiceDocumentNumber(invoice.getDocumentNumber());

        } else {
            supplierReturn.setPurchaseInvoiceDocumentNumber(null);
        }

        return supplierReturnRepository.save(supplierReturn);
    }

    @Transactional
    public SupplierReturnItem addItem(
            Long supplierReturnId, Long productId, BigDecimal quantity, BigDecimal unitPrice) {

        SupplierReturn supplierReturn = supplierReturnRepository.findById(supplierReturnId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier return was not found."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Selected product was not found."));

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Return quantity must be greater than zero.");
        }

        BigDecimal currentStock = inventoryService.getCurrentStock(product.getId());

        if (currentStock.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Cannot return more than the available warehouse stock. Available: "
                            + currentStock.stripTrailingZeros().toPlainString() + "."
            );
        }

        SupplierReturnItem item = new SupplierReturnItem();
        item.setSupplierReturnId(supplierReturn.getId());
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnit(product.getUnit());
        item.setUnitPrice(unitPrice == null ? BigDecimal.ZERO : unitPrice);

        SupplierReturnItem savedItem = supplierReturnItemRepository.save(item);
        supplierReturnItemRepository.flush();

        StockMovement stockMovement = new StockMovement();
        stockMovement.setProduct(product);
        stockMovement.setMovementType("SUPPLIER_RETURN");
        stockMovement.setQuantityChange(quantity.negate());
        stockMovement.setStockUnit(product.getUnit());
        stockMovement.setReferenceType("SUPPLIER_RETURN_ITEM");
        stockMovement.setReferenceItemId(savedItem.getId());
        stockMovement.setReferenceNumber("SUPR-" + supplierReturn.getId());
        stockMovement.setNotes("Stock returned to CBL - removed from warehouse.");

        stockMovementRepository.save(stockMovement);

        return savedItem;
    }
}
