package lk.aak.agency.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_invoices")
public class SalesInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number",
            nullable = false,
            unique = true)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "route_code")
    private String routeCode;

    @Column(name = "gross_amount",
            precision = 15,
            scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "discount_amount",
            precision = 15,
            scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "return_amount",
            precision = 15,
            scale = 2)
    private BigDecimal returnAmount;

    @Column(name = "net_amount",
            precision = 15,
            scale = 2)
    private BigDecimal netAmount;

    @Column(name = "sale_type")
    private String saleType;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "status")
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Recorded when the Owner overrides a blocked over-credit-limit completion.
    @Column(name = "credit_override_approved_by")
    private String creditOverrideApprovedBy;

    @Column(name = "credit_override_reason", length = 500)
    private String creditOverrideReason;

    @Column(name = "credit_override_at")
    private LocalDateTime creditOverrideAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Set once this bill is assigned to a delivery trip (Stage 3: Distribution). Null = not yet assigned.
    @Column(name = "delivery_trip_id")
    private Long deliveryTripId;

    // PENDING / COMPLETED / PARTIAL / UNSUCCESSFUL - only meaningful once deliveryTripId is set.
    @Column(name = "delivery_status")
    private String deliveryStatus;

    public SalesInvoice() {
    }

    @PrePersist
    public void setDefaultValues() {

        if (invoiceDate == null) {
            invoiceDate = LocalDate.now();
        }

        if (grossAmount == null) {
            grossAmount = BigDecimal.ZERO;
        }

        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }

        if (returnAmount == null) {
            returnAmount = BigDecimal.ZERO;
        }

        if (netAmount == null) {
            netAmount = BigDecimal.ZERO;
        }

        if (saleType == null || saleType.isBlank()) {
            saleType = "CREDIT";
        }

        if (paymentStatus == null || paymentStatus.isBlank()) {
            paymentStatus = "UNPAID";
        }

        if (status == null || status.isBlank()) {
            status = "DRAFT";
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

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getSaleType() {
        return saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
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

    public String getCreditOverrideApprovedBy() {
        return creditOverrideApprovedBy;
    }

    public void setCreditOverrideApprovedBy(String creditOverrideApprovedBy) {
        this.creditOverrideApprovedBy = creditOverrideApprovedBy;
    }

    public String getCreditOverrideReason() {
        return creditOverrideReason;
    }

    public void setCreditOverrideReason(String creditOverrideReason) {
        this.creditOverrideReason = creditOverrideReason;
    }

    public LocalDateTime getCreditOverrideAt() {
        return creditOverrideAt;
    }

    public void setCreditOverrideAt(LocalDateTime creditOverrideAt) {
        this.creditOverrideAt = creditOverrideAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getDeliveryTripId() {
        return deliveryTripId;
    }

    public void setDeliveryTripId(Long deliveryTripId) {
        this.deliveryTripId = deliveryTripId;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}