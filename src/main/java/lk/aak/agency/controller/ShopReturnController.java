package lk.aak.agency.controller;

import jakarta.validation.Valid;
import lk.aak.agency.model.ShopReturn;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.service.CustomerService;
import lk.aak.agency.service.ShopReturnService;
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
@RequestMapping("/shop-returns")
public class ShopReturnController {

    private final ShopReturnService shopReturnService;
    private final CustomerService customerService;
    private final ProductRepository productRepository;

    public ShopReturnController(
            ShopReturnService shopReturnService,
            CustomerService customerService,
            ProductRepository productRepository) {

        this.shopReturnService = shopReturnService;
        this.customerService = customerService;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String showList(Model model) {
        model.addAttribute("returns", shopReturnService.getAllReturns());
        return "shop-returns/shop-return-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {

        ShopReturn shopReturn = new ShopReturn();
        shopReturn.setReturnDate(LocalDate.now());

        model.addAttribute("shopReturn", shopReturn);
        model.addAttribute("customers", customerService.getAllCustomers());

        return "shop-returns/shop-return-form";
    }

    @PostMapping("/save")
    public String save(
            @Valid ShopReturn shopReturn,
            RedirectAttributes redirectAttributes) {

        try {
            ShopReturn saved = shopReturnService.saveReturn(shopReturn);
            return "redirect:/shop-returns/view/" + saved.getId();

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/shop-returns/new";
        }
    }

    @GetMapping("/view/{id}")
    public String view(
            @PathVariable Long id,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        ShopReturn shopReturn = shopReturnService.getReturnById(id).orElse(null);

        if (shopReturn == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shop return was not found.");
            return "redirect:/shop-returns";
        }

        model.addAttribute("shopReturn", shopReturn);
        model.addAttribute("items", shopReturnService.getItemsForReturn(id));
        model.addAttribute(
                "products",
                productRepository.findAll(Sort.by(Sort.Direction.ASC, "productName"))
        );
        model.addAttribute(
                "isAdmin",
                authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))
        );

        return "shop-returns/shop-return-view";
    }

    @PostMapping("/{id}/items/save")
    public String addItem(
            @PathVariable Long id,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) BigDecimal unitPrice,
            @RequestParam(required = false) String category,
            RedirectAttributes redirectAttributes) {

        try {
            shopReturnService.addItem(id, productId, quantity, unitPrice, category);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to the return.");

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/shop-returns/view/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public String approve(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            shopReturnService.approveReturn(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Return approved - saleable items were restocked to the warehouse."
            );

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/shop-returns/view/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            shopReturnService.rejectReturn(id);
            redirectAttributes.addFlashAttribute("successMessage", "Return rejected.");

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/shop-returns/view/" + id;
    }
}
