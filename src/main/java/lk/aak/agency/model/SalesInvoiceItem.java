package lk.aak.agency.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_invoice_items")
public class SalesInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "sales_invoice_id",
            nullable = false
    )
    private SalesInvoice salesInvoice;

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
            name = "quantity",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal quantity;

    /*
     * The existing database column name is kept
     * so old invoice data is not lost.
     */
    @Column(
            name = "sales_unit",
            nullable = false
    )
    private String unit;

    @Column(
            name = "unit_price",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Column(
            name = "amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    public SalesInvoiceItem() {
    }

    @PrePersist
    @PreUpdate
    public void calculateAmount() {

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

        amount = quantity.multiply(unitPrice);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SalesInvoice getSalesInvoice() {
        return salesInvoice;
    }

    public void setSalesInvoice(
            SalesInvoice salesInvoice) {

        this.salesInvoice = salesInvoice;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(
            BigDecimal quantity) {

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

    public void setUnitPrice(
            BigDecimal unitPrice) {

        this.unitPrice = unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(
            BigDecimal amount) {

        this.amount = amount;
    }

    /*
     * Compatibility methods for existing controller,
     * service and Thymeleaf files.
     * These can be removed after all sales files
     * have been updated to use unit.
     */

    @Transient
    public String getSalesUnit() {
        return unit;
    }

    public void setSalesUnit(
            String salesUnit) {

        this.unit = salesUnit;
    }
}