package com.cs489.project.adsdentalapp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs489.project.adsdentalapp.dto.address.AddressRequest;
import com.cs489.project.adsdentalapp.dto.address.AddressResponse;
import com.cs489.project.adsdentalapp.dto.patient.PatientSummaryResponse;
import com.cs489.project.adsdentalapp.exception.ResourceNotFoundException;
import com.cs489.project.adsdentalapp.model.Address;
import com.cs489.project.adsdentalapp.model.Patient;
import com.cs489.project.adsdentalapp.repository.AddressRepository;
import com.cs489.project.adsdentalapp.service.AddressService;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public AddressResponse createAddress(AddressRequest request) {
        Address address = Address.builder()
            .addressLine(request.getAddressLine())
            .city(request.getCity())
            .state(request.getState())
            .postalCode(request.getPostalCode())
            .build();

        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully: {}", savedAddress.getId());
        return toResponse(savedAddress);
    }

    @Override
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        Address existingAddress = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with id " + addressId));

        existingAddress.setAddressLine(request.getAddressLine());
        existingAddress.setCity(request.getCity());
        existingAddress.setState(request.getState());
        existingAddress.setPostalCode(request.getPostalCode());

        Address updatedAddress = addressRepository.save(existingAddress);
        log.info("Address updated successfully: {}", addressId);
        return toResponse(updatedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with id " + addressId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses() {
        return addressRepository.findAllWithPatientOrderByCityAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> searchAddresses(String searchString) {
        return addressRepository.searchByAnyField(searchString).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public void deleteAddress(Long addressId) {
        if (!addressRepository.existsById(addressId)) {
            throw new ResourceNotFoundException("Address not found with id " + addressId);
        }
        addressRepository.deleteById(addressId);
        log.info("Address deleted successfully: {}", addressId);
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