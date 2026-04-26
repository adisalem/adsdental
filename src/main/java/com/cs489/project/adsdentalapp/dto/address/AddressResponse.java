package com.cs489.project.adsdentalapp.dto.address;

import com.cs489.project.adsdentalapp.dto.patient.PatientSummaryResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private Long id;
    private String addressLine;
    private String city;
    private String state;
    private String postalCode;
    private PatientSummaryResponse patient;
}