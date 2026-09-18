package lk.aak.agency.controller;

import lk.aak.agency.model.Customer;
import lk.aak.agency.model.Payment;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.repository.CustomerRepository;
import lk.aak.agency.repository.PaymentRepository;
import lk.aak.agency.repository.SalesInvoiceRepository;
import lk.aak.agency.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customers")
public class CustomerCreditController {

    private static final ZoneId SRI_LANKA_TIME_ZONE =
            ZoneId.of("Asia/Colombo");

    private final CustomerRepository customerRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public CustomerCreditController(
            CustomerRepository customerRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            PaymentRepository paymentRepository,
            PaymentService paymentService) {

        this.customerRepository = customerRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @GetMapping("/{id}/credit-history")
    public String showCreditHistory(
            @PathVariable Long id,
            Model model) {

        Customer customer = customerRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found: " + id
                ));

        List<SalesInvoice> creditInvoices = salesInvoiceRepository
                .findByCustomerIdOrderByInvoiceDateDesc(id)
                .stream()
                .filter(invoice -> "COMPLETED".equalsIgnoreCase(
                        invoice.getStatus()
                ))
                .filter(invoice -> "CREDIT".equalsIgnoreCase(
                        invoice.getSaleType()
                ))
                .toList();

        List<Payment> payments = paymentRepository
                .findBySalesInvoiceCustomerIdOrderByPaymentDateDesc(id);

        Map<Long, BigDecimal> paidAmountByInvoice =
                new LinkedHashMap<>();

        Map<Long, BigDecimal> balanceByInvoice =
                new LinkedHashMap<>();

        BigDecimal totalCreditSales = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        long overdueInvoiceCount = 0;
        LocalDate today = LocalDate.now(SRI_LANKA_TIME_ZONE);

        for (SalesInvoice invoice : creditInvoices) {
            BigDecimal netAmount = zeroIfNull(invoice.getNetAmount());
            BigDecimal paidAmount = paymentService.getPaidAmount(invoice.getId());
            BigDecimal balance = netAmount.subtract(paidAmount);

            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = BigDecimal.ZERO;
            }

            paidAmountByInvoice.put(invoice.getId(), paidAmount);
            balanceByInvoice.put(invoice.getId(), balance);

            totalCreditSales = totalCreditSales.add(netAmount);
            totalPaid = totalPaid.add(paidAmount);
            totalOutstanding = totalOutstanding.add(balance);

            if (invoice.getDueDate() != null
                    && invoice.getDueDate().isBefore(today)
                    && balance.compareTo(BigDecimal.ZERO) > 0) {
                overdueInvoiceCount++;
            }
        }

        model.addAttribute("customer", customer);
        model.addAttribute("creditInvoices", creditInvoices);
        model.addAttribute("payments", payments);
        model.addAttribute("paidAmountByInvoice", paidAmountByInvoice);
        model.addAttribute("balanceByInvoice", balanceByInvoice);
        model.addAttribute("totalCreditSales", totalCreditSales);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalOutstanding", totalOutstanding);
        model.addAttribute("overdueInvoiceCount", overdueInvoiceCount);
        model.addAttribute("today", today);

        return "customers/customer-credit-history";
    }

    /**
     * Agency-wide "Outstanding and overdue amounts by shop and bill" report - the
     * proposal's own wording for the Credit Follow-up report the owner needs.
     */
    @GetMapping("/credit-followup")
    public String showCreditFollowUp(Model model) {

        LocalDate today = LocalDate.now(SRI_LANKA_TIME_ZONE);

        List<CreditFollowUpRow> rows = new java.util.ArrayList<>();
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;

        for (Customer customer : customerRepository.findAll()) {

            List<SalesInvoice> creditInvoices = salesInvoiceRepository
                    .findByCustomerIdOrderByInvoiceDateDesc(customer.getId())
                    .stream()
                    .filter(invoice -> "COMPLETED".equalsIgnoreCase(invoice.getStatus()))
                    .filter(invoice -> "CREDIT".equalsIgnoreCase(invoice.getSaleType()))
                    .toList();

            BigDecimal customerOutstanding = BigDecimal.ZERO;
            BigDecimal customerOverdue = BigDecimal.ZERO;
            LocalDate oldestOverdueDueDate = null;

            for (SalesInvoice invoice : creditInvoices) {

                BigDecimal netAmount = zeroIfNull(invoice.getNetAmount());
                BigDecimal paidAmount = paymentService.getPaidAmount(invoice.getId());
                BigDecimal balance = netAmount.subtract(paidAmount);

                if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                customerOutstanding = customerOutstanding.add(balance);

                if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(today)) {

                    customerOverdue = customerOverdue.add(balance);

                    if (oldestOverdueDueDate == null || invoice.getDueDate().isBefore(oldestOverdueDueDate)) {
                        oldestOverdueDueDate = invoice.getDueDate();
                    }
                }
            }

            if (customerOutstanding.compareTo(BigDecimal.ZERO) > 0) {

                long daysOverdue = oldestOverdueDueDate == null
                        ? 0
                        : java.time.temporal.ChronoUnit.DAYS.between(oldestOverdueDueDate, today);

                rows.add(new CreditFollowUpRow(
                        customer, customerOutstanding, customerOverdue, oldestOverdueDueDate, daysOverdue
                ));

                totalOutstanding = totalOutstanding.add(customerOutstanding);
                totalOverdue = totalOverdue.add(customerOverdue);
            }
        }

        rows.sort((a, b) -> b.daysOverdue().compareTo(a.daysOverdue()));

        model.addAttribute("rows", rows);
        model.addAttribute("totalOutstanding", totalOutstanding);
        model.addAttribute("totalOverdue", totalOverdue);
        model.addAttribute("today", today);

        return "customers/credit-followup";
    }

    public record CreditFollowUpRow(
            Customer customer,
            BigDecimal totalOutstanding,
            BigDecimal overdueAmount,
            LocalDate oldestOverdueDueDate,
            Long daysOverdue) {
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
