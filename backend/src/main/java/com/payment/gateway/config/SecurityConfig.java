package com.payment.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - Allow all for testing
                .requestMatchers("/api/v1/blacklist/**").permitAll() // Blacklist endpoint'leri public
                .requestMatchers("/api/v1/payments/**").permitAll()  // Payment endpoint'leri public  
                .requestMatchers("/bank-webhooks/**").permitAll()    // Bank webhook'ları public
                .requestMatchers("/mock/**").permitAll()             // Mock endpoint'leri public
                .requestMatchers("/auth/**").permitAll()             // Auth endpoint'leri public
                .requestMatchers("/public/**").permitAll()           // Public endpoint'ler
                .requestMatchers("/api/**").permitAll()              // Tüm API endpoint'leri public
                
                // All other requests
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
