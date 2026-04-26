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

import com.cs489.project.adsdentalapp.dto.address.AddressRequest;
import com.cs489.project.adsdentalapp.dto.address.AddressResponse;
import com.cs489.project.adsdentalapp.service.AddressService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/adsweb/api/v1")
@Validated
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/addresses")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/adsweb/api/v1/addresses/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/address/{id}")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(id, request));
    }

    @GetMapping("/addresses/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_MANAGER', 'PATIENT', 'DENTIST')")
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    @GetMapping("/addresses")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<List<AddressResponse>> getAllAddresses() {
        return ResponseEntity.ok(addressService.getAllAddresses());
    }

    @DeleteMapping("/address/{id}")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<Map<String, String>> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
    }

    @GetMapping("/address/search/{searchString}")
    @PreAuthorize("hasRole('OFFICE_MANAGER')")
    public ResponseEntity<List<AddressResponse>> searchAddresses(@PathVariable String searchString) {
        return ResponseEntity.ok(addressService.searchAddresses(searchString));
    }
}