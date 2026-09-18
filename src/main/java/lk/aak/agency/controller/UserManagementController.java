package lk.aak.agency.controller;

import lk.aak.agency.model.SystemUser;
import lk.aak.agency.service.UserManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Owner-only: create and manage logins for office staff, sales reps and drivers. */
@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public String showUserList(Model model) {
        model.addAttribute("users", userManagementService.getAllUsers());
        return "users/user-list";
    }

    @GetMapping("/new")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new SystemUser());
        model.addAttribute("pageTitle", "Add New User");
        return "users/user-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        SystemUser user = userManagementService.getUserById(id).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/users";
        }

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Edit User");
        return "users/user-form";
    }

    @PostMapping("/save")
    public String saveNewUser(
            @RequestParam String username,
            @RequestParam String fullName,
            @RequestParam String role,
            @RequestParam String password,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (password == null || password.length() < 8) {
            model.addAttribute("errorMessage", "Password must be at least 8 characters long.");
            model.addAttribute("pageTitle", "Add New User");

            SystemUser attempted = new SystemUser();
            attempted.setUsername(username);
            attempted.setFullName(fullName);
            attempted.setRole(role);
            model.addAttribute("user", attempted);

            return "users/user-form";
        }

        SystemUser newUser = new SystemUser();
        newUser.setUsername(username);
        newUser.setFullName(fullName);
        newUser.setRole(role);
        newUser.setEnabled(true);

        userManagementService.createUser(newUser, password);

        redirectAttributes.addFlashAttribute("successMessage", "User created successfully.");
        return "redirect:/users";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam String role,
            @RequestParam(defaultValue = "false") boolean enabled,
            @RequestParam(required = false) String password,
            RedirectAttributes redirectAttributes) {

        userManagementService.updateUser(id, fullName, role, enabled, password);

        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        return "redirect:/users";
    }
}
