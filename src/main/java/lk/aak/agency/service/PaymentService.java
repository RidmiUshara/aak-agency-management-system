package lk.aak.agency.service;

import lk.aak.agency.model.Payment;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.repository.EmployeeRepository;
import lk.aak.agency.repository.PaymentRepository;
import lk.aak.agency.repository.SalesInvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class PaymentService {

    private static final Set<String>
            ALLOWED_PAYMENT_METHODS = Set.of(
            "CASH",
            "CHEQUE",
            "BANK_TRANSFER"
    );

    private static final Set<String>
            ALLOWED_CHEQUE_STATUSES = Set.of(
            "DEPOSITED",
            "CLEARED",
            "RETURNED"
    );

    private final PaymentRepository paymentRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    public PaymentService(
            PaymentRepository paymentRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            EmployeeRepository employeeRepository,
            AuditLogService auditLogService) {

        this.paymentRepository = paymentRepository;
        this.salesInvoiceRepository =
                salesInvoiceRepository;
        this.employeeRepository = employeeRepository;
        this.auditLogService = auditLogService;
    }

    public List<Payment> getAllPayments() {

        return paymentRepository
                .findAllByOrderByPaymentDateDesc();
    }

    public Page<Payment> getPaymentPage(
            String search, int page, int size) {

        return paymentRepository.search(
                search == null ? "" : search.trim(),
                PageRequest.of(
                        Math.max(page, 0),
                        Math.max(size, 1),
                        Sort.by(Sort.Direction.DESC, "paymentDate")
                )
        );
    }

    public BigDecimal getTotalCollected(String search) {

        return paymentRepository.sumReceivedAmount(
                search == null ? "" : search.trim()
        );
    }

    public long getReceivedPaymentCount(String search) {

        return paymentRepository.countReceived(
                search == null ? "" : search.trim()
        );
    }

    public Payment getPaymentById(Long id) {

        return paymentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found: " + id
                        )
                );
    }

    public List<Payment> getPaymentsByInvoiceId(
            Long invoiceId) {

        return paymentRepository
                .findBySalesInvoiceIdOrderByPaymentDateDesc(
                        invoiceId
                );
    }

    public BigDecimal getPaidAmount(
            Long invoiceId) {

        if (invoiceId == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal paidAmount =
                paymentRepository
                        .calculatePaidAmount(invoiceId);

        return zeroIfNull(paidAmount);
    }

    public BigDecimal getRemainingBalance(
            SalesInvoice invoice) {

        if (invoice == null
                || invoice.getId() == null) {

            return BigDecimal.ZERO;
        }

        BigDecimal netAmount =
                zeroIfNull(invoice.getNetAmount());

        BigDecimal paidAmount =
                getPaidAmount(invoice.getId());

        BigDecimal balance =
                netAmount.subtract(paidAmount);

        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return balance;
    }

    @Transactional
    public Payment savePayment(
            Payment payment) {

        validateBasicPaymentInformation(payment);

        SalesInvoice invoice =
                getValidatedSalesInvoice(payment);

        BigDecimal paymentAmount =
                payment.getAmount();

        BigDecimal currentPaidAmount =
                getPaidAmount(invoice.getId());

        BigDecimal remainingBalance =
                zeroIfNull(invoice.getNetAmount())
                        .subtract(currentPaidAmount);

        if (remainingBalance.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "This sales invoice is already fully paid."
            );
        }

        if (paymentAmount.compareTo(
                remainingBalance) > 0) {

            throw new IllegalArgumentException(
                    "Payment amount cannot be greater "
                            + "than the remaining balance of Rs. "
                            + remainingBalance
                            .stripTrailingZeros()
                            .toPlainString()
                            + "."
            );
        }

        normalizeAndValidatePaymentMethod(payment);

        payment.setSalesInvoice(invoice);
        payment.setStatus("RECEIVED");

        if ("CHEQUE".equals(
                payment.getPaymentMethod())) {

            payment.setChequeStatus("RECEIVED");
            payment.setChequeDepositedDate(null);
            payment.setChequeClearedDate(null);
            payment.setChequeReturnDate(null);
            payment.setChequeReturnReason(null);
        }

        if (payment.getCollectedByEmployeeId() == null) {

            payment.setCollectedByName(null);

        } else {

            payment.setCollectedByName(
                    employeeRepository
                            .findById(payment.getCollectedByEmployeeId())
                            .map(employee -> employee.getFullName())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Selected collector was not found."
                                    )
                            )
            );
        }

        Payment savedPayment =
                paymentRepository.save(payment);

        paymentRepository.flush();

        BigDecimal updatedPaidAmount =
                getPaidAmount(invoice.getId());

        updateInvoicePaymentStatus(
                invoice,
                updatedPaidAmount
        );

        return savedPayment;
    }

    /**
     * Groups a day's field-collected cash/cheque payments by collector, for the office to
     * reconcile what was actually handed in against what was collected out in the field.
     */
    public List<HandoverSummaryRow> getHandoverSummaryForDate(LocalDate date) {

        Map<Long, HandoverSummaryRow> summaryByCollector = new LinkedHashMap<>();

        for (Payment payment
                : paymentRepository.findByPaymentDateOrderByPaymentDateDesc(date)) {

            if (payment.getCollectedByEmployeeId() == null) {
                continue;
            }

            HandoverSummaryRow row = summaryByCollector.computeIfAbsent(
                    payment.getCollectedByEmployeeId(),
                    id -> new HandoverSummaryRow(id, payment.getCollectedByName())
            );

            BigDecimal amount = zeroIfNull(payment.getAmount());

            if ("CHEQUE".equalsIgnoreCase(payment.getPaymentMethod())) {
                row.chequeTotal = row.chequeTotal.add(amount);
            } else {
                row.cashTotal = row.cashTotal.add(amount);
            }

            if (!"HANDED_OVER".equalsIgnoreCase(payment.getHandoverStatus())) {
                row.allHandedOver = false;
            }
        }

        return List.copyOf(summaryByCollector.values());
    }

    @Transactional
    public void markHandedOver(Long collectedByEmployeeId, LocalDate date) {

        List<Payment> payments =
                paymentRepository.findByCollectedByEmployeeIdAndPaymentDate(
                        collectedByEmployeeId, date
                );

        if (payments.isEmpty()) {
            throw new IllegalArgumentException(
                    "No collections were found for this collector on this date."
            );
        }

        for (Payment payment : payments) {
            payment.setHandoverStatus("HANDED_OVER");
            payment.setHandoverDate(LocalDate.now());
            paymentRepository.save(payment);
        }
    }

    public static class HandoverSummaryRow {

        private final Long collectorEmployeeId;
        private final String collectorName;
        private BigDecimal cashTotal = BigDecimal.ZERO;
        private BigDecimal chequeTotal = BigDecimal.ZERO;
        private boolean allHandedOver = true;

        public HandoverSummaryRow(Long collectorEmployeeId, String collectorName) {
            this.collectorEmployeeId = collectorEmployeeId;
            this.collectorName = collectorName;
        }

        public Long getCollectorEmployeeId() {
            return collectorEmployeeId;
        }

        public String getCollectorName() {
            return collectorName;
        }

        public BigDecimal getCashTotal() {
            return cashTotal;
        }

        public BigDecimal getChequeTotal() {
            return chequeTotal;
        }

        public boolean isAllHandedOver() {
            return allHandedOver;
        }
    }

    @Transactional
    public Payment updateChequeStatus(
            Long paymentId,
            String newChequeStatus,
            LocalDate actionDate,
            String returnReason) {

        Payment payment =
                getPaymentById(paymentId);

        if (!"CHEQUE".equalsIgnoreCase(
                payment.getPaymentMethod())) {

            throw new IllegalArgumentException(
                    "Cheque status can only be updated "
                            + "for cheque payments."
            );
        }

        if (newChequeStatus == null
                || newChequeStatus.isBlank()) {

            throw new IllegalArgumentException(
                    "Select a cheque status."
            );
        }

        String normalizedStatus =
                newChequeStatus
                        .trim()
                        .toUpperCase();

        if (!ALLOWED_CHEQUE_STATUSES.contains(
                normalizedStatus)) {

            throw new IllegalArgumentException(
                    "The selected cheque status is invalid."
            );
        }

        String currentChequeStatus =
                payment.getChequeStatus();

        if ("RETURNED".equalsIgnoreCase(
                currentChequeStatus)) {

            throw new IllegalArgumentException(
                    "A returned cheque cannot be updated again."
            );
        }

        LocalDate effectiveDate =
                actionDate == null
                        ? LocalDate.now()
                        : actionDate;

        if ("DEPOSITED".equals(normalizedStatus)) {

            if ("CLEARED".equalsIgnoreCase(
                    currentChequeStatus)) {

                throw new IllegalArgumentException(
                        "A cleared cheque cannot be changed "
                                + "back to deposited."
                );
            }

            payment.setChequeStatus("DEPOSITED");
            payment.setChequeDepositedDate(
                    effectiveDate
            );

            payment.setStatus("RECEIVED");
        }

        if ("CLEARED".equals(normalizedStatus)) {

            payment.setChequeStatus("CLEARED");
            payment.setChequeClearedDate(
                    effectiveDate
            );

            if (payment.getChequeDepositedDate()
                    == null) {

                payment.setChequeDepositedDate(
                        effectiveDate
                );
            }

            payment.setStatus("RECEIVED");
        }

        if ("RETURNED".equals(normalizedStatus)) {

            if (returnReason == null
                    || returnReason.isBlank()) {

                throw new IllegalArgumentException(
                        "Return reason is required "
                                + "for a returned cheque."
                );
            }

            payment.setChequeStatus("RETURNED");
            payment.setChequeReturnDate(
                    effectiveDate
            );

            payment.setChequeReturnReason(
                    returnReason.trim()
            );

            payment.setStatus("RETURNED");
        }

        Payment savedPayment =
                paymentRepository.save(payment);

        paymentRepository.flush();

        SalesInvoice invoice =
                payment.getSalesInvoice();

        if (invoice == null
                || invoice.getId() == null) {

            throw new IllegalArgumentException(
                    "The payment is not connected "
                            + "to a sales invoice."
            );
        }

        BigDecimal updatedPaidAmount =
                getPaidAmount(invoice.getId());

        updateInvoicePaymentStatus(
                invoice,
                updatedPaidAmount
        );

        return savedPayment;
    }

    @Transactional
    public void deletePayment(
            Long paymentId) {

        Payment payment =
                getPaymentById(paymentId);

        SalesInvoice invoice =
                payment.getSalesInvoice();

        if (invoice == null
                || invoice.getId() == null) {

            throw new IllegalArgumentException(
                    "The payment is not connected "
                            + "to a sales invoice."
            );
        }

        paymentRepository.delete(payment);
        paymentRepository.flush();

        BigDecimal remainingPaidAmount =
                getPaidAmount(invoice.getId());

        updateInvoicePaymentStatus(
                invoice,
                remainingPaidAmount
        );

        auditLogService.record(
                "PAYMENT_DELETED", "Payment", paymentId,
                "Deleted payment receipt \"" + payment.getReceiptNumber() + "\""
        );
    }

    private void validateBasicPaymentInformation(
            Payment payment) {

        if (payment == null) {

            throw new IllegalArgumentException(
                    "Payment information is required."
            );
        }

        if (payment.getReceiptNumber() == null
                || payment.getReceiptNumber()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Receipt number is required."
            );
        }

        payment.setReceiptNumber(
                payment.getReceiptNumber()
                        .trim()
        );

        Optional<Payment> existingPayment =
                paymentRepository
                        .findByReceiptNumber(
                                payment.getReceiptNumber()
                        );

        if (existingPayment.isPresent()
                && !existingPayment.get()
                .getId()
                .equals(payment.getId())) {

            throw new IllegalArgumentException(
                    "Receipt number already exists."
            );
        }

        if (payment.getPaymentDate() == null) {

            throw new IllegalArgumentException(
                    "Payment date is required."
            );
        }

        BigDecimal paymentAmount =
                zeroIfNull(payment.getAmount());

        if (paymentAmount.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero."
            );
        }

        payment.setAmount(paymentAmount);

        if (payment.getSalesInvoice() == null
                || payment.getSalesInvoice()
                .getId() == null) {

            throw new IllegalArgumentException(
                    "Select a sales invoice."
            );
        }
    }

    private SalesInvoice getValidatedSalesInvoice(
            Payment payment) {

        SalesInvoice invoice =
                salesInvoiceRepository
                        .findById(
                                payment.getSalesInvoice()
                                        .getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Selected sales invoice "
                                                + "was not found."
                                )
                        );

        if (!"COMPLETED".equalsIgnoreCase(
                invoice.getStatus())) {

            throw new IllegalArgumentException(
                    "Payments can only be recorded "
                            + "for completed sales invoices."
            );
        }

        if (!"CREDIT".equalsIgnoreCase(
                invoice.getSaleType())) {

            throw new IllegalArgumentException(
                    "Payments cannot be recorded here "
                            + "for a cash sales invoice."
            );
        }

        return invoice;
    }

    private void normalizeAndValidatePaymentMethod(
            Payment payment) {

        if (payment.getPaymentMethod() == null
                || payment.getPaymentMethod()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Select a payment method."
            );
        }

        String paymentMethod =
                payment.getPaymentMethod()
                        .trim()
                        .toUpperCase();

        if (!ALLOWED_PAYMENT_METHODS.contains(
                paymentMethod)) {

            throw new IllegalArgumentException(
                    "The selected payment method is invalid."
            );
        }

        payment.setPaymentMethod(paymentMethod);

        if ("CASH".equals(paymentMethod)) {

            clearChequeInformation(payment);
            payment.setBankName(null);
            payment.setReferenceNumber(null);
        }

        if ("CHEQUE".equals(paymentMethod)) {

            validateChequeInformation(payment);
            payment.setReferenceNumber(null);
        }

        if ("BANK_TRANSFER".equals(paymentMethod)) {

            validateBankTransferInformation(payment);
            clearChequeInformation(payment);
        }
    }

    private void validateChequeInformation(
            Payment payment) {

        if (payment.getChequeNumber() == null
                || payment.getChequeNumber()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Cheque number is required "
                            + "for a cheque payment."
            );
        }

        if (payment.getChequeDate() == null) {

            throw new IllegalArgumentException(
                    "Cheque date is required "
                            + "for a cheque payment."
            );
        }

        if (payment.getBankName() == null
                || payment.getBankName()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Bank name is required "
                            + "for a cheque payment."
            );
        }

        payment.setChequeNumber(
                payment.getChequeNumber().trim()
        );

        payment.setBankName(
                payment.getBankName().trim()
        );
    }

    private void validateBankTransferInformation(
            Payment payment) {

        if (payment.getReferenceNumber() == null
                || payment.getReferenceNumber()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction or reference number "
                            + "is required for a bank-transfer payment."
            );
        }

        payment.setReferenceNumber(
                payment.getReferenceNumber().trim()
        );

        if (payment.getBankName() != null
                && !payment.getBankName()
                .isBlank()) {

            payment.setBankName(
                    payment.getBankName().trim()
            );

        } else {

            payment.setBankName(null);
        }
    }

    private void clearChequeInformation(
            Payment payment) {

        payment.setChequeNumber(null);
        payment.setChequeDate(null);
        payment.setChequeStatus(null);
        payment.setChequeDepositedDate(null);
        payment.setChequeClearedDate(null);
        payment.setChequeReturnDate(null);
        payment.setChequeReturnReason(null);
    }

    private void updateInvoicePaymentStatus(
            SalesInvoice invoice,
            BigDecimal paidAmount) {

        BigDecimal netAmount =
                zeroIfNull(invoice.getNetAmount());

        BigDecimal safePaidAmount =
                zeroIfNull(paidAmount);

        if (safePaidAmount.compareTo(
                BigDecimal.ZERO) <= 0) {

            invoice.setPaymentStatus("UNPAID");

        } else if (safePaidAmount.compareTo(
                netAmount) >= 0) {

            invoice.setPaymentStatus("PAID");

        } else {

            invoice.setPaymentStatus(
                    "PARTIALLY_PAID"
            );
        }

        salesInvoiceRepository.save(invoice);
    }

    private BigDecimal zeroIfNull(
            BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}