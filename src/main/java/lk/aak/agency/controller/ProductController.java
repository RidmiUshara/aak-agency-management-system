package lk.aak.agency.controller;

import lk.aak.agency.model.Product;
import lk.aak.agency.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(
            ProductService productService) {

        this.productService = productService;
    }

    @GetMapping
    public String showProductList(
            @RequestParam(
                    required = false
            ) String keyword,
            Model model) {

        model.addAttribute(
                "products",
                productService.searchProducts(keyword)
        );

        model.addAttribute("keyword", keyword);

        return "products/product-list";
    }

    @GetMapping("/new")
    public String showAddProductForm(Model model) {

        model.addAttribute(
                "product",
                new Product()
        );

        model.addAttribute(
                "pageTitle",
                "Add New Product"
        );

        return "products/product-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditProductForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Product product = productService
                .getProductById(id)
                .orElse(null);

        if (product == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Product not found."
            );

            return "redirect:/products";
        }

        model.addAttribute("product", product);

        model.addAttribute(
                "pageTitle",
                "Edit Product"
        );

        return "products/product-form";
    }

    @PostMapping("/save")
    public String saveProduct(
            Product product,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            productService.saveProduct(product);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product saved successfully."
            );

            return "redirect:/products";

        } catch (IllegalArgumentException exception) {

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            if (product.getId() == null) {
                model.addAttribute(
                        "pageTitle",
                        "Add New Product"
                );
            } else {
                model.addAttribute(
                        "pageTitle",
                        "Edit Product"
                );
            }

            return "products/product-form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (productService
                .getProductById(id)
                .isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Product not found."
            );

            return "redirect:/products";
        }

        productService.deleteProduct(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product deleted successfully."
        );

        return "redirect:/products";
    }
}