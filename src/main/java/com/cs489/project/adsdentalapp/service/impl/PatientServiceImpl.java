package com.cs489.project.adsdentalapp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs489.project.adsdentalapp.dto.address.AddressRequest;
import com.cs489.project.adsdentalapp.dto.address.AddressResponse;
import com.cs489.project.adsdentalapp.dto.patient.PatientRequest;
import com.cs489.project.adsdentalapp.dto.patient.PatientResponse;
import com.cs489.project.adsdentalapp.exception.DuplicateResourceException;
import com.cs489.project.adsdentalapp.exception.ResourceNotFoundException;
import com.cs489.project.adsdentalapp.model.Address;
import com.cs489.project.adsdentalapp.model.Patient;
import com.cs489.project.adsdentalapp.repository.PatientRepository;
import com.cs489.project.adsdentalapp.service.PatientService;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public PatientResponse createPatient(PatientRequest request) {
        validateEmailUniqueness(request.getEmail(), null);
        Patient patient = toEntity(request);
        Patient savedPatient = patientRepository.save(patient);
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
        existingPatient.setAddress(toAddressEntity(request.getAddress()));

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