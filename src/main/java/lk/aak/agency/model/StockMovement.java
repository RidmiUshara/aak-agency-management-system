package lk.aak.agency.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "stock_movements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_movement_source",
                        columnNames = {
                                "reference_type",
                                "reference_item_id"
                        }
                )
        }
)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "movement_type", nullable = false)
    private String movementType;

    @Column(name = "quantity_change",
            nullable = false,
            precision = 15,
            scale = 2)
    private BigDecimal quantityChange;

    @Column(name = "stock_unit", nullable = false)
    private String stockUnit;

    @Column(name = "reference_type", nullable = false)
    private String referenceType;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "reference_item_id", nullable = false)
    private Long referenceItemId;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public StockMovement() {
    }

    @PrePersist
    public void setDefaultValues() {

        if (quantityChange == null) {
            quantityChange = BigDecimal.ZERO;
        }

        if (stockUnit == null || stockUnit.isBlank()) {
            stockUnit = "PCS";
        }

        if (movementDate == null) {
            movementDate = LocalDateTime.now();
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

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public BigDecimal getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(BigDecimal quantityChange) {
        this.quantityChange = quantityChange;
    }

    public String getStockUnit() {
        return stockUnit;
    }

    public void setStockUnit(String stockUnit) {
        this.stockUnit = stockUnit;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Long getReferenceItemId() {
        return referenceItemId;
    }

    public void setReferenceItemId(Long referenceItemId) {
        this.referenceItemId = referenceItemId;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}