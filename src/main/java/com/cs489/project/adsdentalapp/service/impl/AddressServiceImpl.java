package com.cs489.project.adsdentalapp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs489.project.adsdentalapp.dto.address.AddressResponse;
import com.cs489.project.adsdentalapp.dto.patient.PatientSummaryResponse;
import com.cs489.project.adsdentalapp.model.Address;
import com.cs489.project.adsdentalapp.model.Patient;
import com.cs489.project.adsdentalapp.repository.AddressRepository;
import com.cs489.project.adsdentalapp.service.AddressService;

@Service
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public List<AddressResponse> getAllAddresses() {
        return addressRepository.findAllWithPatientOrderByCityAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
            .id(address.getId())
            .addressLine(address.getAddressLine())
            .city(address.getCity())
            .state(address.getState())
            .postalCode(address.getPostalCode())
            .patient(toPatientSummary(address.getPatient()))
            .build();
    }

    private PatientSummaryResponse toPatientSummary(Patient patient) {
        if (patient == null) {
            return null;
        }

        return PatientSummaryResponse.builder()
            .id(patient.getId())
            .firstName(patient.getFirstName())
            .lastName(patient.getLastName())
            .phoneNumber(patient.getPhoneNumber())
            .email(patient.getEmail())
            .build();
    }
}