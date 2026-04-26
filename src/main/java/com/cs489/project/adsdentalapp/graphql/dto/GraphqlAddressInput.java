package com.cs489.project.adsdentalapp.graphql.dto;

import jakarta.validation.constraints.NotBlank;

public record GraphqlAddressInput(
    @NotBlank(message = "Address line is required") String addressLine,
    @NotBlank(message = "City is required") String city,
    @NotBlank(message = "State is required") String state,
    @NotBlank(message = "Postal code is required") String postalCode
) {
}
