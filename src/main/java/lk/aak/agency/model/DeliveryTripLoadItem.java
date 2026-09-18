package lk.aak.agency.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Records what was actually loaded onto the vehicle for a trip, confirmed against the
 * planned loading summary. Warehouse stock is already debited when a sales invoice is
 * completed, so this does not create any further stock movements - it exists purely to
 * confirm/adjust the planned quantity and to answer "what is currently on this vehicle".
 */
@Entity
@Table(name = "delivery_trip_load_items")
public class DeliveryTripLoadItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_trip_id", nullable = false)
    private Long deliveryTripId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "unit")
    private String unit;

    @Column(name = "planned_quantity", nullable = false, precision = 15, scale = 2)
    private BigDecimal plannedQuantity;

    @Column(name = "loaded_quantity", nullable = false, precision = 15, scale = 2)
    private BigDecimal loadedQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DeliveryTripLoadItem() {
    }

    @PrePersist
    public void setDefaultValues() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeliveryTripId() {
        return deliveryTripId;
    }

    public void setDeliveryTripId(Long deliveryTripId) {
        this.deliveryTripId = deliveryTripId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public BigDecimal getLoadedQuantity() {
        return loadedQuantity;
    }

    public void setLoadedQuantity(BigDecimal loadedQuantity) {
        this.loadedQuantity = loadedQuantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
