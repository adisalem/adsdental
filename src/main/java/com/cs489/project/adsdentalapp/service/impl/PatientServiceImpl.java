package com.cs489.project.adsdentalapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs489.project.adsdentalapp.dto.address.AddressRequest;
import com.cs489.project.adsdentalapp.dto.address.AddressResponse;
import com.cs489.project.adsdentalapp.dto.auth.PatientRegistrationRequest;
import com.cs489.project.adsdentalapp.dto.patient.PatientRequest;
import com.cs489.project.adsdentalapp.dto.patient.PatientResponse;
import com.cs489.project.adsdentalapp.exception.DuplicateResourceException;
import com.cs489.project.adsdentalapp.exception.ResourceNotFoundException;
import com.cs489.project.adsdentalapp.model.Address;
import com.cs489.project.adsdentalapp.model.Patient;
import com.cs489.project.adsdentalapp.model.Role;
import com.cs489.project.adsdentalapp.model.User;
import com.cs489.project.adsdentalapp.model.UserRole;
import com.cs489.project.adsdentalapp.repository.PatientRepository;
import com.cs489.project.adsdentalapp.repository.RoleRepository;
import com.cs489.project.adsdentalapp.repository.UserRepository;
import com.cs489.project.adsdentalapp.repository.UserRoleRepository;
import com.cs489.project.adsdentalapp.service.PatientService;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientServiceImpl(PatientRepository patientRepository, UserRepository userRepository,
                             UserRoleRepository userRoleRepository, RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Patient registerPatient(PatientRegistrationRequest request) {
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

        // Create Address entity with data from request or defaults
        Address address = new Address();
        address.setAddressLine(request.getAddressLine() != null ? request.getAddressLine() : "Not Provided");
        address.setCity(request.getCity() != null ? request.getCity() : "Not Provided");
        address.setState(request.getState() != null ? request.getState() : "Not Provided");
        address.setPostalCode(request.getPostalCode() != null ? request.getPostalCode() : "Not Provided");

        // Create Patient entity and associate with User
        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setEmail(request.getEmail());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setHasUnpaidBill(false);
        patient.setAddress(address);
        patient.setUser(user);  // Cascade will save User

        // Save patient (User and Address will cascade save through Patient)
        patient = patientRepository.save(patient);

        // Assign ROLE_PATIENT role
        Role patientRole = roleRepository.findByRoleName("ROLE_PATIENT")
                .orElseGet(() -> {
                    Role newRole = new Role("ROLE_PATIENT");
                    return roleRepository.save(newRole);
                });

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(patientRole);
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);

        log.info("Patient registered successfully: {}", request.getEmail());
        return patient;
    }

    @Override
    public PatientResponse createPatient(PatientRequest request) {
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

        // Create Patient with User
        Patient patient = Patient.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .email(request.getEmail())
            .dateOfBirth(request.getDateOfBirth())
            .hasUnpaidBill(request.isHasUnpaidBill())
            .address(toAddressEntity(request.getAddress()))
            .user(user)
            .build();

        Patient savedPatient = patientRepository.save(patient);

        // Assign ROLE_PATIENT role
        Role patientRole = roleRepository.findByRoleName("ROLE_PATIENT")
                .orElseGet(() -> {
                    Role newRole = new Role("ROLE_PATIENT");
                    return roleRepository.save(newRole);
                });

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(patientRole);
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);

        log.info("Patient created successfully by office manager: {}", request.getEmail());
        return toResponse(savedPatient);
    }

    @Override
    public PatientResponse updatePatient(Long patientId, PatientRequest request) {
        Patient existingPatient = patientRepository.findById(patientId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));

        validateEmailUniqueness(request.getEmail(), patientId);

        existingPatient.setFirstName(request.getFirstName());
        existingPatient.setLastName(request.getLastName());
        existingPatient.setPhoneNumber(request.getPhoneNumber());
        existingPatient.setEmail(request.getEmail());
        existingPatient.setDateOfBirth(request.getDateOfBirth());
        existingPatient.setHasUnpaidBill(request.isHasUnpaidBill());

        // Update existing address instead of replacing
        if (existingPatient.getAddress() != null && request.getAddress() != null) {
            existingPatient.getAddress().setAddressLine(request.getAddress().getAddressLine());
            existingPatient.getAddress().setCity(request.getAddress().getCity());
            existingPatient.getAddress().setState(request.getAddress().getState());
            existingPatient.getAddress().setPostalCode(request.getAddress().getPostalCode());
        } else if (request.getAddress() != null) {
            existingPatient.setAddress(toAddressEntity(request.getAddress()));
        }

        return toResponse(patientRepository.save(existingPatient));
    }

    private void validateEmailUniqueness(String email, Long currentPatientId) {
        if (email == null || email.isBlank()) {
            return;
        }

        patientRepository.findByEmail(email)
            .filter(existing -> currentPatientId == null || !existing.getId().equals(currentPatientId))
            .ifPresent(existing -> {
                throw new DuplicateResourceException("Patient email already exists: " + email);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAllByOrderByLastNameAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatients(String searchString) {
        return patientRepository.searchByAnyField(searchString).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public void deletePatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id " + patientId);
        }

        patientRepository.deleteById(patientId);
    }

    private Patient toEntity(PatientRequest request) {
        return Patient.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .email(request.getEmail())
            .dateOfBirth(request.getDateOfBirth())
            .hasUnpaidBill(request.isHasUnpaidBill())
            .address(toAddressEntity(request.getAddress()))
            .build();
    }

    private Address toAddressEntity(AddressRequest request) {
        return Address.builder()
            .addressLine(request.getAddressLine())
            .city(request.getCity())
            .state(request.getState())
            .postalCode(request.getPostalCode())
            .build();
    }

    private PatientResponse toResponse(Patient patient) {
        return PatientResponse.builder()
            .id(patient.getId())
            .firstName(patient.getFirstName())
            .lastName(patient.getLastName())
            .phoneNumber(patient.getPhoneNumber())
            .email(patient.getEmail())
            .dateOfBirth(patient.getDateOfBirth())
            .hasUnpaidBill(patient.isHasUnpaidBill())
            .address(toAddressResponse(patient.getAddress()))
            .build();
    }

    private AddressResponse toAddressResponse(Address address) {
        return AddressResponse.builder()
            .id(address.getId())
            .addressLine(address.getAddressLine())
            .city(address.getCity())
            .state(address.getState())
            .postalCode(address.getPostalCode())
            .build();
    }
}