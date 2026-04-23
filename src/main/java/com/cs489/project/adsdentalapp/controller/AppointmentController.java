package com.cs489.project.adsdentalapp.controller;

import com.cs489.project.adsdentalapp.dto.appointment.AppointmentRequest;
import com.cs489.project.adsdentalapp.dto.appointment.AppointmentResponse;
import com.cs489.project.adsdentalapp.dto.appointment.RescheduleAppointmentRequest;
import com.cs489.project.adsdentalapp.service.AppointmentService;
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
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/appointments")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT')")
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/adsweb/api/v1/appointments/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/appointment/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT')")
    public ResponseEntity<AppointmentResponse> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, request));
    }

    @GetMapping("/appointments/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT', 'DENTIST')")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/appointments/patient/{patientId}")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }

    @GetMapping("/appointments/dentist/{dentistId}")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'DENTIST')")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDentist(@PathVariable Long dentistId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDentist(dentistId));
    }

    @GetMapping("/appointments/surgery/{surgeryId}")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsBySurgery(@PathVariable Long surgeryId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsBySurgery(surgeryId));
    }

    @GetMapping("/appointments/patient/{patientId}/upcoming")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getUpcomingAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getUpcomingAppointmentsByPatient(patientId));
    }

    @GetMapping("/appointments/dentist/{dentistId}/upcoming")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'DENTIST')")
    public ResponseEntity<List<AppointmentResponse>> getUpcomingAppointmentsByDentist(@PathVariable Long dentistId) {
        return ResponseEntity.ok(appointmentService.getUpcomingAppointmentsByDentist(dentistId));
    }

    @GetMapping("/appointments/search")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<List<AppointmentResponse>> searchAppointments(
            @RequestParam(required = false, defaultValue = "") String patientSearchString,
            @RequestParam(required = false, defaultValue = "") String dentistSearchString,
            @RequestParam(required = false, defaultValue = "") String surgerySearchString) {
        return ResponseEntity.ok(appointmentService.searchAppointments(patientSearchString, dentistSearchString, surgerySearchString));
    }

    @DeleteMapping("/appointment/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT')")
    public ResponseEntity<Map<String, String>> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(Map.of("message", "Appointment deleted successfully"));
    }

    @PostMapping("/appointment/{id}/cancel")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT')")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
    }

    @PutMapping("/appointment/{id}/reschedule")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT')")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(@PathVariable Long id, 
                                                                      @Valid @RequestBody RescheduleAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, request));
    }

    // ========== PATIENT PORTAL ENDPOINTS ==========

    @GetMapping("/patient-portal/my-appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/patient-portal/upcoming-appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyUpcomingAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/patient-portal/appointment/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> getMyAppointmentDetails(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    // ========== DENTIST PORTAL ENDPOINTS ==========

    @GetMapping("/dentist-portal/my-appointments")
    @PreAuthorize("hasRole('DENTIST')")
    public ResponseEntity<List<AppointmentResponse>> getMyDentistAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/dentist-portal/upcoming-appointments")
    @PreAuthorize("hasRole('DENTIST')")
    public ResponseEntity<List<AppointmentResponse>> getMyUpcomingDentistAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/dentist-portal/appointment/{id}")
    @PreAuthorize("hasRole('DENTIST')")
    public ResponseEntity<AppointmentResponse> getMyDentistAppointmentDetails(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }
}
