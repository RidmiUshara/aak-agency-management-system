package lk.aak.agency.controller;

import lk.aak.agency.model.Product;
import lk.aak.agency.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService) {

        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String showInventory(Model model) {

        List<Product> products =
                inventoryService.getAllProducts();

        List<Product> lowStockProducts =
                inventoryService.getLowStockProducts();

        List<Product> outOfStockProducts =
                inventoryService.getOutOfStockProducts();

        model.addAttribute(
                "products",
                products
        );

        model.addAttribute(
                "stockByProduct",
                inventoryService.getStockByProduct()
        );

        model.addAttribute(
                "movements",
                inventoryService.getAllMovements()
        );

        model.addAttribute(
                "lowStockProducts",
                lowStockProducts
        );

        model.addAttribute(
                "outOfStockProducts",
                outOfStockProducts
        );

        model.addAttribute(
                "shortageByProduct",
                inventoryService.getShortageByProduct()
        );

        model.addAttribute(
                "totalLowStockProducts",
                lowStockProducts.size()
        );

        model.addAttribute(
                "totalOutOfStockProducts",
                outOfStockProducts.size()
        );

        List<InventoryService.ExpiringProductRow> expiringSoonProducts =
                inventoryService.getExpiringSoonProducts();

        model.addAttribute(
                "expiringSoonProducts",
                expiringSoonProducts
        );

        model.addAttribute(
                "totalExpiringSoonProducts",
                expiringSoonProducts.size()
        );

        return "inventory/inventory-list";
    }
}
