package lk.aak.agency.service;

import lk.aak.agency.model.PurchaseInvoice;
import lk.aak.agency.model.PurchaseInvoiceItem;
import lk.aak.agency.model.StockMovement;
import lk.aak.agency.repository.PurchaseInvoiceItemRepository;
import lk.aak.agency.repository.PurchaseInvoiceRepository;
import lk.aak.agency.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseInvoiceService {

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceItemRepository purchaseInvoiceItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AuditLogService auditLogService;

    public PurchaseInvoiceService(
            PurchaseInvoiceRepository purchaseInvoiceRepository,
            PurchaseInvoiceItemRepository purchaseInvoiceItemRepository,
            StockMovementRepository stockMovementRepository,
            AuditLogService auditLogService) {

        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.purchaseInvoiceItemRepository = purchaseInvoiceItemRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.auditLogService = auditLogService;
    }

    public List<PurchaseInvoice> getAllInvoices() {
        return purchaseInvoiceRepository
                .findAllByOrderByInvoiceDateDesc();
    }

    public Page<PurchaseInvoice> getInvoicePage(
            String search, int page, int size) {

        return purchaseInvoiceRepository.search(
                search == null ? "" : search.trim(),
                PageRequest.of(
                        Math.max(page, 0),
                        Math.max(size, 1),
                        Sort.by(Sort.Direction.DESC, "invoiceDate")
                )
        );
    }

    public PurchaseInvoice getInvoiceById(Long id) {
        return purchaseInvoiceRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Purchase invoice not found: " + id
                        )
                );
    }

    public List<PurchaseInvoiceItem> getItemsByInvoiceId(
            Long invoiceId) {

        return purchaseInvoiceItemRepository
                .findByPurchaseInvoiceIdOrderByIdAsc(invoiceId);
    }

    @Transactional
    public PurchaseInvoice saveInvoice(
            PurchaseInvoice purchaseInvoice) {

        validateInvoiceCanBeSaved(purchaseInvoice);
        setInvoiceDefaults(purchaseInvoice);
        resetVerification(purchaseInvoice);

        BigDecimal subtotal =
                zeroIfNull(purchaseInvoice.getSubtotal());

        BigDecimal discountAmount =
                zeroIfNull(purchaseInvoice.getDiscountAmount());

        BigDecimal vatAmount =
                zeroIfNull(purchaseInvoice.getVatAmount());

        BigDecimal totalAmount =
                subtotal
                        .subtract(discountAmount)
                        .add(vatAmount);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        purchaseInvoice.setTotalBoxes(BigDecimal.ZERO);
        purchaseInvoice.setSubtotal(subtotal);
        purchaseInvoice.setDiscountAmount(discountAmount);
        purchaseInvoice.setVatAmount(vatAmount);
        purchaseInvoice.setTotalAmount(totalAmount);

        return purchaseInvoiceRepository.save(purchaseInvoice);
    }

    @Transactional
    public PurchaseInvoice saveInvoiceWithItems(
            PurchaseInvoice purchaseInvoice,
            List<PurchaseInvoiceItem> items) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Add at least one product to the purchase invoice."
            );
        }

        validateInvoiceCanBeSaved(purchaseInvoice);
        setInvoiceDefaults(purchaseInvoice);
        resetVerification(purchaseInvoice);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (PurchaseInvoiceItem item : items) {
            validateItem(item);

            item.setUnit(item.getProduct().getUnit());
            item.calculateAmount();

            subtotal = subtotal.add(
                    zeroIfNull(item.getAmount())
            );
        }

        BigDecimal discountAmount =
                zeroIfNull(purchaseInvoice.getDiscountAmount());

        BigDecimal vatAmount =
                zeroIfNull(purchaseInvoice.getVatAmount());

        validateDiscount(subtotal, discountAmount);

        BigDecimal totalAmount =
                subtotal
                        .subtract(discountAmount)
                        .add(vatAmount);

        purchaseInvoice.setTotalBoxes(BigDecimal.ZERO);
        purchaseInvoice.setSubtotal(subtotal);
        purchaseInvoice.setDiscountAmount(discountAmount);
        purchaseInvoice.setVatAmount(vatAmount);
        purchaseInvoice.setTotalAmount(totalAmount);

        PurchaseInvoice savedInvoice =
                purchaseInvoiceRepository.save(purchaseInvoice);

        purchaseInvoiceRepository.flush();

        if (savedInvoice.getId() != null) {
            purchaseInvoiceItemRepository
                    .deleteByPurchaseInvoiceId(
                            savedInvoice.getId()
                    );

            purchaseInvoiceItemRepository.flush();
        }

        for (PurchaseInvoiceItem item : items) {
            item.setId(null);
            item.setPurchaseInvoice(savedInvoice);
            item.setUnit(item.getProduct().getUnit());
            item.calculateAmount();

            purchaseInvoiceItemRepository.save(item);
        }

        purchaseInvoiceItemRepository.flush();

        return savedInvoice;
    }

    @Transactional
    public PurchaseInvoiceItem saveItem(
            PurchaseInvoiceItem item) {

        PurchaseInvoice invoice =
                item.getPurchaseInvoice();

        if (invoice == null || invoice.getId() == null) {
            throw new IllegalArgumentException(
                    "Save the purchase invoice before adding products."
            );
        }

        if ("COMPLETED".equalsIgnoreCase(
                invoice.getStatus())) {

            throw new IllegalArgumentException(
                    "Products cannot be added to a completed invoice."
            );
        }

        validateItem(item);

        item.calculateAmount();

        PurchaseInvoiceItem savedItem =
                purchaseInvoiceItemRepository.save(item);

        purchaseInvoiceItemRepository.flush();

        resetInvoiceVerification(invoice.getId());
        recalculateInvoiceTotals(invoice.getId());

        return savedItem;
    }

    @Transactional
    public void deleteItem(Long itemId) {

        PurchaseInvoiceItem item =
                purchaseInvoiceItemRepository
                        .findById(itemId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Purchase invoice item was not found."
                                )
                        );

        PurchaseInvoice invoice =
                item.getPurchaseInvoice();

        if ("COMPLETED".equalsIgnoreCase(
                invoice.getStatus())) {

            throw new IllegalArgumentException(
                    "Products cannot be removed from a completed invoice."
            );
        }

        Long invoiceId = invoice.getId();

        purchaseInvoiceItemRepository.deleteById(itemId);
        purchaseInvoiceItemRepository.flush();

        resetInvoiceVerification(invoiceId);
        recalculateInvoiceTotals(invoiceId);
    }

    @Transactional
    public void completeInvoice(
            Long invoiceId,
            boolean verificationConfirmed) {

        PurchaseInvoice invoice =
                getInvoiceById(invoiceId);

        if ("COMPLETED".equalsIgnoreCase(
                invoice.getStatus())) {

            throw new IllegalArgumentException(
                    "This purchase invoice is already completed."
            );
        }

        if (!invoice.hasInvoiceFile()) {
            throw new IllegalArgumentException(
                    "Upload the original CBL invoice image or PDF "
                            + "before completing this invoice."
            );
        }

        if (!verificationConfirmed) {
            throw new IllegalArgumentException(
                    "Compare the original CBL invoice with the entered "
                            + "details and confirm the verification checkbox."
            );
        }

        List<PurchaseInvoiceItem> items =
                getItemsByInvoiceId(invoiceId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Add at least one product before completing the invoice."
            );
        }

        recalculateInvoiceTotals(invoiceId);

        BigDecimal subtotal =
                zeroIfNull(invoice.getSubtotal());

        BigDecimal discountAmount =
                zeroIfNull(invoice.getDiscountAmount());

        validateDiscount(subtotal, discountAmount);

        for (PurchaseInvoiceItem item : items) {
            validateItem(item);

            boolean movementExists =
                    stockMovementRepository
                            .existsByReferenceTypeAndReferenceItemId(
                                    "PURCHASE_INVOICE_ITEM",
                                    item.getId()
                            );

            if (!movementExists) {
                StockMovement movement =
                        new StockMovement();

                movement.setProduct(item.getProduct());
                movement.setMovementType("PURCHASE");
                movement.setQuantityChange(item.getQuantity());
                movement.setStockUnit(item.getUnit());

                movement.setReferenceType(
                        "PURCHASE_INVOICE_ITEM"
                );

                movement.setReferenceNumber(
                        invoice.getDocumentNumber()
                );

                movement.setReferenceItemId(item.getId());

                movement.setNotes(
                        "Stock received from CBL purchase invoice "
                                + invoice.getDocumentNumber()
                );

                stockMovementRepository.save(movement);
            }
        }

        invoice.setInvoiceVerified(true);
        invoice.setInvoiceVerifiedAt(LocalDateTime.now());
        invoice.setStatus("COMPLETED");

        purchaseInvoiceRepository.save(invoice);
    }

    /*
     * Temporary compatibility method.
     * The controller will be updated in the next step
     * to send the verification checkbox value.
     */
    @Transactional
    public void completeInvoice(Long invoiceId) {
        completeInvoice(invoiceId, false);
    }

    @Transactional
    public void deleteInvoice(Long invoiceId) {

        PurchaseInvoice invoice =
                getInvoiceById(invoiceId);

        if ("COMPLETED".equalsIgnoreCase(
                invoice.getStatus())) {

            throw new IllegalArgumentException(
                    "A completed invoice cannot be deleted."
            );
        }

        purchaseInvoiceItemRepository
                .deleteByPurchaseInvoiceId(invoiceId);

        purchaseInvoiceItemRepository.flush();

        purchaseInvoiceRepository.deleteById(invoiceId);

        auditLogService.record(
                "PURCHASE_INVOICE_DELETED", "PurchaseInvoice", invoiceId,
                "Deleted purchase invoice \"" + invoice.getDocumentNumber() + "\""
        );
    }

    private void validateInvoiceCanBeSaved(
            PurchaseInvoice purchaseInvoice) {

        if (purchaseInvoice == null) {
            throw new IllegalArgumentException(
                    "Purchase invoice information is required."
            );
        }

        if (purchaseInvoice.getDocumentNumber() == null
                || purchaseInvoice.getDocumentNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Document number is required."
            );
        }

        if (purchaseInvoice.getInvoiceDate() == null) {
            throw new IllegalArgumentException(
                    "Invoice date is required."
            );
        }

        if (purchaseInvoice.getId() != null) {
            PurchaseInvoice existingInvoice =
                    getInvoiceById(purchaseInvoice.getId());

            if ("COMPLETED".equalsIgnoreCase(
                    existingInvoice.getStatus())) {

                throw new IllegalArgumentException(
                        "A completed purchase invoice cannot be edited."
                );
            }
        }

        Optional<PurchaseInvoice> duplicateInvoice =
                purchaseInvoiceRepository
                        .findByDocumentNumber(
                                purchaseInvoice
                                        .getDocumentNumber()
                                        .trim()
                        );

        if (duplicateInvoice.isPresent()
                && !duplicateInvoice.get().getId()
                .equals(purchaseInvoice.getId())) {

            throw new IllegalArgumentException(
                    "Document number already exists."
            );
        }
    }

    private void setInvoiceDefaults(
            PurchaseInvoice purchaseInvoice) {

        purchaseInvoice.setSupplierName(
                "CBL Foods International (Pvt) Ltd"
        );

        purchaseInvoice.setPlaceOfSupply("Ranala");

        if (purchaseInvoice.getStatus() == null
                || purchaseInvoice.getStatus().isBlank()) {

            purchaseInvoice.setStatus("DRAFT");
        }

        purchaseInvoice.setTotalBoxes(BigDecimal.ZERO);

        purchaseInvoice.setSubtotal(
                zeroIfNull(purchaseInvoice.getSubtotal())
        );

        purchaseInvoice.setDiscountAmount(
                zeroIfNull(
                        purchaseInvoice.getDiscountAmount()
                )
        );

        purchaseInvoice.setVatAmount(
                zeroIfNull(purchaseInvoice.getVatAmount())
        );

        purchaseInvoice.setTotalAmount(
                zeroIfNull(purchaseInvoice.getTotalAmount())
        );
    }

    private void validateItem(
            PurchaseInvoiceItem item) {

        if (item == null) {
            throw new IllegalArgumentException(
                    "Purchase invoice item is required."
            );
        }

        if (item.getProduct() == null) {
            throw new IllegalArgumentException(
                    "Select a product for every invoice row."
            );
        }

        if (item.getQuantity() == null
                || item.getQuantity()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Purchase quantity must be greater than zero."
            );
        }

        if (item.getUnitPrice() == null
                || item.getUnitPrice()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Purchase rate cannot be negative."
            );
        }

        item.setUnit(item.getProduct().getUnit());
    }

    private void validateDiscount(
            BigDecimal subtotal,
            BigDecimal discountAmount) {

        if (discountAmount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException(
                    "Discount amount cannot be greater than the invoice value."
            );
        }
    }

    private void recalculateInvoiceTotals(
            Long invoiceId) {

        PurchaseInvoice invoice =
                getInvoiceById(invoiceId);

        List<PurchaseInvoiceItem> items =
                getItemsByInvoiceId(invoiceId);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (PurchaseInvoiceItem item : items) {
            item.calculateAmount();

            subtotal = subtotal.add(
                    zeroIfNull(item.getAmount())
            );
        }

        BigDecimal discountAmount =
                zeroIfNull(invoice.getDiscountAmount());

        BigDecimal vatAmount =
                zeroIfNull(invoice.getVatAmount());

        BigDecimal totalAmount =
                subtotal
                        .subtract(discountAmount)
                        .add(vatAmount);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        invoice.setTotalBoxes(BigDecimal.ZERO);
        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(discountAmount);
        invoice.setVatAmount(vatAmount);
        invoice.setTotalAmount(totalAmount);

        purchaseInvoiceRepository.save(invoice);
    }

    private void resetInvoiceVerification(
            Long invoiceId) {

        PurchaseInvoice invoice =
                getInvoiceById(invoiceId);

        resetVerification(invoice);

        purchaseInvoiceRepository.save(invoice);
    }

    private void resetVerification(
            PurchaseInvoice invoice) {

        invoice.setInvoiceVerified(false);
        invoice.setInvoiceVerifiedAt(null);
    }

    private BigDecimal zeroIfNull(
            BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}