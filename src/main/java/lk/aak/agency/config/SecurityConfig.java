package lk.aak.agency.config;

import lk.aak.agency.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Roles: ADMIN (full access, only role allowed to delete records),
 * SALES (customers + sales invoices), INVENTORY (products + purchase
 * invoices + stock), ACCOUNTS (payments + cheques + collections).
 */
@Configuration
@EnableMethodSecurity
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
                                                "/error",
                                                "/actuator/health"
                                        )
                                        .permitAll()

                                        /*
                                         * Remaining actuator endpoints (e.g. /actuator/info)
                                         * expose build/runtime details - admins only.
                                         */
                                        .requestMatchers("/actuator/**")
                                        .hasRole("ADMIN")

                                        /*
                                         * Sales pipeline: customer records and sales invoices.
                                         */
                                        .requestMatchers(
                                                "/customers/**",
                                                "/sales-invoices/**"
                                        )
                                        .hasAnyRole("ADMIN", "SALES")

                                        /*
                                         * Inventory pipeline: products, purchasing and stock.
                                         */
                                        .requestMatchers(
                                                "/products/**",
                                                "/purchase-invoices/**",
                                                "/inventory/**"
                                        )
                                        .hasAnyRole("ADMIN", "INVENTORY")

                                        /*
                                         * Accounts pipeline: payments, cheques and collections.
                                         */
                                        .requestMatchers(
                                                "/payments/**",
                                                "/cheques/**",
                                                "/collections/**"
                                        )
                                        .hasAnyRole("ADMIN", "ACCOUNTS")

                                        /*
                                         * Every other application
                                         * page just requires a login
                                         * (dashboard, reports, account settings).
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