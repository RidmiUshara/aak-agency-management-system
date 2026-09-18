package lk.aak.agency.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Goods AAK sends back to CBL (damaged/expired stock received, wrong item etc.). Reduces
 * warehouse stock via a StockMovement per line item; does not automatically alter the
 * original purchase invoice's totals or supplier balance - tracked here for reporting.
 */
@Entity
@Table(name = "supplier_returns")
public class SupplierReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Optional link back to the original CBL purchase invoice this return relates to.
    @Column(name = "purchase_invoice_id")
    private Long purchaseInvoiceId;

    @Column(name = "purchase_invoice_document_number")
    private String purchaseInvoiceDocumentNumber;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SupplierReturn() {
    }

    @PrePersist
    public void setDefaultValues() {

        if (returnDate == null) {
            returnDate = LocalDate.now();
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

    public Long getPurchaseInvoiceId() {
        return purchaseInvoiceId;
    }

    public void setPurchaseInvoiceId(Long purchaseInvoiceId) {
        this.purchaseInvoiceId = purchaseInvoiceId;
    }

    public String getPurchaseInvoiceDocumentNumber() {
        return purchaseInvoiceDocumentNumber;
    }

    public void setPurchaseInvoiceDocumentNumber(String purchaseInvoiceDocumentNumber) {
        this.purchaseInvoiceDocumentNumber = purchaseInvoiceDocumentNumber;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
