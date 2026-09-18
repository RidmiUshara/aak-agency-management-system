package lk.aak.agency.controller;

import lk.aak.agency.model.Payment;
import lk.aak.agency.model.Product;
import lk.aak.agency.model.PurchaseInvoice;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.model.ShopReturn;
import lk.aak.agency.model.SupplierReturn;
import lk.aak.agency.repository.PaymentRepository;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.repository.PurchaseInvoiceRepository;
import lk.aak.agency.repository.SalesInvoiceRepository;
import lk.aak.agency.repository.StockMovementRepository;
import lk.aak.agency.service.EmployeeAdvanceService;
import lk.aak.agency.service.EmployeeSalaryService;
import lk.aak.agency.service.ShopReturnService;
import lk.aak.agency.service.SupplierPaymentService;
import lk.aak.agency.service.SupplierReturnService;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private static final BigDecimal LOW_STOCK_LIMIT =
            new BigDecimal("10");

    private final PurchaseInvoiceRepository
            purchaseInvoiceRepository;

    private final SalesInvoiceRepository
            salesInvoiceRepository;

    private final PaymentRepository
            paymentRepository;

    private final ProductRepository
            productRepository;

    private final StockMovementRepository
            stockMovementRepository;

    private final ShopReturnService shopReturnService;
    private final SupplierReturnService supplierReturnService;
    private final SupplierPaymentService supplierPaymentService;
    private final EmployeeSalaryService employeeSalaryService;
    private final EmployeeAdvanceService employeeAdvanceService;

    public ReportController(
            PurchaseInvoiceRepository purchaseInvoiceRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            PaymentRepository paymentRepository,
            ProductRepository productRepository,
            StockMovementRepository stockMovementRepository,
            ShopReturnService shopReturnService,
            SupplierReturnService supplierReturnService,
            SupplierPaymentService supplierPaymentService,
            EmployeeSalaryService employeeSalaryService,
            EmployeeAdvanceService employeeAdvanceService) {

        this.purchaseInvoiceRepository =
                purchaseInvoiceRepository;

        this.salesInvoiceRepository =
                salesInvoiceRepository;

        this.paymentRepository =
                paymentRepository;

        this.productRepository =
                productRepository;

        this.stockMovementRepository =
                stockMovementRepository;

        this.shopReturnService = shopReturnService;
        this.supplierReturnService = supplierReturnService;
        this.supplierPaymentService = supplierPaymentService;
        this.employeeSalaryService = employeeSalaryService;
        this.employeeAdvanceService = employeeAdvanceService;
    }

    @GetMapping
    public String showReports(
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate,

            Model model) {

        boolean invalidDateRange =
                fromDate != null
                        && toDate != null
                        && fromDate.isAfter(toDate);

        List<PurchaseInvoice> purchaseInvoices;
        List<SalesInvoice> salesInvoices;
        List<Payment> payments;

        if (invalidDateRange) {

            model.addAttribute(
                    "errorMessage",
                    "From date cannot be after the To date."
            );

            purchaseInvoices =
                    new ArrayList<>();

            salesInvoices =
                    new ArrayList<>();

            payments =
                    new ArrayList<>();

        } else {

            purchaseInvoices =
                    filterPurchaseInvoices(
                            purchaseInvoiceRepository
                                    .findAllByOrderByInvoiceDateDesc(),
                            fromDate,
                            toDate
                    );

            salesInvoices =
                    filterSalesInvoices(
                            salesInvoiceRepository
                                    .findAllByOrderByInvoiceDateDesc(),
                            fromDate,
                            toDate
                    );

            payments =
                    filterPayments(
                            paymentRepository
                                    .findAllByOrderByPaymentDateDesc(),
                            fromDate,
                            toDate
                    );
        }

        InventoryReportData inventoryReportData =
                createInventoryReport();

        model.addAttribute(
                "purchaseInvoices",
                purchaseInvoices
        );

        model.addAttribute(
                "salesInvoices",
                salesInvoices
        );

        model.addAttribute(
                "payments",
                payments
        );

        model.addAttribute(
                "totalPurchases",
                calculateTotalPurchases(
                        purchaseInvoices
                )
        );

        model.addAttribute(
                "totalSales",
                calculateTotalSales(
                        salesInvoices
                )
        );

        model.addAttribute(
                "totalPayments",
                calculateTotalPayments(
                        payments
                )
        );

        model.addAttribute(
                "outstandingCredit",
                calculateOutstandingCredit(
                        salesInvoices
                )
        );

        model.addAttribute(
                "completedPurchaseCount",
                countCompletedPurchases(
                        purchaseInvoices
                )
        );

        model.addAttribute(
                "verifiedPurchaseCount",
                countVerifiedPurchases(
                        purchaseInvoices
                )
        );

        model.addAttribute(
                "completedSalesCount",
                countCompletedSales(
                        salesInvoices
                )
        );

        model.addAttribute(
                "receivedPaymentCount",
                countReceivedPayments(
                        payments
                )
        );

        model.addAttribute(
                "lowStockProducts",
                inventoryReportData.lowStockProducts()
        );

        model.addAttribute(
                "outOfStockProducts",
                inventoryReportData.outOfStockProducts()
        );

        model.addAttribute(
                "stockByProduct",
                inventoryReportData.stockByProduct()
        );

        model.addAttribute(
                "lowStockCount",
                inventoryReportData
                        .lowStockProducts()
                        .size()
        );

        model.addAttribute(
                "outOfStockCount",
                inventoryReportData
                        .outOfStockProducts()
                        .size()
        );

        model.addAttribute(
                "fromDate",
                fromDate
        );

        model.addAttribute(
                "toDate",
                toDate
        );

        model.addAttribute(
                "reportGeneratedAt",
                LocalDateTime.now()
        );

        model.addAttribute(
                "totalShopReturnsValue",
                calculateShopReturnsValue(fromDate, toDate)
        );

        model.addAttribute(
                "totalSupplierReturnsValue",
                calculateSupplierReturnsValue(fromDate, toDate)
        );

        model.addAttribute(
                "totalOwedToSupplier",
                supplierPaymentService.getSupplierBalanceSummary().totalOwed()
        );

        model.addAttribute(
                "totalSalaryPaid",
                calculateSalaryPaid(fromDate, toDate)
        );

        model.addAttribute(
                "totalOutstandingAdvances",
                calculateOutstandingAdvances()
        );

        return "reports/report-dashboard";
    }

    private BigDecimal calculateShopReturnsValue(LocalDate fromDate, LocalDate toDate) {

        BigDecimal total = BigDecimal.ZERO;

        for (ShopReturn shopReturn : shopReturnService.getAllReturns()) {

            if (!isDateWithinRange(shopReturn.getReturnDate(), fromDate, toDate)) {
                continue;
            }

            for (var item : shopReturnService.getItemsForReturn(shopReturn.getId())) {
                total = total.add(item.getAmount());
            }
        }

        return total;
    }

    private BigDecimal calculateSupplierReturnsValue(LocalDate fromDate, LocalDate toDate) {

        BigDecimal total = BigDecimal.ZERO;

        for (SupplierReturn supplierReturn : supplierReturnService.getAllReturns()) {

            if (!isDateWithinRange(supplierReturn.getReturnDate(), fromDate, toDate)) {
                continue;
            }

            for (var item : supplierReturnService.getItemsForReturn(supplierReturn.getId())) {
                total = total.add(item.getAmount());
            }
        }

        return total;
    }

    private BigDecimal calculateSalaryPaid(LocalDate fromDate, LocalDate toDate) {

        BigDecimal total = BigDecimal.ZERO;

        for (var payment : employeeSalaryService.getAllPayments()) {

            if (!isDateWithinRange(payment.getPaymentDate(), fromDate, toDate)) {
                continue;
            }

            total = total.add(payment.getNetPaid());
        }

        return total;
    }

    private BigDecimal calculateOutstandingAdvances() {

        BigDecimal total = BigDecimal.ZERO;

        for (var advance : employeeAdvanceService.getAllAdvances()) {

            if (!advance.isSettled()) {
                total = total.add(advance.getAmount());
            }
        }

        return total;
    }

    private List<PurchaseInvoice>
    filterPurchaseInvoices(
            List<PurchaseInvoice> invoices,
            LocalDate fromDate,
            LocalDate toDate) {

        List<PurchaseInvoice> filteredInvoices =
                new ArrayList<>();

        for (PurchaseInvoice invoice : invoices) {

            if (isDateWithinRange(
                    invoice.getInvoiceDate(),
                    fromDate,
                    toDate)) {

                filteredInvoices.add(invoice);
            }
        }

        return filteredInvoices;
    }

    private List<SalesInvoice>
    filterSalesInvoices(
            List<SalesInvoice> invoices,
            LocalDate fromDate,
            LocalDate toDate) {

        List<SalesInvoice> filteredInvoices =
                new ArrayList<>();

        for (SalesInvoice invoice : invoices) {

            if (isDateWithinRange(
                    invoice.getInvoiceDate(),
                    fromDate,
                    toDate)) {

                filteredInvoices.add(invoice);
            }
        }

        return filteredInvoices;
    }

    private List<Payment> filterPayments(
            List<Payment> payments,
            LocalDate fromDate,
            LocalDate toDate) {

        List<Payment> filteredPayments =
                new ArrayList<>();

        for (Payment payment : payments) {

            if (isDateWithinRange(
                    payment.getPaymentDate(),
                    fromDate,
                    toDate)) {

                filteredPayments.add(payment);
            }
        }

        return filteredPayments;
    }

    private boolean isDateWithinRange(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate) {

        if (date == null) {
            return false;
        }

        boolean afterOrEqualFromDate =
                fromDate == null
                        || !date.isBefore(fromDate);

        boolean beforeOrEqualToDate =
                toDate == null
                        || !date.isAfter(toDate);

        return afterOrEqualFromDate
                && beforeOrEqualToDate;
    }

    private BigDecimal calculateTotalPurchases(
            List<PurchaseInvoice> invoices) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (PurchaseInvoice invoice : invoices) {

            if ("COMPLETED".equalsIgnoreCase(
                    invoice.getStatus())) {

                total = total.add(
                        zeroIfNull(
                                invoice.getTotalAmount()
                        )
                );
            }
        }

        return total;
    }

    private BigDecimal calculateTotalSales(
            List<SalesInvoice> invoices) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (SalesInvoice invoice : invoices) {

            if ("COMPLETED".equalsIgnoreCase(
                    invoice.getStatus())) {

                total = total.add(
                        zeroIfNull(
                                invoice.getNetAmount()
                        )
                );
            }
        }

        return total;
    }

    private BigDecimal calculateTotalPayments(
            List<Payment> payments) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (Payment payment : payments) {

            if ("RECEIVED".equalsIgnoreCase(
                    payment.getStatus())) {

                total = total.add(
                        zeroIfNull(
                                payment.getAmount()
                        )
                );
            }
        }

        return total;
    }

    private BigDecimal calculateOutstandingCredit(
            List<SalesInvoice> invoices) {

        BigDecimal totalOutstanding =
                BigDecimal.ZERO;

        for (SalesInvoice invoice : invoices) {

            boolean completed =
                    "COMPLETED".equalsIgnoreCase(
                            invoice.getStatus()
                    );

            boolean creditSale =
                    "CREDIT".equalsIgnoreCase(
                            invoice.getSaleType()
                    );

            if (completed && creditSale) {

                BigDecimal netAmount =
                        zeroIfNull(
                                invoice.getNetAmount()
                        );

                BigDecimal paidAmount =
                        zeroIfNull(
                                paymentRepository
                                        .calculatePaidAmount(
                                                invoice.getId()
                                        )
                        );

                BigDecimal balance =
                        netAmount.subtract(
                                paidAmount
                        );

                if (balance.compareTo(
                        BigDecimal.ZERO) > 0) {

                    totalOutstanding =
                            totalOutstanding.add(
                                    balance
                            );
                }
            }
        }

        return totalOutstanding;
    }

    private long countCompletedPurchases(
            List<PurchaseInvoice> invoices) {

        long count = 0;

        for (PurchaseInvoice invoice : invoices) {

            if ("COMPLETED".equalsIgnoreCase(
                    invoice.getStatus())) {

                count++;
            }
        }

        return count;
    }

    private long countVerifiedPurchases(
            List<PurchaseInvoice> invoices) {

        long count = 0;

        for (PurchaseInvoice invoice : invoices) {

            if (invoice.isInvoiceVerified()) {
                count++;
            }
        }

        return count;
    }

    private long countCompletedSales(
            List<SalesInvoice> invoices) {

        long count = 0;

        for (SalesInvoice invoice : invoices) {

            if ("COMPLETED".equalsIgnoreCase(
                    invoice.getStatus())) {

                count++;
            }
        }

        return count;
    }

    private long countReceivedPayments(
            List<Payment> payments) {

        long count = 0;

        for (Payment payment : payments) {

            if ("RECEIVED".equalsIgnoreCase(
                    payment.getStatus())) {

                count++;
            }
        }

        return count;
    }

    private InventoryReportData createInventoryReport() {

        List<Product> products =
                productRepository.findAll(
                        Sort.by(
                                Sort.Order.asc(
                                        "productName"
                                ),
                                Sort.Order.asc(
                                        "netWeight"
                                )
                        )
                );

        List<Product> lowStockProducts =
                new ArrayList<>();

        List<Product> outOfStockProducts =
                new ArrayList<>();

        Map<Long, BigDecimal> stockByProduct =
                new LinkedHashMap<>();

        for (Product product : products) {

            BigDecimal currentStock =
                    zeroIfNull(
                            stockMovementRepository
                                    .calculateCurrentStock(
                                            product.getId()
                                    )
                    );

            stockByProduct.put(
                    product.getId(),
                    currentStock
            );

            if (currentStock.compareTo(
                    BigDecimal.ZERO) <= 0) {

                outOfStockProducts.add(product);

            } else if (currentStock.compareTo(
                    LOW_STOCK_LIMIT) <= 0) {

                lowStockProducts.add(product);
            }
        }

        return new InventoryReportData(
                lowStockProducts,
                outOfStockProducts,
                stockByProduct
        );
    }

    private BigDecimal zeroIfNull(
            BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private record InventoryReportData(
            List<Product> lowStockProducts,
            List<Product> outOfStockProducts,
            Map<Long, BigDecimal> stockByProduct) {
    }
}