package lk.aak.agency.controller;

import lk.aak.agency.model.Product;
import lk.aak.agency.model.PurchaseInvoice;
import lk.aak.agency.model.PurchaseInvoiceItem;
import lk.aak.agency.model.SupplierPayment;
import lk.aak.agency.repository.ProductRepository;
import lk.aak.agency.service.PurchaseInvoiceFileService;
import lk.aak.agency.service.PurchaseInvoiceService;
import lk.aak.agency.service.SupplierPaymentService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/purchase-invoices")
public class PurchaseInvoiceController {

    private static final int PAGE_SIZE = 25;

    private final PurchaseInvoiceService purchaseInvoiceService;
    private final ProductRepository productRepository;
    private final PurchaseInvoiceFileService purchaseInvoiceFileService;
    private final SupplierPaymentService supplierPaymentService;

    public PurchaseInvoiceController(
            PurchaseInvoiceService purchaseInvoiceService,
            ProductRepository productRepository,
            PurchaseInvoiceFileService purchaseInvoiceFileService,
            SupplierPaymentService supplierPaymentService) {

        this.purchaseInvoiceService = purchaseInvoiceService;
        this.productRepository = productRepository;
        this.purchaseInvoiceFileService = purchaseInvoiceFileService;
        this.supplierPaymentService = supplierPaymentService;
    }

    @GetMapping
    public String listPurchaseInvoices(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<PurchaseInvoice> invoicePage =
                purchaseInvoiceService.getInvoicePage(
                        search, page, PAGE_SIZE
                );

        model.addAttribute(
                "invoices",
                invoicePage.getContent()
        );

        model.addAttribute("search", search);
        model.addAttribute("currentPage", invoicePage.getNumber());
        model.addAttribute("totalPages", invoicePage.getTotalPages());
        model.addAttribute("totalRecords", invoicePage.getTotalElements());

        return "purchase-invoices/purchase-invoice-list";
    }

    @GetMapping("/new")
    public String showNewInvoiceForm(Model model) {

        PurchaseInvoice purchaseInvoice =
                new PurchaseInvoice();

        purchaseInvoice.setInvoiceDate(LocalDate.now());

        purchaseInvoice.setSupplierName(
                "CBL Foods International (Pvt) Ltd"
        );

        purchaseInvoice.setPlaceOfSupply("Ranala");
        purchaseInvoice.setStatus("DRAFT");

        model.addAttribute(
                "purchaseInvoice",
                purchaseInvoice
        );

        model.addAttribute(
                "products",
                getSortedProducts()
        );

        model.addAttribute(
                "items",
                Collections.emptyList()
        );

        return "purchase-invoices/purchase-invoice-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditInvoiceForm(
            @PathVariable Long id,
            Model model) {

        PurchaseInvoice purchaseInvoice =
                purchaseInvoiceService.getInvoiceById(id);

        if ("COMPLETED".equalsIgnoreCase(
                purchaseInvoice.getStatus())) {

            return "redirect:/purchase-invoices/view/" + id;
        }

        model.addAttribute(
                "purchaseInvoice",
                purchaseInvoice
        );

        model.addAttribute(
                "products",
                getSortedProducts()
        );

        model.addAttribute(
                "items",
                purchaseInvoiceService
                        .getItemsByInvoiceId(id)
        );

        return "purchase-invoices/purchase-invoice-form";
    }

