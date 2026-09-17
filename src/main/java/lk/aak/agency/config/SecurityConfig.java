package lk.aak.agency.config;

import lk.aak.agency.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService
            customUserDetailsService;

    public SecurityConfig(
            CustomUserDetailsService
                    customUserDetailsService) {

        this.customUserDetailsService =
                customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
                .userDetailsService(
                        customUserDetailsService
                )

                .authorizeHttpRequests(
                        authorization ->
                                authorization

                                        /*
                                         * Login page and public
                                         * resources are available
                                         * without authentication.
                                         */
                                        .requestMatchers(
                                                "/login",
                                                "/css/**",
                                                "/js/**",
                                                "/images/**",
                                                "/favicon.ico",
                                                "/error"
                                        )
                                        .permitAll()

                                        /*
                                         * Every other application
                                         * page requires a login.
                                         */
                                        .anyRequest()
                                        .authenticated()
                )

                .formLogin(
                        formLogin ->
                                formLogin
                                        .loginPage("/login")
                                        .loginProcessingUrl(
                                                "/login"
                                        )
                                        .defaultSuccessUrl(
                                                "/",
                                                true
                                        )
                                        .failureUrl(
                                                "/login?error"
                                        )
                                        .permitAll()
                )

                .logout(
                        logout ->
                                logout
                                        .logoutUrl("/logout")
                                        .logoutSuccessUrl(
                                                "/login?logout"
                                        )
                                        .invalidateHttpSession(
                                                true
                                        )
                                        .deleteCookies(
                                                "JSESSIONID"
                                        )
                                        .permitAll()
                );

        return http.build();
    }
}