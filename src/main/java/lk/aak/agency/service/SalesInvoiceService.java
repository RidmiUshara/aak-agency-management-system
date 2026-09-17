package lk.aak.agency.service;

import lk.aak.agency.model.Product;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.model.SalesInvoiceItem;
import lk.aak.agency.model.StockMovement;
import lk.aak.agency.repository.SalesInvoiceItemRepository;
import lk.aak.agency.repository.SalesInvoiceRepository;
import lk.aak.agency.repository.StockMovementRepository;
import lk.aak.agency.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SalesInvoiceService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesInvoiceItemRepository
            salesInvoiceItemRepository;
    private final StockMovementRepository
            stockMovementRepository;
    private final PaymentRepository paymentRepository;

    public SalesInvoiceService(
            SalesInvoiceRepository salesInvoiceRepository,
            SalesInvoiceItemRepository salesInvoiceItemRepository,
            StockMovementRepository stockMovementRepository,
            PaymentRepository paymentRepository) {

        this.salesInvoiceRepository =
                salesInvoiceRepository;

        this.salesInvoiceItemRepository =
                salesInvoiceItemRepository;

        this.stockMovementRepository =
                stockMovementRepository;

        this.paymentRepository =
                paymentRepository;
    }

    public List<SalesInvoice> getAllInvoices() {

        return salesInvoiceRepository
                .findAllByOrderByInvoiceDateDesc();
    }

    public SalesInvoice getInvoiceById(Long id) {

        return salesInvoiceRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sales invoice not found: " + id
                        )
                );
    }

    public List<SalesInvoiceItem> getItemsByInvoiceId(
            Long invoiceId) {

        return salesInvoiceItemRepository
                .findBySalesInvoiceIdOrderByIdAsc(
                        invoiceId
                );
    }

    @Transactional
    public SalesInvoice saveInvoice(
            SalesInvoice salesInvoice) {

        validateInvoice(salesInvoice);

        BigDecimal grossAmount =
                zeroIfNull(
                        salesInvoice.getGrossAmount()
                );

        BigDecimal discountAmount =
                zeroIfNull(
                        salesInvoice.getDiscountAmount()
                );

        BigDecimal returnAmount =
                zeroIfNull(
                        salesInvoice.getReturnAmount()
                );

        validateAdjustments(
                grossAmount,
                discountAmount,
                returnAmount
        );

        BigDecimal netAmount =
                grossAmount
                        .subtract(discountAmount)
                        .subtract(returnAmount);

        salesInvoice.setGrossAmount(grossAmount);
        salesInvoice.setDiscountAmount(discountAmount);
        salesInvoice.setReturnAmount(returnAmount);
        salesInvoice.setNetAmount(netAmount);

        setInvoiceDefaults(salesInvoice);

        return salesInvoiceRepository.save(
                salesInvoice
        );
    }

    @Transactional
    public SalesInvoice saveInvoiceWithItems(
            SalesInvoice salesInvoice,
            List<SalesInvoiceItem> items) {

        validateInvoice(salesInvoice);

        if (items == null || items.isEmpty()) {

            throw new IllegalArgumentException(
                    "Add at least one product before saving the invoice."
            );
        }

        SalesInvoice existingInvoice = null;

        if (salesInvoice.getId() != null) {

            existingInvoice =
                    getInvoiceById(
                            salesInvoice.getId()
                    );

            if ("COMPLETED".equalsIgnoreCase(
                    existingInvoice.getStatus())) {

                throw new IllegalArgumentException(
                        "A completed sales invoice cannot be edited."
                );
            }

            salesInvoice.setStatus(
                    existingInvoice.getStatus()
            );

            salesInvoice.setPaymentStatus(
                    existingInvoice.getPaymentStatus()
            );

        } else {

            salesInvoice.setStatus("DRAFT");
            salesInvoice.setPaymentStatus("UNPAID");
        }

        BigDecimal grossAmount =
                BigDecimal.ZERO;

        for (SalesInvoiceItem item : items) {

            validateItem(item);

            item.setUnit(
                    item.getProduct().getUnit()
            );

            item.calculateAmount();

            grossAmount =
                    grossAmount.add(
                            zeroIfNull(
                                    item.getAmount()
                            )
                    );
        }

        BigDecimal discountAmount =
                zeroIfNull(
                        salesInvoice.getDiscountAmount()
                );

        BigDecimal returnAmount =
                zeroIfNull(
                        salesInvoice.getReturnAmount()
                );

        validateAdjustments(
                grossAmount,
                discountAmount,
                returnAmount
        );

        BigDecimal netAmount =
                grossAmount
                        .subtract(discountAmount)
                        .subtract(returnAmount);

        salesInvoice.setGrossAmount(grossAmount);
        salesInvoice.setDiscountAmount(discountAmount);
        salesInvoice.setReturnAmount(returnAmount);
        salesInvoice.setNetAmount(netAmount);

        SalesInvoice savedInvoice =
                salesInvoiceRepository.save(
                        salesInvoice
                );

        salesInvoiceRepository.flush();

        if (existingInvoice != null) {

            salesInvoiceItemRepository
                    .deleteBySalesInvoiceId(
                            savedInvoice.getId()
                    );

            salesInvoiceItemRepository.flush();
        }

        for (SalesInvoiceItem item : items) {

            item.setId(null);
            item.setSalesInvoice(savedInvoice);

            item.setUnit(
                    item.getProduct().getUnit()
            );

            item.calculateAmount();

            salesInvoiceItemRepository.save(item);
        }

        salesInvoiceItemRepository.flush();

        return savedInvoice;
    }

    @Transactional
    public SalesInvoiceItem saveItem(
            SalesInvoiceItem item) {

        if (item == null) {

            throw new IllegalArgumentException(
                    "Sales invoice product information is required."
            );
        }

        SalesInvoice invoice =
                item.getSalesInvoice();

        if (invoice == null
                || invoice.getId() == null) {

            throw new IllegalArgumentException(
                    "Save the sales invoice before adding products."
            );
        }

        if ("COMPLETED".equalsIgnoreCase(
                invoice.getStatus())) {

            throw new IllegalArgumentException(
                    "Products cannot be added to a completed invoice."
            );
        }

        validateItem(item);

        item.setUnit(
                item.getProduct().getUnit()
        );

        item.calculateAmount();

        SalesInvoiceItem savedItem =
                salesInvoiceItemRepository.save(item);

        salesInvoiceItemRepository.flush();

        recalculateInvoiceTotals(
                invoice.getId()
        );

        return savedItem;
    }

    @Transactional
    public void deleteItem(Long itemId) {

        SalesInvoiceItem item =
                salesInvoiceItemRepository
                        .findById(itemId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Sales invoice item was not found."
                                )
                        );

        SalesInvoice invoice =
                item.getSalesInvoice();

        if ("COMPLETED".equalsIgnoreCase(
                invoice.getStatus())) {

            throw new IllegalArgumentException(
                    "Products cannot be removed from a completed invoice."
            );
        }

        Long invoiceId =
                invoice.getId();

        salesInvoiceItemRepository
                .deleteById(itemId);

        salesInvoiceItemRepository.flush();

        recalculateInvoiceTotals(invoiceId);

        validateCustomerCreditLimit(invoice);
    }

    @Transactional
    public void completeInvoice(Long invoiceId) {

        SalesInvoice invoice =
                getInvoiceById(invoiceId);

        if ("COMPLETED".equalsIgnoreCase(
                invoice.getStatus())) {

            throw new IllegalArgumentException(
                    "This sales invoice is already completed."
            );
        }

        List<SalesInvoiceItem> items =
                getItemsByInvoiceId(invoiceId);

        if (items.isEmpty()) {

            throw new IllegalArgumentException(
                    "Add at least one product before completing the invoice."
            );
        }

        recalculateInvoiceTotals(invoiceId);

        /*
         * Several invoice rows may contain the same product.
         * Therefore quantities are grouped by product before
         * checking the available stock.
         */
        Map<Long, BigDecimal> requiredQuantities =
                new LinkedHashMap<>();

        Map<Long, Product> requiredProducts =
                new LinkedHashMap<>();

        for (SalesInvoiceItem item : items) {

            validateItem(item);

            Product product =
                    item.getProduct();

            BigDecimal quantity =
                    zeroIfNull(
                            item.getQuantity()
                    );

            requiredQuantities.merge(
                    product.getId(),
                    quantity,
                    BigDecimal::add
            );

            requiredProducts.put(
                    product.getId(),
                    product
            );
        }

        /*
         * Validate every required product before
         * creating any negative stock movement.
         */
        for (Map.Entry<Long, BigDecimal> entry
                : requiredQuantities.entrySet()) {

            Long productId =
                    entry.getKey();

            BigDecimal requiredQuantity =
                    entry.getValue();

            BigDecimal currentStock =
                    getAvailableStock(productId);

            if (currentStock.compareTo(
                    requiredQuantity) < 0) {

                Product product =
                        requiredProducts.get(productId);

                throw new IllegalArgumentException(
                        "Not enough stock for "
                                + product.getDisplayName()
                                + ". Available: "
                                + currentStock.stripTrailingZeros()
                                .toPlainString()
                                + " "
                                + getProductUnit(product)
                                + ", required: "
                                + requiredQuantity
                                .stripTrailingZeros()
                                .toPlainString()
                                + " "
                                + getProductUnit(product)
                                + "."
                );
            }
        }

        /*
         * Each item creates one negative stock movement.
         * The reference item check prevents the same
         * invoice row from deducting stock twice.
         */
        for (SalesInvoiceItem item : items) {

            boolean movementExists =
                    stockMovementRepository
                            .existsByReferenceTypeAndReferenceItemId(
                                    "SALES_INVOICE_ITEM",
                                    item.getId()
                            );

            if (!movementExists) {

                StockMovement stockMovement =
                        new StockMovement();

                stockMovement.setProduct(
                        item.getProduct()
                );

                stockMovement.setMovementType(
                        "SALE"
                );

                stockMovement.setQuantityChange(
                        item.getQuantity().negate()
                );

                stockMovement.setStockUnit(
                        item.getUnit()
                );

                stockMovement.setReferenceType(
                        "SALES_INVOICE_ITEM"
                );

                stockMovement.setReferenceNumber(
                        invoice.getInvoiceNumber()
                );

                stockMovement.setReferenceItemId(
                        item.getId()
                );

                stockMovement.setNotes(
                        "Stock issued to customer from sales invoice "
                                + invoice.getInvoiceNumber()
                );

                stockMovementRepository.save(
                        stockMovement
                );
            }
        }

        invoice.setStatus("COMPLETED");

        if ("CASH".equalsIgnoreCase(
                invoice.getSaleType())) {

            invoice.setPaymentStatus("PAID");
        }

        salesInvoiceRepository.save(invoice);
    }

    @Transactional
    public void deleteInvoice(Long invoiceId) {

        SalesInvoice invoice =
                getInvoiceById(invoiceId);

        if ("COMPLETED".equalsIgnoreCase(
                invoice.getStatus())) {

            throw new IllegalArgumentException(
                    "A completed sales invoice cannot be deleted."
            );
        }

        salesInvoiceItemRepository
                .deleteBySalesInvoiceId(invoiceId);

        salesInvoiceItemRepository.flush();

        salesInvoiceRepository
                .deleteById(invoiceId);
    }

    public BigDecimal getAvailableStock(
            Long productId) {

        if (productId == null) {
            return BigDecimal.ZERO;
        }

        return zeroIfNull(
                stockMovementRepository
                        .calculateCurrentStock(productId)
        );
    }

    private void recalculateInvoiceTotals(
            Long invoiceId) {

        SalesInvoice invoice =
                getInvoiceById(invoiceId);

        List<SalesInvoiceItem> items =
                getItemsByInvoiceId(invoiceId);

        BigDecimal grossAmount =
                BigDecimal.ZERO;

        for (SalesInvoiceItem item : items) {

            item.setUnit(
                    item.getProduct().getUnit()
            );

            item.calculateAmount();

            grossAmount =
                    grossAmount.add(
                            zeroIfNull(
                                    item.getAmount()
                            )
                    );
        }

        BigDecimal discountAmount =
                zeroIfNull(
                        invoice.getDiscountAmount()
                );

        BigDecimal returnAmount =
                zeroIfNull(
                        invoice.getReturnAmount()
                );

        BigDecimal netAmount =
                grossAmount
                        .subtract(discountAmount)
                        .subtract(returnAmount);

        /*
         * A draft can temporarily have no product rows.
         */
        if (netAmount.compareTo(
                BigDecimal.ZERO) < 0) {

            netAmount = BigDecimal.ZERO;
        }

        invoice.setGrossAmount(grossAmount);
        invoice.setDiscountAmount(discountAmount);
        invoice.setReturnAmount(returnAmount);
        invoice.setNetAmount(netAmount);

        salesInvoiceRepository.save(invoice);
    }

    private void validateInvoice(
            SalesInvoice salesInvoice) {

        if (salesInvoice == null) {

            throw new IllegalArgumentException(
                    "Sales invoice information is required."
            );
        }

        validateInvoiceNumber(salesInvoice);

        if (salesInvoice.getInvoiceDate() == null) {

            throw new IllegalArgumentException(
                    "Invoice date is required."
            );
        }

        if (salesInvoice.getCustomer() == null) {

            throw new IllegalArgumentException(
                    "Please select a customer."
            );
        }

        if (salesInvoice.getSaleType() == null
                || salesInvoice.getSaleType().isBlank()) {

            salesInvoice.setSaleType("CREDIT");
        }
    }

    private void validateInvoiceNumber(
            SalesInvoice salesInvoice) {

        if (salesInvoice.getInvoiceNumber() == null
                || salesInvoice.getInvoiceNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Invoice number is required."
            );
        }

        salesInvoice.setInvoiceNumber(
                salesInvoice
                        .getInvoiceNumber()
                        .trim()
        );

        Optional<SalesInvoice> existingInvoice =
                salesInvoiceRepository
                        .findByInvoiceNumber(
                                salesInvoice
                                        .getInvoiceNumber()
                        );

        if (existingInvoice.isPresent()
                && !existingInvoice.get().getId()
                .equals(salesInvoice.getId())) {

            throw new IllegalArgumentException(
                    "Invoice number already exists."
            );
        }
    }

    private void validateItem(
            SalesInvoiceItem item) {

        if (item == null) {

            throw new IllegalArgumentException(
                    "Sales invoice product information is required."
            );
        }

        if (item.getProduct() == null) {

            throw new IllegalArgumentException(
                    "Please select a product."
            );
        }

        if (item.getQuantity() == null
                || item.getQuantity()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Product quantity must be greater than zero."
            );
        }

        if (item.getUnitPrice() == null
                || item.getUnitPrice()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Selling rate cannot be negative."
            );
        }

        String productUnit =
                getProductUnit(
                        item.getProduct()
                );

        item.setUnit(productUnit);
    }

    private void validateAdjustments(
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal returnAmount) {

        if (discountAmount.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Discount amount cannot be negative."
            );
        }

        if (returnAmount.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Return amount cannot be negative."
            );
        }

        BigDecimal deductions =
                discountAmount.add(returnAmount);

        if (deductions.compareTo(
                grossAmount) > 0) {

            throw new IllegalArgumentException(
                    "Discount and return amounts cannot "
                            + "be greater than the gross amount."
            );
        }
    }

    private void setInvoiceDefaults(
            SalesInvoice salesInvoice) {

        if (salesInvoice.getStatus() == null
                || salesInvoice.getStatus().isBlank()) {

            salesInvoice.setStatus("DRAFT");
        }

        if (salesInvoice.getPaymentStatus() == null
                || salesInvoice.getPaymentStatus().isBlank()) {

            salesInvoice.setPaymentStatus("UNPAID");
        }

        if (salesInvoice.getSaleType() == null
                || salesInvoice.getSaleType().isBlank()) {

            salesInvoice.setSaleType("CREDIT");
        }
    }

    private void validateCustomerCreditLimit(
            SalesInvoice invoice) {

        if (!"CREDIT".equalsIgnoreCase(
                invoice.getSaleType())) {

            return;
        }

        if (invoice.getCustomer() == null
                || invoice.getCustomer().getId() == null) {

            throw new IllegalArgumentException(
                    "Please select a customer."
            );
        }

        BigDecimal creditLimit =
                zeroIfNull(
                        invoice.getCustomer()
                                .getCreditLimit()
                );

        BigDecimal currentOutstanding =
                BigDecimal.ZERO;

        List<SalesInvoice> customerInvoices =
                salesInvoiceRepository
                        .findByCustomerIdOrderByInvoiceDateDesc(
                                invoice.getCustomer().getId()
                        );

        for (SalesInvoice previousInvoice
                : customerInvoices) {

            if (previousInvoice.getId()
                    .equals(invoice.getId())) {

                continue;
            }

            boolean isCompleted =
                    "COMPLETED".equalsIgnoreCase(
                            previousInvoice.getStatus()
                    );

            boolean isCreditSale =
                    "CREDIT".equalsIgnoreCase(
                            previousInvoice.getSaleType()
                    );

            if (isCompleted && isCreditSale) {

                BigDecimal invoiceAmount =
                        zeroIfNull(
                                previousInvoice.getNetAmount()
                        );

                BigDecimal paidAmount =
                        zeroIfNull(
                                paymentRepository
                                        .calculatePaidAmount(
                                                previousInvoice.getId()
                                        )
                        );

                BigDecimal balance =
                        invoiceAmount.subtract(paidAmount);

                if (balance.compareTo(
                        BigDecimal.ZERO) > 0) {

                    currentOutstanding =
                            currentOutstanding.add(balance);
                }
            }
        }

        BigDecimal newInvoiceAmount =
                zeroIfNull(invoice.getNetAmount());

        BigDecimal newTotalOutstanding =
                currentOutstanding.add(
                        newInvoiceAmount
                );

        if (newTotalOutstanding.compareTo(
                creditLimit) > 0) {

            BigDecimal availableCredit =
                    creditLimit.subtract(
                            currentOutstanding
                    );

            if (availableCredit.compareTo(
                    BigDecimal.ZERO) < 0) {

                availableCredit = BigDecimal.ZERO;
            }

            throw new IllegalArgumentException(
                    "Customer credit limit exceeded. "
                            + "Credit limit: Rs. "
                            + creditLimit.toPlainString()
                            + ", current outstanding: Rs. "
                            + currentOutstanding.toPlainString()
                            + ", available credit: Rs. "
                            + availableCredit.toPlainString()
                            + ", this invoice: Rs. "
                            + newInvoiceAmount.toPlainString()
                            + "."
            );
        }
    }


    private String getProductUnit(
            Product product) {

        if (product.getUnit() == null
                || product.getUnit().isBlank()) {

            return "PKT";
        }

        return product.getUnit()
                .trim()
                .toUpperCase();
    }

    private BigDecimal zeroIfNull(
            BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}
