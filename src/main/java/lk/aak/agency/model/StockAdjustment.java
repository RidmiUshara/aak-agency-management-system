package lk.aak.agency.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustments")
public class StockAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
            name = "adjustment_type",
            nullable = false,
            length = 40
    )
    private String adjustmentType;

    @Column(
            name = "direction",
            nullable = false,
            length = 10
    )
    private String direction;

    @Column(
            name = "quantity",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal quantity;

    @Column(
            name = "adjustment_date",
            nullable = false
    )
    private LocalDateTime adjustmentDate;

    @Column(
            name = "reference_number",
            length = 100
    )
    private String referenceNumber;

    @Column(
            name = "notes",
            columnDefinition = "TEXT"
    )
    private String notes;

    @Column(
            name = "created_by",
            length = 100
    )
    private String createdBy;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public StockAdjustment() {
    }

    @PrePersist
    public void setDefaultValues() {

        if (adjustmentDate == null) {
            adjustmentDate = LocalDateTime.now();
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (quantity == null) {
            quantity = BigDecimal.ZERO;
        }

        if (adjustmentType != null) {
            adjustmentType =
                    adjustmentType.trim().toUpperCase();
        }

        if (direction != null) {
            direction =
                    direction.trim().toUpperCase();
        }

        if (referenceNumber != null) {
            referenceNumber =
                    referenceNumber.trim();
        }

        if (notes != null) {
            notes = notes.trim();
        }

        if (createdBy != null) {
            createdBy = createdBy.trim();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(
            String adjustmentType) {

        this.adjustmentType = adjustmentType;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(
            String direction) {

        this.direction = direction;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(
            BigDecimal quantity) {

        this.quantity = quantity;
    }

    public LocalDateTime getAdjustmentDate() {
        return adjustmentDate;
    }

    public void setAdjustmentDate(
            LocalDateTime adjustmentDate) {

        this.adjustmentDate = adjustmentDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(
            String referenceNumber) {

        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(
            String createdBy) {

        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }
}