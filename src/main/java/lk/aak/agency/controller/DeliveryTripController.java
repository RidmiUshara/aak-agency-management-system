package lk.aak.agency.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lk.aak.agency.model.DeliveryTrip;
import lk.aak.agency.service.DeliveryTripService;
import lk.aak.agency.service.EmployeeService;
import lk.aak.agency.service.RouteService;
import lk.aak.agency.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/delivery-trips")
public class DeliveryTripController {

    private final DeliveryTripService deliveryTripService;
    private final RouteService routeService;
    private final VehicleService vehicleService;
    private final EmployeeService employeeService;

    public DeliveryTripController(
            DeliveryTripService deliveryTripService,
            RouteService routeService,
            VehicleService vehicleService,
            EmployeeService employeeService) {

        this.deliveryTripService = deliveryTripService;
        this.routeService = routeService;
        this.vehicleService = vehicleService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String showTripList(Model model) {
        model.addAttribute("trips", deliveryTripService.getAllTrips());
        return "delivery-trips/trip-list";
    }

    @GetMapping("/new")
    public String showAddTripForm(Model model) {
        model.addAttribute("trip", new DeliveryTrip());
        model.addAttribute("pageTitle", "Plan New Delivery Trip");
        addFormReferenceData(model);
        return "delivery-trips/trip-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditTripForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        DeliveryTrip trip = deliveryTripService.getTripById(id).orElse(null);

        if (trip == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Delivery trip was not found.");
            return "redirect:/delivery-trips";
        }

        model.addAttribute("trip", trip);
        model.addAttribute("pageTitle", "Edit Delivery Trip");
        addFormReferenceData(model);
        return "delivery-trips/trip-form";
    }

    @PostMapping("/save")
    public String saveTrip(
            @Valid DeliveryTrip trip,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "pageTitle",
                    trip.getId() == null ? "Plan New Delivery Trip" : "Edit Delivery Trip"
            );
            addFormReferenceData(model);
            return "delivery-trips/trip-form";
        }

        deliveryTripService.saveTrip(trip);

        redirectAttributes.addFlashAttribute("successMessage", "Delivery trip saved successfully.");
        return "redirect:/delivery-trips";
    }

    @GetMapping("/view/{id}")
    public String viewTrip(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        DeliveryTrip trip = deliveryTripService.getTripById(id).orElse(null);

        if (trip == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Delivery trip was not found.");
            return "redirect:/delivery-trips";
        }

        model.addAttribute("trip", trip);
        model.addAttribute("assignedInvoices", deliveryTripService.getInvoicesForTrip(id));
        model.addAttribute("unassignedInvoices", deliveryTripService.getUnassignedCompletedInvoices());
        model.addAttribute("loadingSummary", deliveryTripService.getLoadingSummary(id));
        model.addAttribute("loadItems", deliveryTripService.getLoadItemsForTrip(id));

        return "delivery-trips/trip-view";
    }

    @PostMapping("/{tripId}/confirm-loading")
    public String confirmLoading(
            @PathVariable Long tripId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Map<Long, BigDecimal> loadedQuantityByProductId = new HashMap<>();

        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {

            if (entry.getKey().startsWith("loadedQuantity_")) {

                Long productId = Long.valueOf(entry.getKey().substring("loadedQuantity_".length()));
                String value = entry.getValue()[0];

                if (value != null && !value.isBlank()) {
                    loadedQuantityByProductId.put(productId, new BigDecimal(value));
                }
            }
        }

        deliveryTripService.confirmLoading(tripId, loadedQuantityByProductId);

        redirectAttributes.addFlashAttribute("successMessage", "Loading confirmed - trip marked as Loaded.");
        return "redirect:/delivery-trips/view/" + tripId;
    }

    @GetMapping("/vehicle-stock")
    public String showVehicleStock(Model model) {

        model.addAttribute("vehicleStockRows", deliveryTripService.getVehicleStockSummary());
        return "delivery-trips/vehicle-stock";
    }

    @PostMapping("/{tripId}/assign-invoice")
    public String assignInvoice(
            @PathVariable Long tripId,
            @RequestParam Long invoiceId,
            RedirectAttributes redirectAttributes) {

        deliveryTripService.assignInvoiceToTrip(tripId, invoiceId);

        redirectAttributes.addFlashAttribute("successMessage", "Bill assigned to the trip.");
        return "redirect:/delivery-trips/view/" + tripId;
    }

    @PostMapping("/{tripId}/remove-invoice")
    public String removeInvoice(
            @PathVariable Long tripId,
            @RequestParam Long invoiceId,
            RedirectAttributes redirectAttributes) {

        deliveryTripService.removeInvoiceFromTrip(tripId, invoiceId);

        redirectAttributes.addFlashAttribute("successMessage", "Bill removed from the trip.");
        return "redirect:/delivery-trips/view/" + tripId;
    }

    @PostMapping("/{tripId}/delivery-status")
    public String updateDeliveryStatus(
            @PathVariable Long tripId,
            @RequestParam Long invoiceId,
            @RequestParam String deliveryStatus,
            RedirectAttributes redirectAttributes) {

        deliveryTripService.updateDeliveryStatus(tripId, invoiceId, deliveryStatus);

        redirectAttributes.addFlashAttribute("successMessage", "Delivery status updated.");
        return "redirect:/delivery-trips/view/" + tripId;
    }

    private void addFormReferenceData(Model model) {
        model.addAttribute("routes", routeService.getActiveRoutes());
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("drivers", employeeService.getActiveDrivers());
        model.addAttribute("helpers", employeeService.getActiveHelpers());
    }
}
