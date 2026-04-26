package com.cs489.project.adsdentalapp.graphql.dto;

public record GraphqlPatient(
    String id,
    String firstName,
    String lastName,
    String phoneNumber,
    String email,
    GraphqlAddress address,
    String dateOfBirth,
    boolean hasUnpaidBill
) {
}
