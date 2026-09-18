package lk.aak.agency.controller;

import lk.aak.agency.service.EmployeeAdvanceService;
import lk.aak.agency.service.EmployeeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/advances")
public class EmployeeAdvanceController {

    private final EmployeeAdvanceService employeeAdvanceService;
    private final EmployeeService employeeService;

    public EmployeeAdvanceController(
            EmployeeAdvanceService employeeAdvanceService,
            EmployeeService employeeService) {

        this.employeeAdvanceService = employeeAdvanceService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String showList(Model model) {
        model.addAttribute("advances", employeeAdvanceService.getAllAdvances());
        return "employees/advance-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "employees/advance-form";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam Long employeeId,
            @RequestParam BigDecimal amount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate advanceDate,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            employeeAdvanceService.giveAdvance(employeeId, amount, advanceDate, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Advance recorded successfully.");
            return "redirect:/advances";

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/advances/new";
        }
    }
}
