package lk.aak.agency.controller;

import lk.aak.agency.model.Payment;
import lk.aak.agency.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/cheques")
public class ChequeController {

    private static final ZoneId SRI_LANKA_TIME_ZONE =
            ZoneId.of("Asia/Colombo");

    private final PaymentService paymentService;

    public ChequeController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String showChequeDashboard(Model model) {

        LocalDate today = LocalDate.now(SRI_LANKA_TIME_ZONE);

        List<Payment> cheques = paymentService
                .getAllPayments()
                .stream()
                .filter(this::isChequePayment)
                .sorted(
                        Comparator.comparing(
                                Payment::getChequeDate,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .toList();

        long totalChequeCount = cheques.size();

        long receivedChequeCount =
                countChequesByStatus(cheques, "RECEIVED");

        long depositedChequeCount =
                countChequesByStatus(cheques, "DEPOSITED");

        long clearedChequeCount =
                countChequesByStatus(cheques, "CLEARED");

        long returnedChequeCount =
                countChequesByStatus(cheques, "RETURNED");

        long overdueChequeCount = cheques.stream()
                .filter(payment -> isOverdueCheque(payment, today))
                .count();

        BigDecimal totalChequeAmount =
                calculateTotalAmount(cheques);

        BigDecimal pendingChequeAmount =
                calculatePendingChequeAmount(cheques);

        BigDecimal clearedChequeAmount =
                calculateAmountByStatus(cheques, "CLEARED");

        BigDecimal returnedChequeAmount =
                calculateAmountByStatus(cheques, "RETURNED");

        model.addAttribute("cheques", cheques);
        model.addAttribute("today", today);
        model.addAttribute("totalChequeCount", totalChequeCount);
        model.addAttribute("receivedChequeCount", receivedChequeCount);
        model.addAttribute("depositedChequeCount", depositedChequeCount);
        model.addAttribute("clearedChequeCount", clearedChequeCount);
        model.addAttribute("returnedChequeCount", returnedChequeCount);
        model.addAttribute("overdueChequeCount", overdueChequeCount);
        model.addAttribute("totalChequeAmount", totalChequeAmount);
        model.addAttribute("pendingChequeAmount", pendingChequeAmount);
        model.addAttribute("clearedChequeAmount", clearedChequeAmount);
        model.addAttribute("returnedChequeAmount", returnedChequeAmount);

        return "cheques/cheque-dashboard";
    }

    private boolean isChequePayment(Payment payment) {
        return payment != null
                && "CHEQUE".equalsIgnoreCase(payment.getPaymentMethod());
    }

    private String getChequeStatus(Payment payment) {

        if (payment.getChequeStatus() == null
                || payment.getChequeStatus().isBlank()) {
            return "RECEIVED";
        }

        return payment.getChequeStatus().trim().toUpperCase();
    }

    private long countChequesByStatus(
            List<Payment> cheques,
            String chequeStatus) {

        return cheques.stream()
                .filter(payment -> chequeStatus.equals(
                        getChequeStatus(payment)
                ))
                .count();
    }

    private BigDecimal calculateTotalAmount(List<Payment> cheques) {

        BigDecimal total = BigDecimal.ZERO;

        for (Payment payment : cheques) {
            total = total.add(zeroIfNull(payment.getAmount()));
        }

        return total;
    }

    private BigDecimal calculatePendingChequeAmount(
            List<Payment> cheques) {

        BigDecimal total = BigDecimal.ZERO;

        for (Payment payment : cheques) {

            String chequeStatus = getChequeStatus(payment);

            boolean isPending =
                    "RECEIVED".equals(chequeStatus)
                            || "DEPOSITED".equals(chequeStatus);

            if (isPending) {
                total = total.add(zeroIfNull(payment.getAmount()));
            }
        }

        return total;
    }

    private BigDecimal calculateAmountByStatus(
            List<Payment> cheques,
            String requiredStatus) {

        BigDecimal total = BigDecimal.ZERO;

        for (Payment payment : cheques) {

            if (requiredStatus.equals(getChequeStatus(payment))) {
                total = total.add(zeroIfNull(payment.getAmount()));
            }
        }

        return total;
    }

    private boolean isOverdueCheque(
            Payment payment,
            LocalDate today) {

        if (payment.getChequeDate() == null) {
            return false;
        }

        String chequeStatus = getChequeStatus(payment);

        boolean isStillPending =
                "RECEIVED".equals(chequeStatus)
                        || "DEPOSITED".equals(chequeStatus);

        return isStillPending
                && payment.getChequeDate().isBefore(today);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
