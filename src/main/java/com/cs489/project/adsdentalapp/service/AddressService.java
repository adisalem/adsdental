package com.cs489.project.adsdentalapp.service;

import java.util.List;

import com.cs489.project.adsdentalapp.dto.address.AddressResponse;

public interface AddressService {

    List<AddressResponse> getAllAddresses();
}