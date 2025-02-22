package com.example.resourceserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final MyJwtAuthenticationConverter myJwtAuthenticationConverter;

    public SecurityConfig(MyJwtAuthenticationConverter myJwtAuthenticationConverter) {
        this.myJwtAuthenticationConverter = myJwtAuthenticationConverter;
    }

    private final String[] publicEndpoints = {"/api/books/count", "/api/books", "/api/books/image/**", "/api/categories/**"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(
                        authorizeRequests -> authorizeRequests
                                .requestMatchers(HttpMethod.GET, publicEndpoints).permitAll()
                                .anyRequest().hasAnyAuthority("SCOPE_access-api")
                );

        http.sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http
                .csrf(
                        AbstractHttpConfigurer::disable
                );

        http.oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(
                                jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(myJwtAuthenticationConverter)
                        )
        );

        return http.build();
    }

}
