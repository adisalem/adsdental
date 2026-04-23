package com.cs489.project.adsdentalapp.controller;

import com.cs489.project.adsdentalapp.dto.surgery.SurgeryRequest;
import com.cs489.project.adsdentalapp.dto.surgery.SurgeryResponse;
import com.cs489.project.adsdentalapp.service.SurgeryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adsweb/api/v1")
@Validated
public class SurgeryController {

    private final SurgeryService surgeryService;

    public SurgeryController(SurgeryService surgeryService) {
        this.surgeryService = surgeryService;
    }

    @PostMapping("/surgeries")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<SurgeryResponse> createSurgery(@Valid @RequestBody SurgeryRequest request) {
        SurgeryResponse response = surgeryService.createSurgery(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/adsweb/api/v1/surgeries/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/surgery/{id}")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<SurgeryResponse> updateSurgery(@PathVariable Long id, @Valid @RequestBody SurgeryRequest request) {
        return ResponseEntity.ok(surgeryService.updateSurgery(id, request));
    }

    @GetMapping("/surgeries/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'DENTIST', 'PATIENT')")
    public ResponseEntity<SurgeryResponse> getSurgeryById(@PathVariable Long id) {
        return ResponseEntity.ok(surgeryService.getSurgeryById(id));
    }

    @GetMapping("/surgeries")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'DENTIST', 'PATIENT')")
    public ResponseEntity<List<SurgeryResponse>> getAllSurgeries() {
        return ResponseEntity.ok(surgeryService.getAllSurgeries());
    }

    @GetMapping("/surgery/search/{searchString}")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<List<SurgeryResponse>> searchSurgeries(@PathVariable String searchString) {
        return ResponseEntity.ok(surgeryService.searchSurgeries(searchString));
    }

    @DeleteMapping("/surgery/{id}")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<Map<String, String>> deleteSurgery(@PathVariable Long id) {
        surgeryService.deleteSurgery(id);
        return ResponseEntity.ok(Map.of("message", "Surgery deleted successfully"));
    }
}
