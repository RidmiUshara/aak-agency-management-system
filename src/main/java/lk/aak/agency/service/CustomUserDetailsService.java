package lk.aak.agency.service;

import lk.aak.agency.model.SystemUser;
import lk.aak.agency.repository.SystemUserRepository;
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

    public CustomUserDetailsService(
            SystemUserRepository systemUserRepository) {

        this.systemUserRepository =
                systemUserRepository;
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
                .disabled(
                        !systemUser.isEnabled()
                )
                .build();
    }
}