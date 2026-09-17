package lk.aak.agency.controller;

import lk.aak.agency.model.Product;
import lk.aak.agency.model.StockAdjustment;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.service.StockAdjustmentService;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/inventory/adjustments")
public class StockAdjustmentController {

    private final StockAdjustmentService
            stockAdjustmentService;

    private final ProductRepository
            productRepository;

    public StockAdjustmentController(
            StockAdjustmentService stockAdjustmentService,
            ProductRepository productRepository) {

        this.stockAdjustmentService =
                stockAdjustmentService;

        this.productRepository =
                productRepository;
    }

    @GetMapping
    public String showAdjustmentHistory(
            Model model) {

        model.addAttribute(
                "adjustments",
                stockAdjustmentService
                        .getAllAdjustments()
        );

        return "inventory/stock-adjustment-list";
    }

    @GetMapping("/new")
    public String showNewAdjustmentForm(
            Model model) {

        StockAdjustment stockAdjustment =
                new StockAdjustment();

        stockAdjustment.setAdjustmentDate(
                LocalDateTime.now()
        );

        stockAdjustment.setDirection("OUT");

        model.addAttribute(
                "stockAdjustment",
                stockAdjustment
        );

        model.addAttribute(
                "products",
                getActiveProducts()
        );

        return "inventory/stock-adjustment-form";
    }

    @PostMapping("/save")
    public String saveAdjustment(
            StockAdjustment stockAdjustment,
            @RequestParam Long productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username =
                    authentication == null
                            ? "SYSTEM"
                            : authentication.getName();

            stockAdjustmentService.saveAdjustment(
                    stockAdjustment,
                    productId,
                    username
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Stock adjustment recorded successfully."
            );

            return "redirect:/inventory/adjustments";

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/inventory/adjustments/new";
        }
    }

    private List<Product> getActiveProducts() {

        return productRepository
                .findAll(
                        Sort.by("productName").ascending()
                )
                .stream()
                .filter(product ->
                        "ACTIVE".equalsIgnoreCase(
                                product.getStatus()
                        )
                )
                .toList();
    }
}