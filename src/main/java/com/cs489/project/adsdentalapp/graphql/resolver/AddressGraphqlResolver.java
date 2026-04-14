package com.cs489.project.adsdentalapp.graphql.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.cs489.project.adsdentalapp.dto.address.AddressResponse;
import com.cs489.project.adsdentalapp.dto.patient.PatientSummaryResponse;
import com.cs489.project.adsdentalapp.graphql.dto.GraphqlAddress;
import com.cs489.project.adsdentalapp.graphql.dto.GraphqlPatientSummary;
import com.cs489.project.adsdentalapp.service.AddressService;

@Controller
public class AddressGraphqlResolver {

    private final AddressService addressService;

    public AddressGraphqlResolver(AddressService addressService) {
        this.addressService = addressService;
    }

    @QueryMapping
    public List<GraphqlAddress> addresses() {
        return addressService.getAllAddresses().stream()
            .map(this::toGraphqlAddress)
            .toList();
    }

    private GraphqlAddress toGraphqlAddress(AddressResponse address) {
        return new GraphqlAddress(
            address.getId() == null ? null : address.getId().toString(),
            address.getAddressLine(),
            address.getCity(),
            address.getState(),
            address.getPostalCode(),
            toGraphqlPatientSummary(address.getPatient())
        );
    }

    private GraphqlPatientSummary toGraphqlPatientSummary(PatientSummaryResponse patient) {
        if (patient == null) {
            return null;
        }

        return new GraphqlPatientSummary(
            patient.getId() == null ? null : patient.getId().toString(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getPhoneNumber(),
            patient.getEmail()
        );
    }
}
