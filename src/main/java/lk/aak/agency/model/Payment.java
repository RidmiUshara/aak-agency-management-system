package lk.aak.agency.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "receipt_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String receiptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sales_invoice_id",
            nullable = false
    )
    private SalesInvoice salesInvoice;

    @Column(
            name = "payment_date",
            nullable = false
    )
    private LocalDate paymentDate;

    @Column(
            name = "amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "payment_method",
            nullable = false,
            length = 30
    )
    private String paymentMethod;

    @Column(
            name = "cheque_number",
            length = 50
    )
    private String chequeNumber;

    @Column(name = "cheque_date")
    private LocalDate chequeDate;

    @Column(
            name = "bank_name",
            length = 100
    )
    private String bankName;

    @Column(
            name = "reference_number",
            length = 100
    )
    private String referenceNumber;

    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private String status;

    @Column(
            name = "cheque_status",
            length = 30
    )
    private String chequeStatus;

    @Column(name = "cheque_deposited_date")
    private LocalDate chequeDepositedDate;

    @Column(name = "cheque_cleared_date")
    private LocalDate chequeClearedDate;

    @Column(name = "cheque_return_date")
    private LocalDate chequeReturnDate;

    @Column(
            name = "cheque_return_reason",
            length = 500
    )
    private String chequeReturnReason;

    @Column(
            name = "notes",
            length = 500
    )
    private String notes;

    // Field staff (sales rep/driver) who physically collected this cash/cheque from the shop. Null = collected directly by office.
    @Column(name = "collected_by_employee_id")
    private Long collectedByEmployeeId;

    @Column(name = "collected_by_name")
    private String collectedByName;

    // PENDING / HANDED_OVER when there is a collector, NOT_APPLICABLE when the office collected it directly.
    @Column(name = "handover_status", length = 30)
    private String handoverStatus;

    @Column(name = "handover_date")
    private LocalDate handoverDate;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public Payment() {
    }

    @PrePersist
    public void prePersist() {

        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        if (paymentMethod == null
                || paymentMethod.isBlank()) {

            paymentMethod = "CASH";
        }

        paymentMethod =
                paymentMethod
                        .trim()
                        .toUpperCase();

        if (status == null
                || status.isBlank()) {

            status = "RECEIVED";
        }

        status =
                status
                        .trim()
                        .toUpperCase();

        if ("CHEQUE".equals(paymentMethod)) {

            if (chequeStatus == null
                    || chequeStatus.isBlank()) {

                chequeStatus = "RECEIVED";
            }

            chequeStatus =
                    chequeStatus
                            .trim()
                            .toUpperCase();

        } else {

            chequeStatus = null;
            chequeDepositedDate = null;
            chequeClearedDate = null;
            chequeReturnDate = null;
            chequeReturnReason = null;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (collectedByEmployeeId == null) {
            handoverStatus = "NOT_APPLICABLE";

        } else if (handoverStatus == null || handoverStatus.isBlank()) {
            handoverStatus = "PENDING";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(
            String receiptNumber) {

        this.receiptNumber = receiptNumber;
    }

    public SalesInvoice getSalesInvoice() {
        return salesInvoice;
    }

    public void setSalesInvoice(
            SalesInvoice salesInvoice) {

        this.salesInvoice = salesInvoice;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(
            LocalDate paymentDate) {

        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(
            BigDecimal amount) {

        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            String paymentMethod) {

        this.paymentMethod = paymentMethod;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(
            String chequeNumber) {

        this.chequeNumber = chequeNumber;
    }

    public LocalDate getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(
            LocalDate chequeDate) {

        this.chequeDate = chequeDate;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(
            String bankName) {

        this.bankName = bankName;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(
            String referenceNumber) {

        this.referenceNumber =
                referenceNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status = status;
    }

    public String getChequeStatus() {
        return chequeStatus;
    }

    public void setChequeStatus(
            String chequeStatus) {

        this.chequeStatus = chequeStatus;
    }

    public LocalDate getChequeDepositedDate() {
        return chequeDepositedDate;
    }

    public void setChequeDepositedDate(
            LocalDate chequeDepositedDate) {

        this.chequeDepositedDate =
                chequeDepositedDate;
    }

    public LocalDate getChequeClearedDate() {
        return chequeClearedDate;
    }

    public void setChequeClearedDate(
            LocalDate chequeClearedDate) {

        this.chequeClearedDate =
                chequeClearedDate;
    }

    public LocalDate getChequeReturnDate() {
        return chequeReturnDate;
    }

    public void setChequeReturnDate(
            LocalDate chequeReturnDate) {

        this.chequeReturnDate =
                chequeReturnDate;
    }

    public String getChequeReturnReason() {
        return chequeReturnReason;
    }

    public void setChequeReturnReason(
            String chequeReturnReason) {

        this.chequeReturnReason =
                chequeReturnReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(
            String notes) {

        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }

    public Long getCollectedByEmployeeId() {
        return collectedByEmployeeId;
    }

    public void setCollectedByEmployeeId(Long collectedByEmployeeId) {
        this.collectedByEmployeeId = collectedByEmployeeId;
    }

    public String getCollectedByName() {
        return collectedByName;
    }

    public void setCollectedByName(String collectedByName) {
        this.collectedByName = collectedByName;
    }

    public String getHandoverStatus() {
        return handoverStatus;
    }

    public void setHandoverStatus(String handoverStatus) {
        this.handoverStatus = handoverStatus;
    }

    public LocalDate getHandoverDate() {
        return handoverDate;
    }

    public void setHandoverDate(LocalDate handoverDate) {
        this.handoverDate = handoverDate;
    }
}