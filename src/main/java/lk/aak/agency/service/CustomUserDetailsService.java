package lk.aak.agency.service;

import lk.aak.agency.model.SystemUser;
import lk.aak.agency.repository.SystemUserRepository;
import lk.aak.agency.security.LoginAttemptService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final SystemUserRepository
            systemUserRepository;

    private final LoginAttemptService loginAttemptService;

    public CustomUserDetailsService(
            SystemUserRepository systemUserRepository,
            LoginAttemptService loginAttemptService) {

        this.systemUserRepository =
                systemUserRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        SystemUser systemUser =
                systemUserRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User account was not found."
                                )
                        );

        return User.builder()
                .username(
                        systemUser.getUsername()
                )
                .password(
                        systemUser.getPassword()
                )
                .roles(
                        systemUser.getRole()
                )
                .accountLocked(
                        loginAttemptService.isLocked(systemUser.getUsername())
                )
                .disabled(
                        !systemUser.isEnabled()
                )
                .build();
    }
}