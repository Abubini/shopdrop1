package com.shopdrop.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Two roles:
 *   USER  - can browse, buy, and manage their own account/orders/reviews.
 *   ADMIN - manages the catalog and order pipeline, but does not shop: cart and checkout
 *           are USER-only, both in the nav (fragments.html) and enforced here.
 *
 * The catalog stays public so anyone can window-shop; buying, reviewing and account pages
 * require login; /admin/** requires the ADMIN role.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Sends admins to their dashboard and everyone else to their account page after login. */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals);
            response.sendRedirect(request.getContextPath() + (isAdmin ? "/admin" : "/account"));
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/products/*/reviews").authenticated()
                .requestMatchers("/", "/products", "/products/**", "/register", "/login",
                        "/css/**", "/webjars/**", "/403").permitAll()
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .requestMatchers("/cart", "/cart/**", "/checkout", "/checkout/**").hasRole("USER")
                .requestMatchers("/orders/**", "/account", "/account/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(roleBasedSuccessHandler())
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?loggedout")
                .permitAll()
            )
            .exceptionHandling(handling -> handling
                .accessDeniedPage("/403")
            );

        return http.build();
    }
}
