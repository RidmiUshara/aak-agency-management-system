package lk.aak.agency.controller;

import jakarta.validation.Valid;
import lk.aak.agency.model.Employee;
import lk.aak.agency.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public String showEmployeeList(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());
        return "employees/employee-list";
    }

    @GetMapping("/new")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("pageTitle", "Add New Employee");
        return "employees/employee-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditEmployeeForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Employee employee = employeeService.getEmployeeById(id).orElse(null);

        if (employee == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found.");
            return "redirect:/employees";
        }

        model.addAttribute("employee", employee);
        model.addAttribute("pageTitle", "Edit Employee");
        return "employees/employee-form";
    }

    @PostMapping("/save")
    public String saveEmployee(
            @Valid Employee employee,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "pageTitle",
                    employee.getId() == null ? "Add New Employee" : "Edit Employee"
            );
            return "employees/employee-form";
        }

        employeeService.saveEmployee(employee);

        redirectAttributes.addFlashAttribute("successMessage", "Employee saved successfully.");
        return "redirect:/employees";
    }
}
