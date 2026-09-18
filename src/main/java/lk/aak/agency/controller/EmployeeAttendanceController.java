package lk.aak.agency.controller;

import lk.aak.agency.service.EmployeeAttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/attendance")
public class EmployeeAttendanceController {

    private final EmployeeAttendanceService employeeAttendanceService;

    public EmployeeAttendanceController(EmployeeAttendanceService employeeAttendanceService) {
        this.employeeAttendanceService = employeeAttendanceService;
    }

    @GetMapping
    public String showAttendanceSheet(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            Model model) {

        LocalDate selectedDate = date != null ? date : LocalDate.now();

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("rows", employeeAttendanceService.getAttendanceSheetForDate(selectedDate));

        return "employees/attendance-sheet";
    }

    @PostMapping("/mark")
    public String markAttendance(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            employeeAttendanceService.markAttendance(employeeId, date, status, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Attendance marked.");

        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/attendance?date=" + date;
    }
}
