package com.cs489.project.adsdentalapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs489.project.adsdentalapp.dto.auth.DentistRegistrationRequest;
import com.cs489.project.adsdentalapp.dto.dentist.DentistRequest;
import com.cs489.project.adsdentalapp.dto.dentist.DentistResponse;
import com.cs489.project.adsdentalapp.exception.DuplicateResourceException;
import com.cs489.project.adsdentalapp.exception.ResourceNotFoundException;
import com.cs489.project.adsdentalapp.model.Dentist;
import com.cs489.project.adsdentalapp.model.Role;
import com.cs489.project.adsdentalapp.model.User;
import com.cs489.project.adsdentalapp.model.UserRole;
import com.cs489.project.adsdentalapp.repository.DentistRepository;
import com.cs489.project.adsdentalapp.repository.RoleRepository;
import com.cs489.project.adsdentalapp.repository.UserRepository;
import com.cs489.project.adsdentalapp.repository.UserRoleRepository;
import com.cs489.project.adsdentalapp.service.DentistService;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class DentistServiceImpl implements DentistService {

    private final DentistRepository dentistRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DentistServiceImpl(DentistRepository dentistRepository, UserRepository userRepository,
                             UserRoleRepository userRoleRepository, RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder) {
        this.dentistRepository = dentistRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Dentist registerDentist(DentistRegistrationRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        // Create User entity with password
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(true);

        // Create Dentist entity and associate with User
        Dentist dentist = new Dentist();
        dentist.setFirstName(request.getFirstName());
        dentist.setLastName(request.getLastName());
        dentist.setPhoneNumber(request.getPhoneNumber());
        dentist.setEmail(request.getEmail());
        dentist.setSpecialization(request.getSpecialization());
        dentist.setUser(user);  // Cascade will save User

        // Save dentist (User will cascade save through Dentist)
        dentist = dentistRepository.save(dentist);

        // Assign ROLE_DENTIST role
        Role dentistRole = roleRepository.findByRoleName("ROLE_DENTIST")
                .orElseGet(() -> {
                    Role newRole = new Role("ROLE_DENTIST");
                    return roleRepository.save(newRole);
                });

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(dentistRole);
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);

        log.info("Dentist registered successfully: {}", request.getEmail());
        return dentist;
    }

    @Override
    public DentistResponse createDentist(DentistRequest request) {
        validateEmailUniqueness(request.getEmail(), null);
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        // Create User entity with a temporary password
        String temporaryPassword = "TempPassword" + System.currentTimeMillis();
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(true);
        user = userRepository.save(user);

        // Create Dentist with User
        Dentist dentist = Dentist.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .email(request.getEmail())
            .specialization(request.getSpecialization())
            .isActive(true)
            .user(user)
            .build();

        Dentist savedDentist = dentistRepository.save(dentist);

        // Assign ROLE_DENTIST role
        Role dentistRole = roleRepository.findByRoleName("ROLE_DENTIST")
                .orElseGet(() -> {
                    Role newRole = new Role("ROLE_DENTIST");
                    return roleRepository.save(newRole);
                });

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(dentistRole);
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);

        log.info("Dentist created successfully by admin: {}", request.getEmail());
        return toResponse(savedDentist);
    }

    @Override
    public DentistResponse updateDentist(Long dentistId, DentistRequest request) {
        Dentist existingDentist = dentistRepository.findById(dentistId)
            .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id " + dentistId));

        validateEmailUniqueness(request.getEmail(), dentistId);

        existingDentist.setFirstName(request.getFirstName());
        existingDentist.setLastName(request.getLastName());
        existingDentist.setPhoneNumber(request.getPhoneNumber());
        existingDentist.setEmail(request.getEmail());
        existingDentist.setSpecialization(request.getSpecialization());

        return toResponse(dentistRepository.save(existingDentist));
    }

    private void validateEmailUniqueness(String email, Long currentDentistId) {
        if (email == null || email.isBlank()) {
            return;
        }

        dentistRepository.findByEmail(email)
            .filter(existing -> currentDentistId == null || !existing.getId().equals(currentDentistId))
            .ifPresent(existing -> {
                throw new DuplicateResourceException("Dentist email already exists: " + email);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public DentistResponse getDentistById(Long dentistId) {
        return dentistRepository.findById(dentistId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id " + dentistId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponse> getAllDentists() {
        return dentistRepository.findAllByOrderByLastNameAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponse> searchDentists(String searchString) {
        return dentistRepository.searchByAnyField(searchString).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public void deleteDentist(Long dentistId) {
        if (!dentistRepository.existsById(dentistId)) {
            throw new ResourceNotFoundException("Dentist not found with id " + dentistId);
        }

        dentistRepository.deleteById(dentistId);
    }

    private Dentist toEntity(DentistRequest request) {
        return Dentist.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .email(request.getEmail())
            .specialization(request.getSpecialization())
            .build();
    }

    private DentistResponse toResponse(Dentist dentist) {
        return DentistResponse.builder()
            .id(dentist.getId())
            .firstName(dentist.getFirstName())
            .lastName(dentist.getLastName())
            .phoneNumber(dentist.getPhoneNumber())
            .email(dentist.getEmail())
            .specialization(dentist.getSpecialization())
            .build();
    }
}
