package lk.aak.agency.service;

import lk.aak.agency.model.PurchaseInvoice;
import lk.aak.agency.model.SupplierPayment;
import lk.aak.agency.repository.PurchaseInvoiceRepository;
import lk.aak.agency.repository.SupplierPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierPaymentService {

    private final SupplierPaymentRepository supplierPaymentRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;

    public SupplierPaymentService(
            SupplierPaymentRepository supplierPaymentRepository,
            PurchaseInvoiceRepository purchaseInvoiceRepository) {

        this.supplierPaymentRepository = supplierPaymentRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    }

    public BigDecimal getPaidAmount(Long purchaseInvoiceId) {
        return zeroIfNull(supplierPaymentRepository.calculatePaidAmount(purchaseInvoiceId));
    }

    public BigDecimal getOutstandingBalance(PurchaseInvoice invoice) {

        BigDecimal totalAmount = zeroIfNull(invoice.getTotalAmount());
        BigDecimal paidAmount = getPaidAmount(invoice.getId());
        BigDecimal balance = totalAmount.subtract(paidAmount);

        return balance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : balance;
    }

    public List<SupplierPayment> getPaymentsForInvoice(Long purchaseInvoiceId) {
        return supplierPaymentRepository.findByPurchaseInvoiceIdOrderByPaymentDateDesc(purchaseInvoiceId);
    }

    /**
     * Every completed purchase invoice with an outstanding balance still owed to CBL,
     * plus the total owed across all of them - the CBL supplier balance.
     */
    public SupplierBalanceSummary getSupplierBalanceSummary() {

        List<SupplierBalanceRow> rows = new ArrayList<>();
        BigDecimal totalOwed = BigDecimal.ZERO;

        for (PurchaseInvoice invoice : purchaseInvoiceRepository.findAllByOrderByInvoiceDateDesc()) {

            if (!"COMPLETED".equalsIgnoreCase(invoice.getStatus())) {
                continue;
            }

            BigDecimal outstanding = getOutstandingBalance(invoice);

            if (outstanding.compareTo(BigDecimal.ZERO) > 0) {
                rows.add(new SupplierBalanceRow(invoice, outstanding));
                totalOwed = totalOwed.add(outstanding);
            }
        }

        return new SupplierBalanceSummary(rows, totalOwed);
    }

    @Transactional
    public SupplierPayment recordPayment(SupplierPayment payment) {

        if (payment == null || payment.getPurchaseInvoiceId() == null) {
            throw new IllegalArgumentException("Please select a purchase invoice.");
        }

        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(payment.getPurchaseInvoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Selected purchase invoice was not found."));

        if (!"COMPLETED".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException(
                    "Only a completed purchase invoice can have a supplier payment recorded against it."
            );
        }

        BigDecimal amount = payment.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        BigDecimal outstanding = getOutstandingBalance(invoice);

        if (amount.compareTo(outstanding) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount cannot be greater than the outstanding balance of Rs. "
                            + outstanding.stripTrailingZeros().toPlainString() + "."
            );
        }

        payment.setPurchaseInvoiceDocumentNumber(invoice.getDocumentNumber());

        return supplierPaymentRepository.save(payment);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record SupplierBalanceRow(PurchaseInvoice invoice, BigDecimal outstandingBalance) {
    }

    public record SupplierBalanceSummary(List<SupplierBalanceRow> rows, BigDecimal totalOwed) {
    }
}
