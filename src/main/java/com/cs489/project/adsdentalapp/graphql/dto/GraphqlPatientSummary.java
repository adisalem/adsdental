package com.cs489.project.adsdentalapp.graphql.dto;

public record GraphqlPatientSummary(
    String id,
    String firstName,
    String lastName,
    String phoneNumber,
    String email
) {
}
