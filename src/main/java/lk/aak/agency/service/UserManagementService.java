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

    public UserManagementService(
            SystemUserRepository systemUserRepository,
            PasswordEncoder passwordEncoder) {

        this.systemUserRepository = systemUserRepository;
        this.passwordEncoder = passwordEncoder;
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

        user.setFullName(fullName);
        user.setRole(role);
        user.setEnabled(enabled);

        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        systemUserRepository.save(user);
    }

    private long countOtherEnabledAdmins(Long excludingId) {
        return getAllUsers().stream()
                .filter(user -> !user.getId().equals(excludingId))
                .filter(SystemUser::isEnabled)
                .filter(user -> "ADMIN".equals(user.getRole()))
                .count();
    }
}
