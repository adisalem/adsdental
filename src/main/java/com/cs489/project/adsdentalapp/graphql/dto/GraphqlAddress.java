package com.cs489.project.adsdentalapp.graphql.dto;

public record GraphqlAddress(
    String id,
    String addressLine,
    String city,
    String state,
    String postalCode,
    GraphqlPatientSummary patient
) {
}
