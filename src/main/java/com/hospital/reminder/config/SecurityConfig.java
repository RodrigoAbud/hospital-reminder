package com.hospital.reminder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiKeyAuthenticationFilter apiKeyAuthenticationFilter)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/api-docs/**",
                    "/api-docs",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Endpoints que requerem API Key
                .requestMatchers("/api/**").authenticated()
                // Qualquer outra requisição deve ser negada
                .anyRequest().denyAll()
            )
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter(
            @Value("${security.api-key.header-name:X-API-KEY}") String apiKeyHeaderName,
            @Value("${security.api-key.secret-key:hospital-api-key-2024-secret}") String apiKeySecret) {
        return new ApiKeyAuthenticationFilter(apiKeyHeaderName, apiKeySecret);
    }
}
