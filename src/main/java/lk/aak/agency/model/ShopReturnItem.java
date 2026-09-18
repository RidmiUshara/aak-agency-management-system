package lk.aak.agency.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "shop_return_items")
public class ShopReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_return_id", nullable = false)
    private Long shopReturnId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit")
    private String unit;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    public ShopReturnItem() {
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

        amount = quantity.multiply(unitPrice);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShopReturnId() {
        return shopReturnId;
    }

    public void setShopReturnId(Long shopReturnId) {
        this.shopReturnId = shopReturnId;
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
}
