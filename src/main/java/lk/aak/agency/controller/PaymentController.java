package lk.aak.agency.controller;

import lk.aak.agency.model.Payment;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.repository.SalesInvoiceRepository;
import lk.aak.agency.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final SalesInvoiceRepository salesInvoiceRepository;

    public PaymentController(
            PaymentService paymentService,
            SalesInvoiceRepository salesInvoiceRepository) {

        this.paymentService = paymentService;
        this.salesInvoiceRepository =
                salesInvoiceRepository;
    }

    @GetMapping
    public String showPaymentList(Model model) {

        List<Payment> payments =
                paymentService.getAllPayments();

        BigDecimal totalCollected =
                calculateTotalCollected(payments);

        long receivedPaymentCount =
                payments.stream()
                        .filter(payment ->
                                "RECEIVED".equalsIgnoreCase(
                                        payment.getStatus()
                                )
                        )
                        .count();

        model.addAttribute(
                "payments",
                payments
        );

        model.addAttribute(
                "totalPayments",
                payments.size()
        );

        model.addAttribute(
                "receivedPaymentCount",
                receivedPaymentCount
        );

        model.addAttribute(
                "totalCollected",
                totalCollected
        );

        return "payments/payment-list";
    }

    @GetMapping("/new")
    public String showNewPaymentForm(
            @RequestParam(required = false)
            Long invoiceId,
            Model model) {

        Payment payment = new Payment();

        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod("CASH");
        payment.setStatus("RECEIVED");

        Long selectedInvoiceId = null;

        if (invoiceId != null) {

            SalesInvoice selectedInvoice =
                    salesInvoiceRepository
                            .findById(invoiceId)
                            .orElse(null);

            if (selectedInvoice != null
                    && "COMPLETED".equalsIgnoreCase(
                    selectedInvoice.getStatus())
                    && "CREDIT".equalsIgnoreCase(
                    selectedInvoice.getSaleType())
                    && paymentService
                    .getRemainingBalance(selectedInvoice)
                    .compareTo(BigDecimal.ZERO) > 0) {

                payment.setSalesInvoice(
                        selectedInvoice
                );

                selectedInvoiceId =
                        selectedInvoice.getId();
            }
        }

        List<SalesInvoice> creditInvoices =
                getOutstandingCreditInvoices();

        Map<Long, BigDecimal> paidAmountByInvoice =
                new LinkedHashMap<>();

        Map<Long, BigDecimal> balanceByInvoice =
                new LinkedHashMap<>();

        for (SalesInvoice invoice : creditInvoices) {

            BigDecimal paidAmount =
                    paymentService.getPaidAmount(
                            invoice.getId()
                    );

            BigDecimal balance =
                    paymentService.getRemainingBalance(
                            invoice
                    );

            paidAmountByInvoice.put(
                    invoice.getId(),
                    paidAmount
            );

            balanceByInvoice.put(
                    invoice.getId(),
                    balance
            );
        }

        model.addAttribute(
                "payment",
                payment
        );

        model.addAttribute(
                "creditInvoices",
                creditInvoices
        );

        model.addAttribute(
                "selectedInvoiceId",
                selectedInvoiceId
        );

        model.addAttribute(
                "paidAmountByInvoice",
                paidAmountByInvoice
        );

        model.addAttribute(
                "balanceByInvoice",
                balanceByInvoice
        );

        return "payments/payment-form";
    }

    @PostMapping("/save")
    public String savePayment(
            Payment payment,
            @RequestParam Long salesInvoiceId,
            RedirectAttributes redirectAttributes) {

        try {
            SalesInvoice salesInvoice =
                    salesInvoiceRepository
                            .findById(salesInvoiceId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Selected sales invoice "
                                                    + "was not found."
                                    )
                            );

            payment.setSalesInvoice(
                    salesInvoice
            );

            Payment savedPayment =
                    paymentService.savePayment(payment);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Payment recorded successfully."
            );

            return "redirect:/payments/view/"
                    + savedPayment.getId();

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/payments/new?invoiceId="
                    + salesInvoiceId;
        }
    }

    @GetMapping("/view/{id}")
    public String viewPaymentReceipt(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Payment payment =
                    paymentService.getPaymentById(id);

            SalesInvoice salesInvoice =
                    payment.getSalesInvoice();

            BigDecimal totalPaid =
                    paymentService.getPaidAmount(
                            salesInvoice.getId()
                    );

            BigDecimal remainingBalance =
                    paymentService.getRemainingBalance(
                            salesInvoice
                    );

            model.addAttribute(
                    "payment",
                    payment
            );

            model.addAttribute(
                    "invoice",
                    salesInvoice
            );

            model.addAttribute(
                    "totalPaid",
                    totalPaid
            );

            model.addAttribute(
                    "remainingBalance",
                    remainingBalance
            );

            model.addAttribute(
                    "today",
                    LocalDate.now()
            );

            return "payments/payment-receipt";

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/payments";
        }
    }

    @PostMapping("/{id}/cheque-status")
    public String updateChequeStatus(
            @PathVariable Long id,

            @RequestParam
            String chequeStatus,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate actionDate,

            @RequestParam(required = false)
            String returnReason,

            RedirectAttributes redirectAttributes) {

        try {
            Payment updatedPayment =
                    paymentService.updateChequeStatus(
                            id,
                            chequeStatus,
                            actionDate,
                            returnReason
                    );

            String successMessage =
                    createChequeStatusMessage(
                            updatedPayment.getChequeStatus()
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    successMessage
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/payments/view/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deletePayment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            paymentService.deletePayment(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Payment deleted successfully."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/payments";
    }

    private List<SalesInvoice>
    getOutstandingCreditInvoices() {

        return salesInvoiceRepository
                .findAllByOrderByInvoiceDateDesc()
                .stream()
                .filter(invoice ->
                        "COMPLETED".equalsIgnoreCase(
                                invoice.getStatus()
                        )
                )
                .filter(invoice ->
                        "CREDIT".equalsIgnoreCase(
                                invoice.getSaleType()
                        )
                )
                .filter(invoice ->
                        paymentService
                                .getRemainingBalance(invoice)
                                .compareTo(
                                        BigDecimal.ZERO
                                ) > 0
                )
                .toList();
    }

    private BigDecimal calculateTotalCollected(
            List<Payment> payments) {

        BigDecimal totalCollected =
                BigDecimal.ZERO;

        for (Payment payment : payments) {

            boolean isReceived =
                    "RECEIVED".equalsIgnoreCase(
                            payment.getStatus()
                    );

            if (isReceived
                    && payment.getAmount() != null) {

                totalCollected =
                        totalCollected.add(
                                payment.getAmount()
                        );
            }
        }

        return totalCollected;
    }

    private String createChequeStatusMessage(
            String chequeStatus) {

        if ("DEPOSITED".equalsIgnoreCase(
                chequeStatus)) {

            return "Cheque marked as deposited successfully.";
        }

        if ("CLEARED".equalsIgnoreCase(
                chequeStatus)) {

            return "Cheque marked as cleared successfully.";
        }

        if ("RETURNED".equalsIgnoreCase(
                chequeStatus)) {

            return "Cheque marked as returned. "
                    + "The sales invoice balance was recalculated.";
        }

        return "Cheque status updated successfully.";
    }
}