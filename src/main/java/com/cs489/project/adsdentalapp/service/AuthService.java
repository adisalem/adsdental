package com.cs489.project.adsdentalapp.service;

import com.cs489.project.adsdentalapp.dto.auth.LoginRequest;
import com.cs489.project.adsdentalapp.dto.auth.RegisterRequest;
import com.cs489.project.adsdentalapp.dto.auth.AuthResponse;
import com.cs489.project.adsdentalapp.model.Role;
import com.cs489.project.adsdentalapp.model.User;
import com.cs489.project.adsdentalapp.model.UserRole;
import com.cs489.project.adsdentalapp.repository.UserRepository;
import com.cs489.project.adsdentalapp.repository.RoleRepository;
import com.cs489.project.adsdentalapp.repository.UserRoleRepository;
import com.cs489.project.adsdentalapp.util.JwtTokenProvider;
import com.cs489.project.adsdentalapp.exception.DuplicateResourceException;
import com.cs489.project.adsdentalapp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(true);

        user = userRepository.save(user);

        // Assign role (create if doesn't exist)
        Role role = roleRepository.findByRoleName("ROLE_" + request.getRole().toUpperCase())
                .orElseGet(() -> {
                    Role newRole = new Role("ROLE_" + request.getRole().toUpperCase());
                    return roleRepository.save(newRole);
                });

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);

        log.info("User registered successfully: {}", request.getEmail());

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, 
                    java.util.Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + request.getRole().toUpperCase())))
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken, (long) jwtExpiration / 1000, user.getEmail(), request.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String role = userRoleRepository.findByUser(user)
                    .stream()
                    .findFirst()
                    .map(ur -> ur.getRole().getRoleName().replace("ROLE_", ""))
                    .orElse("USER");

            log.info("User logged in successfully: {}", request.getEmail());

            return new AuthResponse(accessToken, refreshToken, (long) jwtExpiration / 1000, user.getEmail(), role);
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getEmail(), e);
            throw new ResourceNotFoundException("Invalid email or password");
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getEmail(), e);
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    public AuthResponse generateTokensForUser(User user) {
        // Get user's roles
        String role = userRoleRepository.findByUser(user)
                .stream()
                .findFirst()
                .map(ur -> ur.getRole().getRoleName().replace("ROLE_", ""))
                .orElse("USER");

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                    java.util.Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.toUpperCase())))
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken, (long) jwtExpiration / 1000, user.getEmail(), role);
    }

    public AuthResponse refreshToken(String token) {
        String bearerToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        if (!jwtTokenProvider.validateToken(bearerToken)) {
            throw new ResourceNotFoundException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(bearerToken);
        String[] roles = jwtTokenProvider.getRolesFromToken(bearerToken);

        String accessToken = jwtTokenProvider.generateAccessToken(
                new UsernamePasswordAuthenticationToken(email, null,
                    java.util.Arrays.stream(roles)
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .collect(java.util.stream.Collectors.toList()))
        );

        return new AuthResponse(accessToken, bearerToken, (long) jwtExpiration / 1000, email, roles.length > 0 ? roles[0] : "USER");
    }
}
