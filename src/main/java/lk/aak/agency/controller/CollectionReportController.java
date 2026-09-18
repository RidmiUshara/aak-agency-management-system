package lk.aak.agency.controller;

import lk.aak.agency.model.Payment;
import lk.aak.agency.repository.PaymentRepository;
import lk.aak.agency.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Controller
@RequestMapping("/collections")
public class CollectionReportController {

    private static final ZoneId SRI_LANKA_TIME_ZONE =
            ZoneId.of("Asia/Colombo");

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public CollectionReportController(
            PaymentRepository paymentRepository,
            PaymentService paymentService) {

        this.paymentRepository =
                paymentRepository;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String showCollectionReport(
            @RequestParam(defaultValue = "DAILY")
            String period,

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

        LocalDate today =
                LocalDate.now(
                        SRI_LANKA_TIME_ZONE
                );

        String selectedPeriod =
                normalizePeriod(period);

        DateRange dateRange =
                calculateDateRange(
                        selectedPeriod,
                        fromDate,
                        toDate,
                        today
                );

        List<Payment> payments =
                paymentRepository
                        .findByPaymentDateBetweenOrderByPaymentDateDesc(
                                dateRange.fromDate(),
                                dateRange.toDate()
                        );

        BigDecimal totalCash =
                calculateMethodTotal(
                        payments,
                        "CASH"
                );

        BigDecimal totalCheque =
                calculateMethodTotal(
                        payments,
                        "CHEQUE"
                );

        BigDecimal totalBankTransfer =
                calculateMethodTotal(
                        payments,
                        "BANK_TRANSFER"
                );

        BigDecimal totalCollected =
                totalCash
                        .add(totalCheque)
                        .add(totalBankTransfer);

        long receivedPaymentCount =
                payments.stream()
                        .filter(
                                this::isReceivedPayment
                        )
                        .count();

        long cashPaymentCount =
                countPaymentsByMethod(
                        payments,
                        "CASH"
                );

        long chequePaymentCount =
                countPaymentsByMethod(
                        payments,
                        "CHEQUE"
                );

        long bankTransferPaymentCount =
                countPaymentsByMethod(
                        payments,
                        "BANK_TRANSFER"
                );

        model.addAttribute(
                "payments",
                payments
        );

        model.addAttribute(
                "selectedPeriod",
                selectedPeriod
        );

        model.addAttribute(
                "fromDate",
                dateRange.fromDate()
        );

        model.addAttribute(
                "toDate",
                dateRange.toDate()
        );

        model.addAttribute(
                "today",
                today
        );

        model.addAttribute(
                "totalCollected",
                totalCollected
        );

        model.addAttribute(
                "totalCash",
                totalCash
        );

        model.addAttribute(
                "totalCheque",
                totalCheque
        );

        model.addAttribute(
                "totalBankTransfer",
                totalBankTransfer
        );

        model.addAttribute(
                "receivedPaymentCount",
                receivedPaymentCount
        );

        model.addAttribute(
                "cashPaymentCount",
                cashPaymentCount
        );

        model.addAttribute(
                "chequePaymentCount",
                chequePaymentCount
        );

        model.addAttribute(
                "bankTransferPaymentCount",
                bankTransferPaymentCount
        );

        model.addAttribute(
                "reportTitle",
                createReportTitle(
                        selectedPeriod
                )
        );

        return "collections/collection-dashboard";
    }

    @GetMapping("/handover")
    public String showHandoverReconciliation(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            Model model) {

        LocalDate selectedDate =
                date != null ? date : LocalDate.now(SRI_LANKA_TIME_ZONE);

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute(
                "handoverRows",
                paymentService.getHandoverSummaryForDate(selectedDate)
        );

        return "collections/handover-reconciliation";
    }

    @PostMapping("/handover/mark-handed-over")
    public String markHandedOver(
            @RequestParam Long collectorEmployeeId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            RedirectAttributes redirectAttributes) {

        try {
            paymentService.markHandedOver(collectorEmployeeId, date);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Marked as handed over."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/collections/handover?date=" + date;
    }

    private String normalizePeriod(
            String period) {

        if (period == null
                || period.isBlank()) {

            return "DAILY";
        }

        String normalizedPeriod =
                period
                        .trim()
                        .toUpperCase();

        return switch (normalizedPeriod) {

            case "DAILY",
                 "WEEKLY",
                 "MONTHLY",
                 "CUSTOM" ->
                    normalizedPeriod;

            default ->
                    "DAILY";
        };
    }

    private DateRange calculateDateRange(
            String selectedPeriod,
            LocalDate requestedFromDate,
            LocalDate requestedToDate,
            LocalDate today) {

        if ("WEEKLY".equals(
                selectedPeriod)) {

            LocalDate weekStart =
                    today.with(
                            TemporalAdjusters
                                    .previousOrSame(
                                            DayOfWeek.MONDAY
                                    )
                    );

            LocalDate weekEnd =
                    today.with(
                            TemporalAdjusters
                                    .nextOrSame(
                                            DayOfWeek.SUNDAY
                                    )
                    );

            return new DateRange(
                    weekStart,
                    weekEnd
            );
        }

        if ("MONTHLY".equals(
                selectedPeriod)) {

            LocalDate monthStart =
                    today.withDayOfMonth(1);

            LocalDate monthEnd =
                    today.withDayOfMonth(
                            today.lengthOfMonth()
                    );

            return new DateRange(
                    monthStart,
                    monthEnd
            );
        }

        if ("CUSTOM".equals(
                selectedPeriod)) {

            LocalDate customFromDate =
                    requestedFromDate == null
                            ? today
                            : requestedFromDate;

            LocalDate customToDate =
                    requestedToDate == null
                            ? customFromDate
                            : requestedToDate;

            if (customFromDate.isAfter(
                    customToDate)) {

                LocalDate temporaryDate =
                        customFromDate;

                customFromDate =
                        customToDate;

                customToDate =
                        temporaryDate;
            }

            return new DateRange(
                    customFromDate,
                    customToDate
            );
        }

        return new DateRange(
                today,
                today
        );
    }

    private BigDecimal calculateMethodTotal(
            List<Payment> payments,
            String paymentMethod) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (Payment payment : payments) {

            boolean correctPaymentMethod =
                    paymentMethod.equalsIgnoreCase(
                            payment.getPaymentMethod()
                    );

            if (correctPaymentMethod
                    && isReceivedPayment(payment)) {

                BigDecimal paymentAmount =
                        payment.getAmount() == null
                                ? BigDecimal.ZERO
                                : payment.getAmount();

                total =
                        total.add(
                                paymentAmount
                        );
            }
        }

        return total;
    }

    private long countPaymentsByMethod(
            List<Payment> payments,
            String paymentMethod) {

        return payments.stream()
                .filter(
                        this::isReceivedPayment
                )
                .filter(payment ->
                        paymentMethod.equalsIgnoreCase(
                                payment.getPaymentMethod()
                        )
                )
                .count();
    }

    private boolean isReceivedPayment(
            Payment payment) {

        return payment != null
                && "RECEIVED".equalsIgnoreCase(
                payment.getStatus()
        );
    }

    private String createReportTitle(
            String selectedPeriod) {

        return switch (selectedPeriod) {

            case "WEEKLY" ->
                    "This Week's Collection";

            case "MONTHLY" ->
                    "This Month's Collection";

            case "CUSTOM" ->
                    "Custom Collection Report";

            default ->
                    "Today's Collection";
        };
    }

    private record DateRange(
            LocalDate fromDate,
            LocalDate toDate) {
    }
}