package lk.aak.agency.controller;

import lk.aak.agency.model.SystemUser;
import lk.aak.agency.repository.SystemUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {

    private final SystemUserRepository
            systemUserRepository;

    private final PasswordEncoder
            passwordEncoder;

    public AccountController(
            SystemUserRepository systemUserRepository,
            PasswordEncoder passwordEncoder) {

        this.systemUserRepository =
                systemUserRepository;

        this.passwordEncoder =
                passwordEncoder;
    }

    @GetMapping("/account/change-password")
    public String showChangePasswordPage() {

        return "change-password";
    }

    @PostMapping("/account/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            SystemUser systemUser =
                    systemUserRepository
                            .findByUsername(
                                    authentication.getName()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "User account was not found."
                                    )
                            );

            if (!passwordEncoder.matches(
                    currentPassword,
                    systemUser.getPassword())) {

                throw new IllegalArgumentException(
                        "Current password is incorrect."
                );
            }

            if (newPassword == null ||
                    newPassword.length() < 8) {

                throw new IllegalArgumentException(
                        "New password must contain at least 8 characters."
                );
            }

            if (!newPassword.equals(
                    confirmPassword)) {

                throw new IllegalArgumentException(
                        "New password and confirmation do not match."
                );
            }

            if (passwordEncoder.matches(
                    newPassword,
                    systemUser.getPassword())) {

                throw new IllegalArgumentException(
                        "New password must be different from the current password."
                );
            }

            systemUser.setPassword(
                    passwordEncoder.encode(
                            newPassword
                    )
            );

            systemUserRepository.save(systemUser);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Password changed successfully."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/account/change-password";
    }
}