    @PostMapping("/save")
    public String saveInvoice(
            PurchaseInvoice purchaseInvoice,

            @RequestParam(
                    name = "originalInvoiceFile",
                    required = false
            )
            MultipartFile originalInvoiceFile,

            @RequestParam(
                    name = "productIds",
                    required = false
            )
            List<Long> productIds,

            @RequestParam(
                    name = "quantities",
                    required = false
            )
            List<BigDecimal> quantities,

            @RequestParam(
                    name = "unitPrices",
                    required = false
            )
            List<BigDecimal> unitPrices,

            @RequestParam(
                    name = "expiryDates",
                    required = false
            )
            List<String> expiryDates,

            RedirectAttributes redirectAttributes) {

        String newlyStoredFileName = null;
        String previousStoredFileName = null;

        try {
            validateProductLists(
                    productIds,
                    quantities,
                    unitPrices
            );

            if (purchaseInvoice.getId() != null) {

                PurchaseInvoice existingInvoice =
                        purchaseInvoiceService.getInvoiceById(
                                purchaseInvoice.getId()
                        );

                copyExistingFileInformation(
                        existingInvoice,
                        purchaseInvoice
                );

                previousStoredFileName =
                        existingInvoice
                                .getInvoiceFileStoredName();
            }

            boolean hasNewFile =
                    originalInvoiceFile != null
                            && !originalInvoiceFile.isEmpty();

            if (hasNewFile) {

                PurchaseInvoiceFileService.StoredInvoiceFile
                        storedFile =
                        purchaseInvoiceFileService.storeFile(
                                originalInvoiceFile
                        );

                newlyStoredFileName =
                        storedFile.storedFileName();

                purchaseInvoice.setInvoiceFileOriginalName(
                        storedFile.originalFileName()
                );

                purchaseInvoice.setInvoiceFileStoredName(
                        storedFile.storedFileName()
                );

                purchaseInvoice.setInvoiceFileContentType(
                        storedFile.contentType()
                );

                purchaseInvoice.setInvoiceFileSize(
                        storedFile.fileSize()
                );

                purchaseInvoice.setInvoiceFileUploadedAt(
                        LocalDateTime.now()
                );
            }

            List<PurchaseInvoiceItem> invoiceItems =
                    createInvoiceItems(
                            productIds,
                            quantities,
                            unitPrices,
                            expiryDates
                    );

            PurchaseInvoice savedInvoice =
                    purchaseInvoiceService
                            .saveInvoiceWithItems(
                                    purchaseInvoice,
                                    invoiceItems
                            );

            if (hasNewFile
                    && previousStoredFileName != null
                    && !previousStoredFileName.isBlank()
                    && !previousStoredFileName.equals(
                    newlyStoredFileName)) {

                try {
                    purchaseInvoiceFileService.deleteFile(
                            previousStoredFileName
                    );

                } catch (IllegalArgumentException ignored) {
                    /*
                     * Invoice data has already been saved.
                     * Failure to delete the old file must not
                     * remove the new invoice information.
                     */
                }
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Purchase invoice and products saved successfully."
            );

            return "redirect:/purchase-invoices/view/"
                    + savedInvoice.getId();

        } catch (IllegalArgumentException exception) {

            if (newlyStoredFileName != null) {

                try {
                    purchaseInvoiceFileService.deleteFile(
                            newlyStoredFileName
                    );

                } catch (IllegalArgumentException ignored) {
                    // Keep and display the original error.
                }
            }

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            if (purchaseInvoice.getId() == null) {
                return "redirect:/purchase-invoices/new";
            }

            return "redirect:/purchase-invoices/edit/"
                    + purchaseInvoice.getId();
        }
    }

    @GetMapping("/view/{id}")
    public String viewInvoice(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "invoice",
                purchaseInvoiceService.getInvoiceById(id)
        );

        model.addAttribute(
                "items",
                purchaseInvoiceService
                        .getItemsByInvoiceId(id)
        );

        model.addAttribute(
                "products",
                getSortedProducts()
        );

