package com.cs489.project.adsdentalapp.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.cs489.project.adsdentalapp.dto.patient.PatientResponse;
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

@SpringBootTest
@Transactional
@DisplayName("PatientService Integration Tests")
class PatientServiceIntegrationTest {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Patient testPatient;
    private User testUser;
    private Address testAddress;

    @BeforeEach
    @DisplayName("Setup test data before each test")
    void setUp() {
        testAddress = new Address();
        testAddress.setAddressLine("123 Main St");
        testAddress.setCity("Springfield");
        testAddress.setState("IL");
        testAddress.setPostalCode("62701");
        testUser = new User();
        testUser.setEmail("testpatient@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
        Role patientRole = roleRepository.findByRoleName("ROLE_PATIENT")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName("ROLE_PATIENT");
                    return roleRepository.save(newRole);
                });
        UserRole userRole = new UserRole();
        userRole.setUser(testUser);
        userRole.setRole(patientRole);
        userRole.setAssignedAt(LocalDateTime.now());
        userRoleRepository.save(userRole);
        testPatient = Patient.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("555-1234")
                .email("testpatient@example.com")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .hasUnpaidBill(false)
                .address(testAddress)
                .user(testUser)
                .build();

        testPatient = patientRepository.save(testPatient);
    }

    @Test
    @DisplayName("Test 1: getPatientById should return PatientResponse when patient exists")
    void testGetPatientById_WhenPatientExists_ShouldReturnPatientResponse() {
        Long existingPatientId = testPatient.getId();
        PatientResponse result = patientService.getPatientById(existingPatientId);
        assertNotNull(result, "PatientResponse should not be null");
        assertEquals(existingPatientId, result.getId(), "Patient ID should match");
        assertEquals("John", result.getFirstName(), "First name should match");
        assertEquals("Doe", result.getLastName(), "Last name should match");
        assertEquals("testpatient@example.com", result.getEmail(), "Email should match");
        assertEquals("555-1234", result.getPhoneNumber(), "Phone number should match");
        assertEquals(LocalDate.of(1990, 5, 15), result.getDateOfBirth(), "Date of birth should match");
        assertFalse(result.isHasUnpaidBill(), "Should have no unpaid bills");
        assertNotNull(result.getAddress(), "Address should not be null");
        assertEquals("Springfield", result.getAddress().getCity(), "City should match");
    }

    @Test
    @DisplayName("Test 2: getPatientById should throw ResourceNotFoundException when patient ID is invalid")
    void testGetPatientById_WhenPatientIdInvalid_ShouldThrowResourceNotFoundException() {
        Long invalidPatientId = 999999L;
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.getPatientById(invalidPatientId),
                "Should throw ResourceNotFoundException"
        );
        assertTrue(exception.getMessage().contains("Patient not found"),
                "Exception message should contain 'Patient not found'");
        assertTrue(exception.getMessage().contains("999999"),
                "Exception message should contain the invalid ID");
    }
}
