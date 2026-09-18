package lk.aak.agency.controller;

import lk.aak.agency.model.Customer;
import lk.aak.agency.model.Product;
import lk.aak.agency.model.SalesInvoice;
import lk.aak.agency.model.SalesInvoiceItem;
import lk.aak.agency.repository.CustomerRepository;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.service.SalesInvoiceService;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/sales-invoices")
public class SalesInvoiceController {

    private final SalesInvoiceService salesInvoiceService;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public SalesInvoiceController(
            SalesInvoiceService salesInvoiceService,
            CustomerRepository customerRepository,
            ProductRepository productRepository) {

        this.salesInvoiceService =
                salesInvoiceService;

        this.customerRepository =
                customerRepository;

        this.productRepository =
                productRepository;
    }

    @GetMapping
    public String showInvoiceList(Model model) {

        model.addAttribute(
                "invoices",
                salesInvoiceService.getAllInvoices()
        );

        return "sales-invoices/sales-invoice-list";
    }

    @GetMapping("/new")
    public String showNewInvoiceForm(Model model) {

        SalesInvoice salesInvoice =
                new SalesInvoice();

        salesInvoice.setInvoiceDate(
                LocalDate.now()
        );

        salesInvoice.setSaleType("CREDIT");
        salesInvoice.setPaymentStatus("UNPAID");
        salesInvoice.setStatus("DRAFT");

        salesInvoice.setGrossAmount(
                BigDecimal.ZERO
        );

        salesInvoice.setDiscountAmount(
                BigDecimal.ZERO
        );

        salesInvoice.setReturnAmount(
                BigDecimal.ZERO
        );

        salesInvoice.setNetAmount(
                BigDecimal.ZERO
        );

        model.addAttribute(
                "salesInvoice",
                salesInvoice
        );

        model.addAttribute(
                "items",
                new ArrayList<SalesInvoiceItem>()
        );

        addFormData(model);

        return "sales-invoices/sales-invoice-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditInvoiceForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            SalesInvoice salesInvoice =
                    salesInvoiceService
                            .getInvoiceById(id);

            if ("COMPLETED".equalsIgnoreCase(
                    salesInvoice.getStatus())) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "A completed sales invoice cannot be edited."
                );

