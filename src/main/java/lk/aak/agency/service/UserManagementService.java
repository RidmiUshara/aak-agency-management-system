package lk.aak.agency.service;

import lk.aak.agency.model.SystemUser;
import lk.aak.agency.repository.SystemUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserManagementService {

    private final SystemUserRepository systemUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserManagementService(
            SystemUserRepository systemUserRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService) {

        this.systemUserRepository = systemUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public List<SystemUser> getAllUsers() {
        return systemUserRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));
    }

    public Optional<SystemUser> getUserById(Long id) {
        return systemUserRepository.findById(id);
    }

    public void createUser(SystemUser newUser, String rawPassword) {
        String username = newUser.getUsername().trim();

        if (systemUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "A user with the username \"" + username + "\" already exists."
            );
        }

        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));

        systemUserRepository.save(newUser);

        auditLogService.record(
                "USER_CREATED", "SystemUser", newUser.getId(),
                "Created user \"" + username + "\" with role " + newUser.getRole()
        );
    }

    public void updateUser(
            Long id,
            String fullName,
            String role,
            boolean enabled,
            String rawPassword) {

        SystemUser user = systemUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User account was not found."));

        boolean wasEnabledAdmin = "ADMIN".equals(user.getRole()) && user.isEnabled();
        boolean staysEnabledAdmin = "ADMIN".equals(role) && enabled;

        if (wasEnabledAdmin && !staysEnabledAdmin && countOtherEnabledAdmins(id) == 0) {
            throw new IllegalArgumentException(
                    "Cannot remove the last active Owner/Administrator account."
            );
        }

        String previousRole = user.getRole();
        boolean previousEnabled = user.isEnabled();
        boolean passwordChanged = rawPassword != null && !rawPassword.isBlank();

        user.setFullName(fullName);
        user.setRole(role);
        user.setEnabled(enabled);

        if (passwordChanged) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        systemUserRepository.save(user);

        StringBuilder details = new StringBuilder("Updated user \"" + user.getUsername() + "\"");

        if (!previousRole.equals(role)) {
            details.append(": role ").append(previousRole).append(" -> ").append(role);
        }

        if (previousEnabled != enabled) {
            details.append(", ").append(enabled ? "enabled" : "disabled");
        }

        if (passwordChanged) {
            details.append(", password reset");
        }

        auditLogService.record(
                "USER_UPDATED", "SystemUser", user.getId(), details.toString()
        );
    }

    private long countOtherEnabledAdmins(Long excludingId) {
        return getAllUsers().stream()
                .filter(user -> !user.getId().equals(excludingId))
                .filter(SystemUser::isEnabled)
                .filter(user -> "ADMIN".equals(user.getRole()))
                .count();
    }
}

