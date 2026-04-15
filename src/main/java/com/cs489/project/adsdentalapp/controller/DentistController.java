package com.cs489.project.adsdentalapp.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.cs489.project.adsdentalapp.dto.auth.DentistRegistrationRequest;
import com.cs489.project.adsdentalapp.dto.dentist.DentistRequest;
import com.cs489.project.adsdentalapp.dto.dentist.DentistResponse;
import com.cs489.project.adsdentalapp.model.Dentist;
import com.cs489.project.adsdentalapp.service.AuthService;
import com.cs489.project.adsdentalapp.service.DentistService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/adsweb/api/v1")
@Validated
public class DentistController {

    private final DentistService dentistService;
    private final AuthService authService;

    public DentistController(DentistService dentistService, AuthService authService) {
        this.dentistService = dentistService;
        this.authService = authService;
    }

    @PostMapping("/dentists/register")
    public ResponseEntity<AuthResponse> registerDentist(@Valid @RequestBody DentistRegistrationRequest request) {
        Dentist dentist = dentistService.registerDentist(request);
        AuthResponse response = authService.generateTokensForUser(dentist.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/dentists")
    public ResponseEntity<DentistResponse> createDentist(@Valid @RequestBody DentistRequest request) {
        DentistResponse response = dentistService.createDentist(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/adsweb/api/v1/dentists/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/dentist/{id}")
    public ResponseEntity<DentistResponse> updateDentist(@PathVariable Long id, @Valid @RequestBody DentistRequest request) {
        return ResponseEntity.ok(dentistService.updateDentist(id, request));
    }

    @GetMapping("/dentists/{id}")
    public ResponseEntity<DentistResponse> getDentistById(@PathVariable Long id) {
        return ResponseEntity.ok(dentistService.getDentistById(id));
    }

    @GetMapping("/dentists")
    public ResponseEntity<List<DentistResponse>> getAllDentists() {
        return ResponseEntity.ok(dentistService.getAllDentists());
    }

    @DeleteMapping("/dentist/{id}")
    public ResponseEntity<Map<String, String>> deleteDentist(@PathVariable Long id) {
        dentistService.deleteDentist(id);
        return ResponseEntity.ok(Map.of("message", "Dentist deleted successfully"));
    }

    @GetMapping("/dentist/search/{searchString}")
    public ResponseEntity<List<DentistResponse>> searchDentists(@PathVariable String searchString) {
        return ResponseEntity.ok(dentistService.searchDentists(searchString));
    }
}
