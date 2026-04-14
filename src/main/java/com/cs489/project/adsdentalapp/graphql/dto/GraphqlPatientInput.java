package com.cs489.project.adsdentalapp.graphql.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GraphqlPatientInput(
    @NotBlank(message = "First name is required") String firstName,
    @NotBlank(message = "Last name is required") String lastName,
    String phoneNumber,
    @Email(message = "Email must be valid") String email,
    @NotNull(message = "Address is required") @Valid GraphqlAddressInput address,
    @NotBlank(message = "Date of birth is required") String dateOfBirth,
    Boolean hasUnpaidBill
) {
}
