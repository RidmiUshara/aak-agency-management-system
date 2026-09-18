package lk.aak.agency.controller;

import jakarta.validation.Valid;
import lk.aak.agency.model.Vehicle;
import lk.aak.agency.service.EmployeeService;
import lk.aak.agency.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final EmployeeService employeeService;

    public VehicleController(VehicleService vehicleService, EmployeeService employeeService) {
        this.vehicleService = vehicleService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String showVehicleList(Model model) {
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        return "vehicles/vehicle-list";
    }

    @GetMapping("/new")
    public String showAddVehicleForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("pageTitle", "Add New Vehicle");
        model.addAttribute("drivers", employeeService.getActiveDrivers());
        return "vehicles/vehicle-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditVehicleForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Vehicle vehicle = vehicleService.getVehicleById(id).orElse(null);

        if (vehicle == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vehicle not found.");
            return "redirect:/vehicles";
        }

        model.addAttribute("vehicle", vehicle);
        model.addAttribute("pageTitle", "Edit Vehicle");
        model.addAttribute("drivers", employeeService.getActiveDrivers());
        return "vehicles/vehicle-form";
    }

    @PostMapping("/save")
    public String saveVehicle(
            @Valid Vehicle vehicle,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "pageTitle",
                    vehicle.getId() == null ? "Add New Vehicle" : "Edit Vehicle"
            );
            model.addAttribute("drivers", employeeService.getActiveDrivers());
            return "vehicles/vehicle-form";
        }

        vehicleService.saveVehicle(vehicle);

        redirectAttributes.addFlashAttribute("successMessage", "Vehicle saved successfully.");
        return "redirect:/vehicles";
    }
}
