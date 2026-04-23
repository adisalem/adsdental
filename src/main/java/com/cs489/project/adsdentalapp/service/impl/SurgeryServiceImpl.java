package com.cs489.project.adsdentalapp.service.impl;

import com.cs489.project.adsdentalapp.dto.address.AddressRequest;
import com.cs489.project.adsdentalapp.dto.address.AddressResponse;
import com.cs489.project.adsdentalapp.dto.surgery.SurgeryRequest;
import com.cs489.project.adsdentalapp.dto.surgery.SurgeryResponse;
import com.cs489.project.adsdentalapp.exception.DuplicateResourceException;
import com.cs489.project.adsdentalapp.exception.ResourceNotFoundException;
import com.cs489.project.adsdentalapp.model.Address;
import com.cs489.project.adsdentalapp.model.Surgery;
import com.cs489.project.adsdentalapp.repository.SurgeryRepository;
import com.cs489.project.adsdentalapp.service.SurgeryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class SurgeryServiceImpl implements SurgeryService {

    private final SurgeryRepository surgeryRepository;

    public SurgeryServiceImpl(SurgeryRepository surgeryRepository) {
        this.surgeryRepository = surgeryRepository;
    }

    @Override
    public SurgeryResponse createSurgery(SurgeryRequest request) {
        if (surgeryRepository.findBySurgeryName(request.getSurgeryName()).isPresent()) {
            throw new DuplicateResourceException("Surgery with name '" + request.getSurgeryName() + "' already exists");
        }

        Surgery surgery = Surgery.builder()
            .surgeryName(request.getSurgeryName())
            .telephoneNumber(request.getTelephoneNumber())
            .address(toAddressEntity(request.getAddress()))
            .build();

        Surgery savedSurgery = surgeryRepository.save(surgery);
        log.info("Surgery created successfully: {}", request.getSurgeryName());
        return toResponse(savedSurgery);
    }

    @Override
    public SurgeryResponse updateSurgery(Long surgeryId, SurgeryRequest request) {
        Surgery existingSurgery = surgeryRepository.findById(surgeryId)
            .orElseThrow(() -> new ResourceNotFoundException("Surgery not found with id " + surgeryId));

        if (!existingSurgery.getSurgeryName().equals(request.getSurgeryName()) &&
            surgeryRepository.findBySurgeryName(request.getSurgeryName()).isPresent()) {
            throw new DuplicateResourceException("Surgery with name '" + request.getSurgeryName() + "' already exists");
        }

        existingSurgery.setSurgeryName(request.getSurgeryName());
        existingSurgery.setTelephoneNumber(request.getTelephoneNumber());

        if (existingSurgery.getAddress() != null && request.getAddress() != null) {
            existingSurgery.getAddress().setAddressLine(request.getAddress().getAddressLine());
            existingSurgery.getAddress().setCity(request.getAddress().getCity());
            existingSurgery.getAddress().setState(request.getAddress().getState());
            existingSurgery.getAddress().setPostalCode(request.getAddress().getPostalCode());
        } else if (request.getAddress() != null) {
            existingSurgery.setAddress(toAddressEntity(request.getAddress()));
        }

        log.info("Surgery updated successfully: {}", surgeryId);
        return toResponse(surgeryRepository.save(existingSurgery));
    }

    @Override
    @Transactional(readOnly = true)
    public SurgeryResponse getSurgeryById(Long surgeryId) {
        return surgeryRepository.findById(surgeryId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Surgery not found with id " + surgeryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurgeryResponse> getAllSurgeries() {
        return surgeryRepository.findAllByOrderBySurgeryNameAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SurgeryResponse> searchSurgeries(String searchString) {
        return surgeryRepository.searchByAnyField(searchString).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public void deleteSurgery(Long surgeryId) {
        if (!surgeryRepository.existsById(surgeryId)) {
            throw new ResourceNotFoundException("Surgery not found with id " + surgeryId);
        }
        surgeryRepository.deleteById(surgeryId);
        log.info("Surgery deleted successfully: {}", surgeryId);
    }

    private Address toAddressEntity(AddressRequest request) {
        return Address.builder()
            .addressLine(request.getAddressLine())
            .city(request.getCity())
            .state(request.getState())
            .postalCode(request.getPostalCode())
            .build();
    }

    private SurgeryResponse toResponse(Surgery surgery) {
        return SurgeryResponse.builder()
            .id(surgery.getId())
            .surgeryName(surgery.getSurgeryName())
            .telephoneNumber(surgery.getTelephoneNumber())
            .address(toAddressResponse(surgery.getAddress()))
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
