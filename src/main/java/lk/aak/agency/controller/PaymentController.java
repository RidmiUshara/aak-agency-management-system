package lk.aak.agency.controller;

import lk.aak.agency.model.Payment;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.repository.SalesInvoiceRepository;
import lk.aak.agency.service.EmployeeService;
import lk.aak.agency.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    private static final int PAGE_SIZE = 25;

    private final PaymentService paymentService;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final EmployeeService employeeService;

    public PaymentController(
            PaymentService paymentService,
            SalesInvoiceRepository salesInvoiceRepository,
            EmployeeService employeeService) {

        this.paymentService = paymentService;
        this.salesInvoiceRepository =
                salesInvoiceRepository;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String showPaymentList(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Payment> paymentPage =
                paymentService.getPaymentPage(
                        search, page, PAGE_SIZE
                );

        model.addAttribute(
                "payments",
                paymentPage.getContent()
        );

        model.addAttribute(
                "totalPayments",
                paymentPage.getTotalElements()
        );

        model.addAttribute(
                "receivedPaymentCount",
                paymentService.getReceivedPaymentCount(search)
        );

        model.addAttribute(
                "totalCollected",
                paymentService.getTotalCollected(search)
        );

        model.addAttribute("search", search);
        model.addAttribute("currentPage", paymentPage.getNumber());
        model.addAttribute("totalPages", paymentPage.getTotalPages());
        model.addAttribute("totalRecords", paymentPage.getTotalElements());

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

        Long selectedInvoiceId =
                applySelectedInvoice(payment, invoiceId);

        populatePaymentFormModel(model, selectedInvoiceId);

        model.addAttribute("payment", payment);

        return "payments/payment-form";
    }

    private Long applySelectedInvoice(
            Payment payment, Long invoiceId) {

        if (invoiceId == null) {
            return null;
        }

        SalesInvoice selectedInvoice =
                salesInvoiceRepository
                        .findById(invoiceId)
                        .orElse(null);

        if (selectedInvoice == null
                || !"COMPLETED".equalsIgnoreCase(selectedInvoice.getStatus())
                || !"CREDIT".equalsIgnoreCase(selectedInvoice.getSaleType())
                || paymentService.getRemainingBalance(selectedInvoice)
                        .compareTo(BigDecimal.ZERO) <= 0) {

            return null;
        }

        payment.setSalesInvoice(selectedInvoice);

        return selectedInvoice.getId();
    }

    /** Shared by the "new payment" GET view and the "save" POST's validation-failure path. */
    private void populatePaymentFormModel(
            Model model, Long selectedInvoiceId) {

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

        model.addAttribute(
                "fieldCollectors",
                employeeService.getActiveFieldCollectors()
        );
    }

    @PostMapping("/save")
    public String savePayment(
            @Valid Payment payment,
            BindingResult bindingResult,
            @RequestParam Long salesInvoiceId,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            populatePaymentFormModel(model, salesInvoiceId);

            SalesInvoice selectedInvoice =
                    salesInvoiceRepository.findById(salesInvoiceId).orElse(null);

            payment.setSalesInvoice(selectedInvoice);

            return "payments/payment-form";
        }

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