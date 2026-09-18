package lk.aak.agency.controller;

import lk.aak.agency.model.SupplierReturn;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.repository.PurchaseInvoiceRepository;
import lk.aak.agency.service.SupplierReturnService;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

@Controller
@RequestMapping("/supplier-returns")
public class SupplierReturnController {

    private final SupplierReturnService supplierReturnService;
    private final ProductRepository productRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;

    public SupplierReturnController(
            SupplierReturnService supplierReturnService,
            ProductRepository productRepository,
            PurchaseInvoiceRepository purchaseInvoiceRepository) {

        this.supplierReturnService = supplierReturnService;
        this.productRepository = productRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    }

    @GetMapping
    public String showList(Model model) {
        model.addAttribute("returns", supplierReturnService.getAllReturns());
        return "supplier-returns/supplier-return-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {

        SupplierReturn supplierReturn = new SupplierReturn();
        supplierReturn.setReturnDate(LocalDate.now());

        model.addAttribute("supplierReturn", supplierReturn);
        model.addAttribute(
                "purchaseInvoices",
                purchaseInvoiceRepository.findAllByOrderByInvoiceDateDesc()
        );

        return "supplier-returns/supplier-return-form";
    }

    @PostMapping("/save")
    public String save(
            SupplierReturn supplierReturn,
            RedirectAttributes redirectAttributes) {

        try {
            SupplierReturn saved = supplierReturnService.saveReturn(supplierReturn);
            return "redirect:/supplier-returns/view/" + saved.getId();

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/supplier-returns/new";
        }
    }

    @GetMapping("/view/{id}")
    public String view(
            @PathVariable Long id,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        SupplierReturn supplierReturn = supplierReturnService.getReturnById(id).orElse(null);

        if (supplierReturn == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Supplier return was not found.");
            return "redirect:/supplier-returns";
        }

        model.addAttribute("supplierReturn", supplierReturn);
        model.addAttribute("items", supplierReturnService.getItemsForReturn(id));
        model.addAttribute(
                "products",
                productRepository.findAll(Sort.by(Sort.Direction.ASC, "productName"))
        );
        model.addAttribute(
                "isAdmin",
                authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))
        );

        return "supplier-returns/supplier-return-view";
    }

    @PostMapping("/{id}/items/save")
    public String addItem(
            @PathVariable Long id,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) BigDecimal unitPrice,
            RedirectAttributes redirectAttributes) {

        try {
            supplierReturnService.addItem(id, productId, quantity, unitPrice);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to the return.");

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/supplier-returns/view/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public String approve(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            supplierReturnService.approveReturn(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Return approved - stock was removed from the warehouse."
            );

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/supplier-returns/view/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            supplierReturnService.rejectReturn(id);
            redirectAttributes.addFlashAttribute("successMessage", "Return rejected.");

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/supplier-returns/view/" + id;
    }
}
