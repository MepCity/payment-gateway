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
                // Public endpoints
                .requestMatchers("/health", "/actuator/**").permitAll()
                .requestMatchers("/v1/payments/**").permitAll()  // Ödeme endpoint'leri public
                .requestMatchers("/v1/customers/**").permitAll()  // Customer endpoint'leri public
                .requestMatchers("/v1/mandates/**").permitAll()   // Mandate endpoint'leri public
                .requestMatchers("/v1/refunds/**").permitAll()    // Refund endpoint'leri public
                .requestMatchers("/v1/disputes/**").permitAll()   // Dispute endpoint'leri public
                .requestMatchers("/v1/payouts/**").permitAll()    // Payout endpoint'leri public
                .requestMatchers("/v1/webhooks/**").permitAll()   // Webhook endpoint'leri public
                .requestMatchers("/v1/3dsecure/**").permitAll()   // 3D Secure endpoint'leri public
                .requestMatchers("/v1/blacklist/**").permitAll()  // Blacklist endpoint'leri public
                .requestMatchers("/bank-webhooks/**").permitAll() // Bank webhook'ları public
                .requestMatchers("/mock/**").permitAll()          // Mock endpoint'leri public
                .requestMatchers("/auth/**").permitAll()          // Auth endpoint'leri public
                .requestMatchers("/public/**").permitAll()        // Public endpoint'ler
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