                return "redirect:/sales-invoices/view/"
                        + id;
            }

            model.addAttribute(
                    "salesInvoice",
                    salesInvoice
            );

            model.addAttribute(
                    "items",
                    salesInvoiceService
                            .getItemsByInvoiceId(id)
            );

            addFormData(model);

            return "sales-invoices/sales-invoice-form";

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/sales-invoices";
        }
    }

    @PostMapping("/save")
    public String saveInvoice(
            SalesInvoice salesInvoice,

            @RequestParam Long customerId,

            @RequestParam(
                    required = false,
                    name = "productIds"
            )
            List<Long> productIds,

            @RequestParam(
                    required = false,
                    name = "quantities"
            )
            List<BigDecimal> quantities,

            @RequestParam(
                    required = false,
                    name = "unitPrices"
            )
            List<BigDecimal> unitPrices,

            RedirectAttributes redirectAttributes) {

        try {
            Customer customer =
                    findCustomer(customerId);

            salesInvoice.setCustomer(customer);

            List<SalesInvoiceItem> items =
                    buildInvoiceItems(
                            productIds,
                            quantities,
                            unitPrices
                    );

            SalesInvoice savedInvoice =
                    salesInvoiceService
                            .saveInvoiceWithItems(
                                    salesInvoice,
                                    items
                            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Sales invoice and products saved successfully."
            );

            return "redirect:/sales-invoices/view/"
                    + savedInvoice.getId();

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            if (salesInvoice.getId() == null) {
                return "redirect:/sales-invoices/new";
            }

            return "redirect:/sales-invoices/edit/"
                    + salesInvoice.getId();
        }
    }

    @GetMapping("/view/{id}")
    public String viewInvoice(
            @PathVariable Long id,
            Model model,
            Authentication authentication) {

        SalesInvoice invoice =
                salesInvoiceService
                        .getInvoiceById(id);

        model.addAttribute(
                "invoice",
                invoice
        );

        model.addAttribute(
                "items",
                salesInvoiceService
                        .getItemsByInvoiceId(id)
        );

        model.addAttribute(
                "isAdmin",
                authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))
        );

        addProductData(model);

        return "sales-invoices/sales-invoice-view";
    }

    @PostMapping("/{invoiceId}/items/save")
    public String saveInvoiceItem(
            @PathVariable Long invoiceId,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity,
            @RequestParam BigDecimal unitPrice,
            RedirectAttributes redirectAttributes) {

        try {
            SalesInvoice invoice =
                    salesInvoiceService
                            .getInvoiceById(invoiceId);

            Product product =
                    findProduct(productId);

            SalesInvoiceItem item =
                    new SalesInvoiceItem();

            item.setSalesInvoice(invoice);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnit(product.getUnit());
            item.setUnitPrice(unitPrice);

            salesInvoiceService.saveItem(item);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    product.getDisplayName()
                            + " was added to the sales invoice."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/sales-invoices/view/"
                + invoiceId;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{invoiceId}/items/delete/{itemId}")
    public String deleteInvoiceItem(
            @PathVariable Long invoiceId,
            @PathVariable Long itemId,
            RedirectAttributes redirectAttributes) {

        try {
            salesInvoiceService.deleteItem(itemId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product removed from the sales invoice."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/sales-invoices/view/"
                + invoiceId;
    }

    @PostMapping("/{invoiceId}/complete")
    public String completeInvoice(
            @PathVariable Long invoiceId,
            RedirectAttributes redirectAttributes) {

        try {
            salesInvoiceService
                    .completeInvoice(invoiceId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Sales invoice completed successfully. "
                            + "Sold quantities were deducted from stock."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/sales-invoices/view/"
                + invoiceId;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{invoiceId}/complete-with-override")
    public String completeInvoiceWithCreditOverride(
            @PathVariable Long invoiceId,
            @RequestParam String overrideReason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            salesInvoiceService.completeInvoiceWithCreditOverride(
                    invoiceId, authentication.getName(), overrideReason
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Sales invoice completed with a credit limit override."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/sales-invoices/view/"
                + invoiceId;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteInvoice(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            salesInvoiceService.deleteInvoice(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Sales invoice deleted successfully."
            );

            return "redirect:/sales-invoices";

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/sales-invoices/view/"
                    + id;
        }
    }

    private List<SalesInvoiceItem> buildInvoiceItems(
            List<Long> productIds,
            List<BigDecimal> quantities,
            List<BigDecimal> unitPrices) {

        List<SalesInvoiceItem> items =
                new ArrayList<>();

        if (productIds == null
                || productIds.isEmpty()) {

            return items;
        }

        if (quantities == null
                || unitPrices == null) {

            throw new IllegalArgumentException(
                    "Some product information is missing."
            );
        }

        int itemCount =
                productIds.size();

        if (quantities.size() != itemCount
                || unitPrices.size() != itemCount) {

            throw new IllegalArgumentException(
                    "Product information is incomplete."
            );
        }

        for (int index = 0;
             index < itemCount;
             index++) {

            Product product =
                    findProduct(
                            productIds.get(index)
                    );

            SalesInvoiceItem item =
                    new SalesInvoiceItem();

            item.setProduct(product);

            item.setQuantity(
                    quantities.get(index)
            );

            item.setUnit(
                    product.getUnit()
            );

            item.setUnitPrice(
                    unitPrices.get(index)
            );

            item.calculateAmount();

            items.add(item);
        }

        return items;
    }

    private Customer findCustomer(
            Long customerId) {

        if (customerId == null) {

            throw new IllegalArgumentException(
                    "Please select a customer."
            );
        }

        return customerRepository
                .findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Selected customer was not found."
                        )
                );
    }

    private Product findProduct(
            Long productId) {

        if (productId == null) {

            throw new IllegalArgumentException(
                    "Please select a product."
            );
        }

        return productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Selected product was not found."
                        )
                );
    }

    private void addFormData(Model model) {

        model.addAttribute(
                "customers",
                customerRepository.findAll(
                        Sort.by(
                                Sort.Order.asc(
                                        "customerName"
                                )
                        )
                )
        );

        addProductData(model);
    }

    private void addProductData(Model model) {

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

        Map<Long, BigDecimal>
                availableStockByProduct =
                new LinkedHashMap<>();

        for (Product product : products) {

            availableStockByProduct.put(
                    product.getId(),
                    salesInvoiceService
                            .getAvailableStock(
                                    product.getId()
                            )
            );
        }

        model.addAttribute(
                "products",
                products
        );

        model.addAttribute(
                "availableStockByProduct",
                availableStockByProduct
        );
    }
}