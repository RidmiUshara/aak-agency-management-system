package lk.aak.agency.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "purchase_invoice_items")
public class PurchaseInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "purchase_invoice_id",
            nullable = false
    )
    private PurchaseInvoice purchaseInvoice;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    @Column(
            name = "purchased_quantity",
            precision = 12,
            scale = 2,
            nullable = false
    )
    private BigDecimal quantity;

    @Column(
            name = "purchase_unit",
            nullable = false
    )
    private String unit;

    @Column(
            name = "unit_price",
            precision = 15,
            scale = 2,
            nullable = false
    )
    private BigDecimal unitPrice;

    @Column(
            name = "amount_including_vat",
            precision = 15,
            scale = 2,
            nullable = false
    )
    private BigDecimal amount;

    // Optional - the expiry date of this specific received batch, if known.
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    public PurchaseInvoiceItem() {
    }

    @PrePersist
    @PreUpdate
    public void calculateValues() {

        if (quantity == null) {
            quantity = BigDecimal.ZERO;
        }

        if (unitPrice == null) {
            unitPrice = BigDecimal.ZERO;
        }

        if ((unit == null || unit.isBlank())
                && product != null) {

            unit = product.getUnit();
        }

        if (unit == null || unit.isBlank()) {
            unit = "PKT";
        } else {
            unit = unit.trim().toUpperCase();
        }

        amount = quantity.multiply(unitPrice);
    }

    public void calculateAmount() {
        calculateValues();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PurchaseInvoice getPurchaseInvoice() {
        return purchaseInvoice;
    }

    public void setPurchaseInvoice(
            PurchaseInvoice purchaseInvoice) {

        this.purchaseInvoice = purchaseInvoice;
    }

    public Product getProduct() {
        return product;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setProduct(Product product) {
        this.product = product;

        if (product != null) {
            this.unit = product.getUnit();
        }
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /*
     * Temporary compatibility methods.
     * These keep the existing controller, service and HTML
     * files working until they are updated.
     */

    @Deprecated
    @Transient
    public BigDecimal getNumberOfBoxes() {
        return BigDecimal.ZERO;
    }

    @Deprecated
    public void setNumberOfBoxes(
            BigDecimal numberOfBoxes) {
        // Number of boxes is no longer required.
    }

    @Deprecated
    @Transient
    public BigDecimal getPurchasedQuantity() {
        return quantity;
    }

    @Deprecated
    public void setPurchasedQuantity(
            BigDecimal purchasedQuantity) {

        this.quantity = purchasedQuantity;
    }

    @Deprecated
    @Transient
    public String getPurchaseUnit() {
        return unit;
    }

    @Deprecated
    public void setPurchaseUnit(
            String purchaseUnit) {

        this.unit = purchaseUnit;
    }

    @Deprecated
    @Transient
    public BigDecimal getAmountIncludingVat() {
        return amount;
    }

    @Deprecated
    public void setAmountIncludingVat(
            BigDecimal amountIncludingVat) {

        this.amount = amountIncludingVat;
    }
}