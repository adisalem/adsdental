package com.cs489.project.adsdentalapp.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cs489.project.adsdentalapp.dto.auth.AuthResponse;
import com.cs489.project.adsdentalapp.dto.auth.PatientRegistrationRequest;
import com.cs489.project.adsdentalapp.dto.patient.PatientRequest;
import com.cs489.project.adsdentalapp.dto.patient.PatientResponse;
import com.cs489.project.adsdentalapp.model.Patient;
import com.cs489.project.adsdentalapp.service.AuthService;
import com.cs489.project.adsdentalapp.service.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/adsweb/api/v1")
@Validated
public class PatientController {

    private final PatientService patientService;
    private final AuthService authService;

    public PatientController(PatientService patientService, AuthService authService) {
        this.patientService = patientService;
        this.authService = authService;
    }

    @PostMapping("/patients/register")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody PatientRegistrationRequest request) {
        Patient patient = patientService.registerPatient(request);
        AuthResponse response = authService.generateTokensForUser(patient.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/patients")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        PatientResponse response = patientService.createPatient(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/adsweb/api/v1/patients/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/patient/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT')")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    @GetMapping("/patients/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT')")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/patients")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @DeleteMapping("/patient/{id}")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<Map<String, String>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(Map.of("message", "Patient deleted successfully"));
    }

    @GetMapping("/patient/search/{searchString}")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<List<PatientResponse>> searchPatients(@PathVariable String searchString) {
        return ResponseEntity.ok(patientService.searchPatients(searchString));
    }
}