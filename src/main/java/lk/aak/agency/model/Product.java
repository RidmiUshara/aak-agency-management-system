package lk.aak.agency.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "CBL product code is required.")
    @Size(max = 100, message = "CBL product code must be 100 characters or fewer.")
    @Column(
            name = "cbl_product_code",
            nullable = false,
            unique = true
    )
    private String cblProductCode;

    @NotBlank(message = "Product name is required.")
    @Size(max = 200, message = "Product name must be 200 characters or fewer.")
    @Column(
            name = "product_name",
            nullable = false
    )
    private String productName;

    @Column(name = "brand")
    private String brand;

    @Column(name = "category")
    private String category;

    @Column(name = "net_weight")
    private String netWeight;

    @NotBlank(message = "Unit is required.")
    @Column(
            name = "unit",
            nullable = false
    )
    private String unit;

    @DecimalMin(value = "0", message = "MRP cannot be negative.")
    @Column(
            name = "mrp",
            precision = 12,
            scale = 2
    )
    private BigDecimal mrp;

    @DecimalMin(value = "0", message = "Standard selling price cannot be negative.")
    @Column(
            name = "standard_selling_price",
            precision = 12,
            scale = 2
    )
    private BigDecimal standardSellingPrice;

    @DecimalMin(value = "0", message = "Reorder level cannot be negative.")
    @Column(
            name = "reorder_level",
            precision = 15,
            scale = 2
    )
    private BigDecimal reorderLevel;

    @Column(name = "status")
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Product() {
    }

    @PrePersist
    @PreUpdate
    public void setDefaultValues() {

        if (cblProductCode != null) {
            cblProductCode =
                    cblProductCode.trim().toUpperCase();
        }

        if (productName != null) {
            productName = productName.trim();
        }

        if (brand != null) {
            brand = brand.trim();
        }

        if (category != null) {
            category = category.trim();
        }

        if (netWeight != null) {
            netWeight = netWeight.trim();
        }

        if (unit == null || unit.isBlank()) {
            unit = "PKT";
        } else {
            unit = unit.trim().toUpperCase();
        }

        if (mrp == null) {
            mrp = BigDecimal.ZERO;
        }

        if (standardSellingPrice == null) {
            standardSellingPrice = BigDecimal.ZERO;
        }

        if (reorderLevel == null
                || reorderLevel.compareTo(
                BigDecimal.ZERO) < 0) {

            reorderLevel = BigDecimal.ZERO;
        }

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        } else {
            status = status.trim().toUpperCase();
        }

        if (notes != null) {
            notes = notes.trim();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCblProductCode() {
        return cblProductCode;
    }

    public void setCblProductCode(
            String cblProductCode) {

        this.cblProductCode = cblProductCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(
            String productName) {

        this.productName = productName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(String netWeight) {
        this.netWeight = netWeight;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public BigDecimal getStandardSellingPrice() {
        return standardSellingPrice;
    }

    public void setStandardSellingPrice(
            BigDecimal standardSellingPrice) {

        this.standardSellingPrice =
                standardSellingPrice;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(
            BigDecimal reorderLevel) {

        this.reorderLevel = reorderLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Transient
    public String getDisplayName() {

        if (netWeight == null || netWeight.isBlank()) {
            return productName;
        }

        return productName + " - " + netWeight;
    }

    /*
     * Temporary compatibility methods.
     * Purchase, sales and inventory files are still using
     * the old salesUnit and purchaseUnit method names.
     */

    @Deprecated
    @Transient
    public String getSalesUnit() {
        return unit;
    }

    @Deprecated
    public void setSalesUnit(String salesUnit) {
        this.unit = salesUnit;
    }

    @Deprecated
    @Transient
    public String getPurchaseUnit() {
        return unit;
    }

    @Deprecated
    public void setPurchaseUnit(String purchaseUnit) {
        this.unit = purchaseUnit;
    }

    @Deprecated
    @Transient
    public Integer getPiecesPerPurchaseUnit() {
        return 1;
    }

    @Deprecated
    public void setPiecesPerPurchaseUnit(
            Integer piecesPerPurchaseUnit) {
        // No longer required.
    }

    @Deprecated
    @Transient
    public Integer getPurchaseUnitsPerBox() {
        return 1;
    }

    @Deprecated
    public void setPurchaseUnitsPerBox(
            Integer purchaseUnitsPerBox) {
        // No longer required.
    }

    @Deprecated
    @Transient
    public String getPackDescription() {
        return null;
    }

    @Deprecated
    public void setPackDescription(
            String packDescription) {
        // No longer required.
    }
}
