package lk.aak.agency.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_invoices")
public class PurchaseInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "document_number",
            nullable = false,
            unique = true
    )
    private String documentNumber;

    @Column(name = "tax_invoice_number")
    private String taxInvoiceNumber;

    @Column(name = "po_number")
    private String poNumber;

    @Column(
            name = "invoice_date",
            nullable = false
    )
    private LocalDate invoiceDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(
            name = "supplier_name",
            nullable = false
    )
    private String supplierName;

    @Column(name = "territory")
    private String territory;

    @Column(
            name = "place_of_supply",
            nullable = false
    )
    private String placeOfSupply;

    @Column(
            name = "subtotal",
            precision = 15,
            scale = 2
    )
    private BigDecimal subtotal;

    @Column(
            name = "discount_amount",
            precision = 15,
            scale = 2
    )
    private BigDecimal discountAmount;

    @Column(
            name = "vat_amount",
            precision = 15,
            scale = 2
    )
    private BigDecimal vatAmount;

    @Column(
            name = "total_amount",
            precision = 15,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "status")
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(
            name = "invoice_file_original_name",
            length = 255
    )
    private String invoiceFileOriginalName;

    @Column(
            name = "invoice_file_stored_name",
            length = 255
    )
    private String invoiceFileStoredName;

    @Column(
            name = "invoice_file_content_type",
            length = 100
    )
    private String invoiceFileContentType;

    @Column(name = "invoice_file_size")
    private Long invoiceFileSize;

    @Column(name = "invoice_file_uploaded_at")
    private LocalDateTime invoiceFileUploadedAt;

    @Column(
            name = "invoice_verified",
            nullable = false
    )
    private boolean invoiceVerified;

    @Column(name = "invoice_verified_at")
    private LocalDateTime invoiceVerifiedAt;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    public PurchaseInvoice() {
    }

    @PrePersist
    @PreUpdate
    public void setDefaultValues() {

        supplierName =
                "CBL Foods International (Pvt) Ltd";

        placeOfSupply = "Ranala";

        if (documentNumber != null) {
            documentNumber =
                    documentNumber.trim();
        }

        if (taxInvoiceNumber != null) {
            taxInvoiceNumber =
                    taxInvoiceNumber.trim();
        }

        if (poNumber != null) {
            poNumber = poNumber.trim();
        }

        if (territory != null) {
            territory = territory.trim();
        }

        if (paymentMethod != null) {
            paymentMethod =
                    paymentMethod.trim().toUpperCase();
        }

        if (notes != null) {
            notes = notes.trim();
        }

        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }

        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }

        if (vatAmount == null) {
            vatAmount = BigDecimal.ZERO;
        }

        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

        if (status == null || status.isBlank()) {
            status = "DRAFT";
        } else {
            status = status.trim().toUpperCase();
        }

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

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(
            String documentNumber) {

        this.documentNumber = documentNumber;
    }

    public String getTaxInvoiceNumber() {
        return taxInvoiceNumber;
    }

    public void setTaxInvoiceNumber(
            String taxInvoiceNumber) {

        this.taxInvoiceNumber = taxInvoiceNumber;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(
            LocalDate invoiceDate) {

        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(
            LocalDate deliveryDate) {

        this.deliveryDate = deliveryDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(
            String supplierName) {

        this.supplierName =
                "CBL Foods International (Pvt) Ltd";
    }

    public String getTerritory() {
        return territory;
    }

    public void setTerritory(
            String territory) {

        this.territory = territory;
    }

    public String getPlaceOfSupply() {
        return placeOfSupply;
    }

    public void setPlaceOfSupply(
            String placeOfSupply) {

        this.placeOfSupply = "Ranala";
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(
            BigDecimal subtotal) {

        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(
            BigDecimal discountAmount) {

        this.discountAmount = discountAmount;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(
            BigDecimal vatAmount) {

        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount) {

        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            String paymentMethod) {

        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(
            String notes) {

        this.notes = notes;
    }

    public String getInvoiceFileOriginalName() {
        return invoiceFileOriginalName;
    }

    public void setInvoiceFileOriginalName(
            String invoiceFileOriginalName) {

        this.invoiceFileOriginalName =
                invoiceFileOriginalName;
    }

    public String getInvoiceFileStoredName() {
        return invoiceFileStoredName;
    }

    public void setInvoiceFileStoredName(
            String invoiceFileStoredName) {

        this.invoiceFileStoredName =
                invoiceFileStoredName;
    }

    public String getInvoiceFileContentType() {
        return invoiceFileContentType;
    }

    public void setInvoiceFileContentType(
            String invoiceFileContentType) {

        this.invoiceFileContentType =
                invoiceFileContentType;
    }

    public Long getInvoiceFileSize() {
        return invoiceFileSize;
    }

    public void setInvoiceFileSize(
            Long invoiceFileSize) {

        this.invoiceFileSize = invoiceFileSize;
    }

    public LocalDateTime getInvoiceFileUploadedAt() {
        return invoiceFileUploadedAt;
    }

    public void setInvoiceFileUploadedAt(
            LocalDateTime invoiceFileUploadedAt) {

        this.invoiceFileUploadedAt =
                invoiceFileUploadedAt;
    }

    public boolean isInvoiceVerified() {
        return invoiceVerified;
    }

    public void setInvoiceVerified(
            boolean invoiceVerified) {

        this.invoiceVerified = invoiceVerified;
    }

    public LocalDateTime getInvoiceVerifiedAt() {
        return invoiceVerifiedAt;
    }

    public void setInvoiceVerifiedAt(
            LocalDateTime invoiceVerifiedAt) {

        this.invoiceVerifiedAt =
                invoiceVerifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }

    @Transient
    public boolean hasInvoiceFile() {

        return invoiceFileStoredName != null
                && !invoiceFileStoredName.isBlank();
    }

    /*
     * Temporary compatibility methods.
     * These do not create or use database columns.
     */

    @Deprecated
    @Transient
    public String getDeliveryNoteNumber() {
        return null;
    }

    @Deprecated
    public void setDeliveryNoteNumber(
            String deliveryNoteNumber) {
        // Delivery note number is no longer required.
    }

    @Deprecated
    @Transient
    public BigDecimal getTotalBoxes() {
        return BigDecimal.ZERO;
    }

    @Deprecated
    public void setTotalBoxes(
            BigDecimal totalBoxes) {
        // BOX calculations are no longer required.
    }
}