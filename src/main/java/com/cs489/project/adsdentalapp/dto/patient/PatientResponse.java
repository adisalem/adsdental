package com.cs489.project.adsdentalapp.dto.patient;

import java.time.LocalDate;

import com.cs489.project.adsdentalapp.dto.address.AddressResponse;

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
public class PatientResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private AddressResponse address;
    private LocalDate dateOfBirth;
    private boolean hasUnpaidBill;
}