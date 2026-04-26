package com.cs489.project.adsdentalapp.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cs489.project.adsdentalapp.dto.address.AddressResponse;
import com.cs489.project.adsdentalapp.dto.patient.PatientResponse;
import com.cs489.project.adsdentalapp.service.AuthService;
import com.cs489.project.adsdentalapp.service.PatientService;

@DisplayName("PatientController Unit Tests - REST API Endpoint for getAllPatients")
class PatientControllerUnitTest {

    @Mock
    private PatientService patientService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private PatientController patientController;

    private PatientResponse patient1;
    private PatientResponse patient2;
    private List<PatientResponse> allPatients;

    @BeforeEach
    @DisplayName("Setup test data and mocks before each test")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AddressResponse address1 = AddressResponse.builder()
                .id(1L)
                .addressLine("123 Main St")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .build();
        AddressResponse address2 = AddressResponse.builder()
                .id(2L)
                .addressLine("456 Oak Ave")
                .city("Shelbyville")
                .state("IL")
                .postalCode("62702")
                .build();
        patient1 = PatientResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("555-1234")
                .email("john.doe@example.com")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .hasUnpaidBill(false)
                .address(address1)
                .build();
        patient2 = PatientResponse.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("555-5678")
                .email("jane.smith@example.com")
                .dateOfBirth(LocalDate.of(1992, 8, 22))
                .hasUnpaidBill(true)
                .address(address2)
                .build();
        allPatients = Arrays.asList(patient1, patient2);
    }

    @Test
    @DisplayName("TEST 1: getAllPatients should return list of all patients")
    void testGetAllPatients_ShouldReturnAllPatients() {
        when(patientService.getAllPatients()).thenReturn(allPatients);
        var response = patientController.getAllPatients();
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCode().value(), "HTTP status should be 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(2, response.getBody().size(), "Should return 2 patients");
        assertEquals(1L, response.getBody().get(0).getId(), "First patient ID should be 1");
        assertEquals("John", response.getBody().get(0).getFirstName(), "First patient first name should be John");
        assertEquals("Doe", response.getBody().get(0).getLastName(), "First patient last name should be Doe");
        assertEquals("john.doe@example.com", response.getBody().get(0).getEmail());
        assertEquals(2L, response.getBody().get(1).getId(), "Second patient ID should be 2");
        assertEquals("Jane", response.getBody().get(1).getFirstName(), "Second patient first name should be Jane");
        assertEquals("Smith", response.getBody().get(1).getLastName(), "Second patient last name should be Smith");
        assertEquals("jane.smith@example.com", response.getBody().get(1).getEmail());
        verify(patientService, times(1)).getAllPatients();
        verifyNoMoreInteractions(patientService);
    }

    @Test
    @DisplayName("TEST 2: getAllPatients should return empty list when no patients exist")
    void testGetAllPatients_WhenNoPatientsExist_ShouldReturnEmptyList() {
        when(patientService.getAllPatients()).thenReturn(List.of());
        var response = patientController.getAllPatients();
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCode().value(), "HTTP status should be 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(0, response.getBody().size(), "Should return empty list");
        assertTrue(response.getBody().isEmpty(), "List should be empty");
        verify(patientService, times(1)).getAllPatients();
    }

    @Test
    @DisplayName("TEST 3: getAllPatients properly mocks the service layer")
    void testGetAllPatients_VerifyMockingBehavior() {
        when(patientService.getAllPatients()).thenReturn(allPatients);
        var response1 = patientController.getAllPatients();
        var response2 = patientController.getAllPatients();
        assertNotNull(response1.getBody());
        assertNotNull(response2.getBody());
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        verify(patientService, times(2)).getAllPatients();
        verifyNoMoreInteractions(patientService, authService);
    }
}
