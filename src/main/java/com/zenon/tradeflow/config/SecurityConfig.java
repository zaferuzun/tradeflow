package com.zenon.tradeflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF korumasını kapat (Postman/Testler için kolaylık)
                .csrf(AbstractHttpConfigurer::disable)
                // Tüm isteklere (anyRequest) izin ver (permitAll)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}