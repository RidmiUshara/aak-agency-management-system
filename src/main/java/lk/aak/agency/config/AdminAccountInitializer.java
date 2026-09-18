package lk.aak.agency.config;

import lk.aak.agency.model.SystemUser;
import lk.aak.agency.repository.SystemUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer
        implements CommandLineRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AdminAccountInitializer.class
            );

    private final SystemUserRepository
            systemUserRepository;

    private final PasswordEncoder
            passwordEncoder;

    private final String adminUsername;
    private final String adminPassword;
    private final String adminFullName;

    public AdminAccountInitializer(
            SystemUserRepository systemUserRepository,
            PasswordEncoder passwordEncoder,

            @Value("${APP_ADMIN_USERNAME:admin}")
            String adminUsername,

            @Value("${APP_ADMIN_PASSWORD:}")
            String adminPassword,

            @Value("${APP_ADMIN_FULL_NAME:AAK Agency Owner}")
            String adminFullName) {

        this.systemUserRepository =
                systemUserRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.adminUsername =
                adminUsername;

        this.adminPassword =
                adminPassword;

        this.adminFullName =
                adminFullName;
    }

    @Override
    public void run(String... args) {

        String username =
                adminUsername == null
                        ? ""
                        : adminUsername.trim();

        boolean adminExists =
                !username.isBlank()
                        && systemUserRepository
                        .existsByUsername(username);

        if (adminExists) {

            LOGGER.info(
                    "AAK Agency administrator account already exists."
            );

            return;
        }

        if (username.isBlank()) {

            LOGGER.warn(
                    "Administrator account was not created because "
                            + "APP_ADMIN_USERNAME is empty."
            );

            return;
        }

        if (adminPassword == null
                || adminPassword.isBlank()) {

            LOGGER.warn(
                    "Administrator account was not created because "
                            + "APP_ADMIN_PASSWORD is not configured."
            );

            return;
        }

        if (adminPassword.length() < 8) {

            LOGGER.warn(
                    "Administrator account was not created because "
                            + "APP_ADMIN_PASSWORD must contain at least "
                            + "8 characters."
            );

            return;
        }

        SystemUser adminUser =
                new SystemUser();

        adminUser.setUsername(username);

        adminUser.setPassword(
                passwordEncoder.encode(
                        adminPassword
                )
        );

        adminUser.setFullName(
                adminFullName == null
                        || adminFullName.isBlank()
                        ? "AAK Agency Owner"
                        : adminFullName.trim()
        );

        adminUser.setRole("ADMIN");
        adminUser.setEnabled(true);

        systemUserRepository.save(adminUser);

        LOGGER.info(
                "AAK Agency administrator account created successfully."
        );
    }
}