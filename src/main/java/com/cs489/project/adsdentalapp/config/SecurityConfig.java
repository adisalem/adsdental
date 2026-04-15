package com.cs489.project.adsdentalapp.config;

import com.cs489.project.adsdentalapp.util.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/api/auth/login", "/api/auth/register", "/h2-console/**").permitAll()
                .requestMatchers("/graphql").permitAll()
                // Public registration endpoints
                .requestMatchers(HttpMethod.POST, "/adsweb/api/v1/patients/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/adsweb/api/v1/dentists/register").permitAll()
                // Admin/Office Manager - Create, Update, Delete patients
                .requestMatchers(HttpMethod.POST, "/adsweb/api/v1/patients").hasAnyRole("ADMIN", "OFFICE_MANAGER")
                .requestMatchers(HttpMethod.PUT, "/adsweb/api/v1/patient/**").hasAnyRole("ADMIN", "OFFICE_MANAGER", "PATIENT")
                .requestMatchers(HttpMethod.DELETE, "/adsweb/api/v1/patient/**").hasRole("ADMIN")
                // Admin/Office Manager - Create, Update, Delete dentists
                .requestMatchers(HttpMethod.POST, "/adsweb/api/v1/dentists").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/adsweb/api/v1/dentist/**").hasAnyRole("ADMIN", "DENTIST")
                .requestMatchers(HttpMethod.DELETE, "/adsweb/api/v1/dentist/**").hasRole("ADMIN")
                // Patient endpoints - Read access
                .requestMatchers(HttpMethod.GET, "/adsweb/api/v1/patients/**").hasAnyRole("PATIENT", "DENTIST", "OFFICE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/adsweb/api/v1/patient/**").hasAnyRole("PATIENT", "DENTIST", "OFFICE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/adsweb/api/v1/dentists/**").hasAnyRole("PATIENT", "DENTIST", "OFFICE_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/adsweb/api/v1/dentist/**").hasAnyRole("PATIENT", "DENTIST", "OFFICE_MANAGER", "ADMIN")
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    
                    Map<String, Object> errorBody = new LinkedHashMap<>();
                    errorBody.put("timestamp", Instant.now().toString());
                    errorBody.put("status", 401);
                    errorBody.put("error", "Unauthorized");
                    errorBody.put("message", "Authentication failed: missing or invalid token");
                    errorBody.put("path", request.getRequestURI());
                    
                    response.getWriter().write(new ObjectMapper().writeValueAsString(errorBody));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    
                    Map<String, Object> errorBody = new LinkedHashMap<>();
                    errorBody.put("timestamp", Instant.now().toString());
                    errorBody.put("status", 403);
                    errorBody.put("error", "Forbidden");
                    errorBody.put("message", "Access denied: insufficient permissions for this operation");
                    errorBody.put("path", request.getRequestURI());
                    
                    response.getWriter().write(new ObjectMapper().writeValueAsString(errorBody));
                })
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .httpBasic(httpBasic -> httpBasic.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // For H2 console

        return http.build();
    }
}
