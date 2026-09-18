package lk.aak.agency.controller;

import lk.aak.agency.service.EmployeeAdvanceService;
import lk.aak.agency.service.EmployeeSalaryService;
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
import java.util.List;

@Controller
@RequestMapping("/salary")
public class EmployeeSalaryController {

    private final EmployeeSalaryService employeeSalaryService;
    private final EmployeeAdvanceService employeeAdvanceService;
    private final EmployeeService employeeService;

    public EmployeeSalaryController(
            EmployeeSalaryService employeeSalaryService,
            EmployeeAdvanceService employeeAdvanceService,
            EmployeeService employeeService) {

        this.employeeSalaryService = employeeSalaryService;
        this.employeeAdvanceService = employeeAdvanceService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String showList(Model model) {
        model.addAttribute("payments", employeeSalaryService.getAllPayments());
        return "employees/salary-list";
    }

    @GetMapping("/new")
    public String showNewForm(
            @RequestParam(required = false) Long employeeId,
            Model model) {

        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("selectedEmployeeId", employeeId);

        if (employeeId != null) {
            model.addAttribute("unsettledAdvances", employeeAdvanceService.getUnsettledAdvances(employeeId));
        }

        return "employees/salary-form";
    }

    @PostMapping("/save")
    public String save(
            @RequestParam Long employeeId,
            @RequestParam String payPeriodMonth,
            @RequestParam BigDecimal grossSalary,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) List<Long> advanceIdsToSettle,
            RedirectAttributes redirectAttributes) {

        try {
            employeeSalaryService.recordSalaryPayment(
                    employeeId, payPeriodMonth, grossSalary, paymentDate, notes, advanceIdsToSettle
            );

            redirectAttributes.addFlashAttribute("successMessage", "Salary payment recorded successfully.");
            return "redirect:/salary";

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/salary/new?employeeId=" + employeeId;
        }
    }
}