        return "purchase-invoices/purchase-invoice-view";
    }

    @GetMapping("/{invoiceId}/original-file")
    public ResponseEntity<Resource> viewOriginalInvoiceFile(
            @PathVariable Long invoiceId) {

        PurchaseInvoice invoice =
                purchaseInvoiceService
                        .getInvoiceById(invoiceId);

        if (!invoice.hasInvoiceFile()) {
            throw new IllegalArgumentException(
                    "No original CBL invoice file is attached."
            );
        }

        Resource resource =
                purchaseInvoiceFileService.loadFile(
                        invoice.getInvoiceFileStoredName()
                );

        MediaType mediaType =
                getMediaType(
                        invoice.getInvoiceFileContentType()
                );

        ContentDisposition disposition =
                ContentDisposition
                        .inline()
                        .filename(
                                invoice.getInvoiceFileOriginalName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(resource);
    }

    @PostMapping("/{invoiceId}/items/save")
    public String saveInvoiceItem(
            @PathVariable Long invoiceId,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity,
            @RequestParam BigDecimal unitPrice,
            @RequestParam(required = false) String expiryDate,
            RedirectAttributes redirectAttributes) {

        try {
            PurchaseInvoice invoice =
                    purchaseInvoiceService
                            .getInvoiceById(invoiceId);

            Product product =
                    productRepository
                            .findById(productId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Selected product was not found."
                                    )
                            );

            PurchaseInvoiceItem item =
                    new PurchaseInvoiceItem();

            item.setPurchaseInvoice(invoice);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnit(product.getUnit());
            item.setUnitPrice(unitPrice);

            if (expiryDate != null && !expiryDate.isBlank()) {
                item.setExpiryDate(java.time.LocalDate.parse(expiryDate));
            }

            purchaseInvoiceService.saveItem(item);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    product.getDisplayName()
                            + " was added to the purchase invoice."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/purchase-invoices/view/"
                + invoiceId;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{invoiceId}/items/delete/{itemId}")
    public String deleteInvoiceItem(
            @PathVariable Long invoiceId,
            @PathVariable Long itemId,
            RedirectAttributes redirectAttributes) {

        try {
            purchaseInvoiceService.deleteItem(itemId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product removed from the purchase invoice."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/purchase-invoices/view/"
                + invoiceId;
    }

    @PostMapping("/{invoiceId}/complete")
    public String completeInvoice(
            @PathVariable Long invoiceId,

            @RequestParam(
                    name = "verificationConfirmed",
                    defaultValue = "false"
            )
            boolean verificationConfirmed,

            RedirectAttributes redirectAttributes) {

        try {
            purchaseInvoiceService.completeInvoice(
                    invoiceId,
                    verificationConfirmed
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Purchase invoice verified and completed. "
                            + "Purchased quantities were added to stock."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/purchase-invoices/view/"
                + invoiceId;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteInvoice(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            PurchaseInvoice invoice =
                    purchaseInvoiceService
                            .getInvoiceById(id);

            String storedFileName =
                    invoice.getInvoiceFileStoredName();

            purchaseInvoiceService.deleteInvoice(id);

            if (storedFileName != null
                    && !storedFileName.isBlank()) {

                try {
                    purchaseInvoiceFileService.deleteFile(
                            storedFileName
                    );

                } catch (IllegalArgumentException exception) {

                    redirectAttributes.addFlashAttribute(
                            "errorMessage",
                            "Invoice deleted, but its uploaded file "
                                    + "could not be removed."
                    );

                    return "redirect:/purchase-invoices";
                }
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Purchase invoice deleted successfully."
            );

            return "redirect:/purchase-invoices";

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/purchase-invoices/view/" + id;
        }
    }

    @GetMapping("/supplier-balance")
    public String showSupplierBalance(Model model) {

        model.addAttribute(
                "summary",
                supplierPaymentService.getSupplierBalanceSummary()
        );

        return "purchase-invoices/supplier-balance";
    }

    @PostMapping("/supplier-balance/pay")
    public String recordSupplierPayment(
            @RequestParam Long purchaseInvoiceId,
            @RequestParam BigDecimal amount,
            @RequestParam LocalDate paymentDate,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String referenceNumber,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            SupplierPayment payment = new SupplierPayment();
            payment.setPurchaseInvoiceId(purchaseInvoiceId);
            payment.setAmount(amount);
            payment.setPaymentDate(paymentDate);
            payment.setPaymentMethod(paymentMethod);
            payment.setReferenceNumber(referenceNumber);
            payment.setNotes(notes);

            supplierPaymentService.recordPayment(payment);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Payment to CBL recorded successfully."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/purchase-invoices/supplier-balance";
    }

    private void validateProductLists(
            List<Long> productIds,
            List<BigDecimal> quantities,
            List<BigDecimal> unitPrices) {

        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Add at least one product to the purchase invoice."
            );
        }

        if (quantities == null
                || unitPrices == null
                || productIds.size() != quantities.size()
                || productIds.size() != unitPrices.size()) {

            throw new IllegalArgumentException(
                    "The purchase product information is incomplete."
            );
        }
    }

    private List<PurchaseInvoiceItem> createInvoiceItems(
            List<Long> productIds,
            List<BigDecimal> quantities,
            List<BigDecimal> unitPrices,
            List<String> expiryDates) {

        List<PurchaseInvoiceItem> invoiceItems =
                new ArrayList<>();

        for (int index = 0;
             index < productIds.size();
             index++) {

            Long productId = productIds.get(index);
            BigDecimal quantity = quantities.get(index);
            BigDecimal unitPrice = unitPrices.get(index);

            Product product =
                    productRepository
                            .findById(productId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "A selected product was not found."
                                    )
                            );

            PurchaseInvoiceItem item =
                    new PurchaseInvoiceItem();

            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnit(product.getUnit());
            item.setUnitPrice(unitPrice);

            if (expiryDates != null
                    && index < expiryDates.size()
                    && expiryDates.get(index) != null
                    && !expiryDates.get(index).isBlank()) {

                item.setExpiryDate(
                        java.time.LocalDate.parse(expiryDates.get(index))
                );
            }

            item.calculateAmount();

            invoiceItems.add(item);
        }

        return invoiceItems;
    }

    private void copyExistingFileInformation(
            PurchaseInvoice source,
            PurchaseInvoice destination) {

        destination.setInvoiceFileOriginalName(
                source.getInvoiceFileOriginalName()
        );

        destination.setInvoiceFileStoredName(
                source.getInvoiceFileStoredName()
        );

        destination.setInvoiceFileContentType(
                source.getInvoiceFileContentType()
        );

        destination.setInvoiceFileSize(
                source.getInvoiceFileSize()
        );

        destination.setInvoiceFileUploadedAt(
                source.getInvoiceFileUploadedAt()
        );
    }

    private MediaType getMediaType(String contentType) {

        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);

        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private List<Product> getSortedProducts() {

        return productRepository.findAll(
                Sort.by(
                        Sort.Order.asc("productName"),
                        Sort.Order.asc("netWeight")
                )
        );
    }
}