package lk.aak.agency.controller;

import jakarta.validation.Valid;
import lk.aak.agency.model.Route;
import lk.aak.agency.service.RouteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public String showRouteList(Model model) {
        model.addAttribute("routes", routeService.getAllRoutes());
        return "routes/route-list";
    }

    @GetMapping("/new")
    public String showAddRouteForm(Model model) {
        model.addAttribute("route", new Route());
        model.addAttribute("pageTitle", "Add New Route");
        return "routes/route-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditRouteForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Route route = routeService.getRouteById(id).orElse(null);

        if (route == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Route not found.");
            return "redirect:/routes";
        }

        model.addAttribute("route", route);
        model.addAttribute("pageTitle", "Edit Route");
        return "routes/route-form";
    }

    @PostMapping("/save")
    public String saveRoute(
            @Valid Route route,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "pageTitle",
                    route.getId() == null ? "Add New Route" : "Edit Route"
            );
            return "routes/route-form";
        }

        routeService.saveRoute(route);

        redirectAttributes.addFlashAttribute("successMessage", "Route saved successfully.");
        return "redirect:/routes";
    }
}
