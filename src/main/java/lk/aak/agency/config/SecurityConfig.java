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
 * Roles (per the AAK Agency business proposal's "Access by role" table):
 * - ADMIN: the Owner/Distributor. Full access; only role allowed to delete
 *   records or manage other users' logins.
 * - OFFICE: daily operational staff. Stock, bills, purchasing, payments,
 *   cheques and collections - everything except user management and deletes.
 * - SALES_REP: sales representatives. Shops (customers) and bills (sales
 *   invoices) only - no purchasing, payments or inventory access.
 * - DRIVER: reserved for drivers/cash collectors. No module access yet -
 *   the proposal treats this as "optional later access", office enters on
 *   their behalf for now.
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
                                         * User account management is an Owner-only function.
                                         */
                                        .requestMatchers("/users/**")
                                        .hasRole("ADMIN")

                                        /*
                                         * Sales reps only see shops (customers) and bills
                                         * (sales invoices) - no purchasing/financial access.
                                         */
                                        .requestMatchers(
                                                "/customers/**",
                                                "/sales-invoices/**"
                                        )
                                        .hasAnyRole("ADMIN", "OFFICE", "SALES_REP")

                                        /*
                                         * Office-only operational pipeline: purchasing,
                                         * products, stock and payments/collections.
                                         */
                                        .requestMatchers(
                                                "/products/**",
                                                "/purchase-invoices/**",
                                                "/inventory/**",
                                                "/payments/**",
                                                "/cheques/**",
                                                "/collections/**"
                                        )
                                        .hasAnyRole("ADMIN", "OFFICE")

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