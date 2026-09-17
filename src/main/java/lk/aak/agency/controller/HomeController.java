package lk.aak.agency.controller;

import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.model.Product;
import lk.aak.agency.model.SystemUser;
import lk.aak.agency.repository.CustomerRepository;
import lk.aak.agency.repository.PaymentRepository;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.repository.PurchaseInvoiceRepository;
import lk.aak.agency.repository.SalesInvoiceRepository;
import lk.aak.agency.repository.StockMovementRepository;
import lk.aak.agency.repository.SystemUserRepository;
import lk.aak.agency.service.InventoryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PaymentRepository paymentRepository;
    private final SystemUserRepository systemUserRepository;
    private final InventoryService inventoryService;

    public HomeController(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            PurchaseInvoiceRepository purchaseInvoiceRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            StockMovementRepository stockMovementRepository,
            PaymentRepository paymentRepository,
            SystemUserRepository systemUserRepository,
            InventoryService inventoryService) {

        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.paymentRepository = paymentRepository;
        this.systemUserRepository = systemUserRepository;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/")
    public String showHomePage(
            Model model,
            Authentication authentication) {

        model.addAttribute(
                "totalCustomers",
                customerRepository.count()
        );

        model.addAttribute(
                "totalProducts",
                productRepository.count()
        );

        model.addAttribute(
                "totalPurchaseInvoices",
                purchaseInvoiceRepository.count()
        );

        model.addAttribute(
                "totalSalesInvoices",
                salesInvoiceRepository.count()
        );

        model.addAttribute(
                "totalStockMovements",
                stockMovementRepository.count()
        );

        model.addAttribute(
                "totalPayments",
                paymentRepository.count()
        );

        List<SalesInvoice> invoices =
                salesInvoiceRepository.findAll();

        model.addAttribute(
                "outstandingCredit",
                calculateOutstandingCredit(invoices)
        );

        addOverdueCreditInformation(
                model,
                invoices
        );

        addLowStockInformation(model);

        addLoggedInUserInformation(
                model,
                authentication
        );

        return "home";
    }

    private void addLowStockInformation(
            Model model) {

        List<Product> stockAlertProducts =
                inventoryService.getLowStockProducts();

        List<Product> outOfStockProducts =
                inventoryService.getOutOfStockProducts();

        model.addAttribute(
                "stockAlertProducts",
                stockAlertProducts
        );

        model.addAttribute(
                "stockByProduct",
                inventoryService.getStockByProduct()
        );

        model.addAttribute(
                "shortageByProduct",
                inventoryService.getShortageByProduct()
        );

        model.addAttribute(
                "totalLowStockProducts",
                stockAlertProducts.size()
                        - outOfStockProducts.size()
        );

        model.addAttribute(
                "totalOutOfStockProducts",
                outOfStockProducts.size()
        );

        model.addAttribute(
                "totalStockAlerts",
                stockAlertProducts.size()
        );
    }

    private void addLoggedInUserInformation(
            Model model,
            Authentication authentication) {

        String username =
                authentication == null
                        ? "User"
                        : authentication.getName();

        SystemUser systemUser =
                systemUserRepository
                        .findByUsername(username)
                        .orElse(null);

        if (systemUser != null) {

            model.addAttribute(
                    "loggedInName",
                    systemUser.getFullName()
            );

            model.addAttribute(
                    "loggedInUsername",
                    systemUser.getUsername()
            );

            model.addAttribute(
                    "loggedInRole",
                    systemUser.getRole()
            );

        } else {

            model.addAttribute(
                    "loggedInName",
                    username
            );

            model.addAttribute(
                    "loggedInUsername",
                    username
            );

            model.addAttribute(
                    "loggedInRole",
                    "USER"
            );
        }
    }

    private BigDecimal calculateOutstandingCredit(
            List<SalesInvoice> invoices) {

        BigDecimal outstandingCredit =
                BigDecimal.ZERO;

        for (SalesInvoice invoice : invoices) {

            if (!isCompletedCreditInvoice(invoice)) {
                continue;
            }

            BigDecimal remainingBalance =
                    calculateRemainingBalance(invoice);

            if (remainingBalance.compareTo(
                    BigDecimal.ZERO) > 0) {

                outstandingCredit =
                        outstandingCredit.add(
                                remainingBalance
                        );
            }
        }

        return outstandingCredit;
    }

    private void addOverdueCreditInformation(
            Model model,
            List<SalesInvoice> invoices) {

        List<SalesInvoice> overdueInvoices =
                new ArrayList<>();

        Map<Long, BigDecimal> overdueBalanceByInvoice =
                new HashMap<>();

        Map<Long, Long> daysOverdueByInvoice =
                new HashMap<>();

        BigDecimal totalOverdueAmount =
                BigDecimal.ZERO;

        LocalDate today = LocalDate.now();

        for (SalesInvoice invoice : invoices) {

            if (!isCompletedCreditInvoice(invoice)) {
                continue;
            }

            if (invoice.getDueDate() == null
                    || !invoice.getDueDate().isBefore(today)) {

                continue;
            }

            BigDecimal remainingBalance =
                    calculateRemainingBalance(invoice);

            if (remainingBalance.compareTo(
                    BigDecimal.ZERO) > 0) {

                overdueInvoices.add(invoice);

                overdueBalanceByInvoice.put(
                        invoice.getId(),
                        remainingBalance
                );

                daysOverdueByInvoice.put(
                        invoice.getId(),
                        ChronoUnit.DAYS.between(
                                invoice.getDueDate(),
                                today
                        )
                );

                totalOverdueAmount =
                        totalOverdueAmount.add(
                                remainingBalance
                        );
            }
        }

        overdueInvoices.sort(
                (firstInvoice, secondInvoice) ->
                        firstInvoice.getDueDate()
                                .compareTo(
                                        secondInvoice.getDueDate()
                                )
        );

        model.addAttribute(
                "overdueInvoices",
                overdueInvoices
        );

        model.addAttribute(
                "overdueBalanceByInvoice",
                overdueBalanceByInvoice
        );

        model.addAttribute(
                "daysOverdueByInvoice",
                daysOverdueByInvoice
        );

        model.addAttribute(
                "totalOverdueInvoices",
                overdueInvoices.size()
        );

        model.addAttribute(
                "totalOverdueAmount",
                totalOverdueAmount
        );
    }

    private boolean isCompletedCreditInvoice(
            SalesInvoice invoice) {

        boolean isCompleted =
                "COMPLETED".equalsIgnoreCase(
                        invoice.getStatus()
                );

        boolean isCreditSale =
                "CREDIT".equalsIgnoreCase(
                        invoice.getSaleType()
                );

        return isCompleted && isCreditSale;
    }

    private BigDecimal calculateRemainingBalance(
            SalesInvoice invoice) {

        BigDecimal netAmount =
                invoice.getNetAmount() == null
                        ? BigDecimal.ZERO
                        : invoice.getNetAmount();

        BigDecimal paidAmount =
                paymentRepository.calculatePaidAmount(
                        invoice.getId()
                );

        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }

        BigDecimal remainingBalance =
                netAmount.subtract(paidAmount);

        if (remainingBalance.compareTo(
                BigDecimal.ZERO) < 0) {

            return BigDecimal.ZERO;
        }

        return remainingBalance;
    }
}
