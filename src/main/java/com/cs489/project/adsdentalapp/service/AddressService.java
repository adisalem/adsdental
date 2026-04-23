package com.cs489.project.adsdentalapp.service;

import java.util.List;

import com.cs489.project.adsdentalapp.dto.address.AddressRequest;
import com.cs489.project.adsdentalapp.dto.address.AddressResponse;

public interface AddressService {

    AddressResponse createAddress(AddressRequest request);

    AddressResponse updateAddress(Long addressId, AddressRequest request);

    AddressResponse getAddressById(Long addressId);

    List<AddressResponse> getAllAddresses();

    List<AddressResponse> searchAddresses(String searchString);

    void deleteAddress(Long addressId);
}