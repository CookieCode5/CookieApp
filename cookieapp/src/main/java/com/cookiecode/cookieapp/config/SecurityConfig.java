package com.cookiecode.cookieapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Le pom contient spring-boot-starter-security : sans cette classe, TOUTES les pages
 * redirigent vers le formulaire de connexion par défaut de Spring Security.
 * Comme dans le plan, on ne garde que BCrypt ; la protection des pages se fera avec
 * l'intercepteur de session (ConnexionInterceptor, Amina).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // CSRF désactivé pour rester simple (console H2, formulaires + session maison)
                .csrf(csrf -> csrf.disable())
                // console H2 affichée dans des frames de la même origine
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
