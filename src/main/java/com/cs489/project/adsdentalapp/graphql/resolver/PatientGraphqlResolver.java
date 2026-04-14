package com.cs489.project.adsdentalapp.graphql.resolver;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import com.cs489.project.adsdentalapp.dto.address.AddressRequest;
import com.cs489.project.adsdentalapp.dto.address.AddressResponse;
import com.cs489.project.adsdentalapp.dto.patient.PatientRequest;
import com.cs489.project.adsdentalapp.dto.patient.PatientResponse;
import com.cs489.project.adsdentalapp.exception.ResourceNotFoundException;
import com.cs489.project.adsdentalapp.graphql.dto.GraphqlAddress;
import com.cs489.project.adsdentalapp.graphql.dto.GraphqlAddressInput;
import com.cs489.project.adsdentalapp.graphql.dto.GraphqlPatient;
import com.cs489.project.adsdentalapp.graphql.dto.GraphqlPatientInput;
import com.cs489.project.adsdentalapp.service.PatientService;

import jakarta.validation.Valid;

@Controller
@Validated
public class PatientGraphqlResolver {

    private final PatientService patientService;

    public PatientGraphqlResolver(PatientService patientService) {
        this.patientService = patientService;
    }

    @QueryMapping
    public List<GraphqlPatient> patients() {
        return patientService.getAllPatients().stream()
            .map(this::toGraphqlPatient)
            .toList();
    }

    @QueryMapping
    public GraphqlPatient patientById(@Argument String id) {
        return toGraphqlPatient(patientService.getPatientById(parseId(id)));
    }

    @QueryMapping
    public List<GraphqlPatient> searchPatients(@Argument String searchString) {
        return patientService.searchPatients(searchString).stream()
            .map(this::toGraphqlPatient)
            .toList();
    }

    @MutationMapping
    public GraphqlPatient createPatient(@Argument @Valid GraphqlPatientInput input) {
        PatientResponse createdPatient = patientService.createPatient(toPatientRequest(input));
        return toGraphqlPatient(createdPatient);
    }

    @MutationMapping
    public GraphqlPatient updatePatient(@Argument String id, @Argument @Valid GraphqlPatientInput input) {
        PatientResponse updatedPatient = patientService.updatePatient(parseId(id), toPatientRequest(input));
        return toGraphqlPatient(updatedPatient);
    }

    @MutationMapping
    public boolean deletePatient(@Argument String id) {
        patientService.deletePatient(parseId(id));
        return true;
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new ResourceNotFoundException("Invalid patient id " + id);
        }
    }

    private PatientRequest toPatientRequest(GraphqlPatientInput input) {
        return PatientRequest.builder()
            .firstName(input.firstName())
            .lastName(input.lastName())
            .phoneNumber(input.phoneNumber())
            .email(input.email())
            .address(toAddressRequest(input.address()))
            .dateOfBirth(parseDate(input.dateOfBirth()))
            .hasUnpaidBill(Boolean.TRUE.equals(input.hasUnpaidBill()))
            .build();
    }

    private AddressRequest toAddressRequest(GraphqlAddressInput input) {
        return AddressRequest.builder()
            .addressLine(input.addressLine())
            .city(input.city())
            .state(input.state())
            .postalCode(input.postalCode())
            .build();
    }

    private LocalDate parseDate(String dateOfBirth) {
        try {
            return LocalDate.parse(dateOfBirth);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format, expected yyyy-MM-dd");
        }
    }

    private GraphqlPatient toGraphqlPatient(PatientResponse patient) {
        return new GraphqlPatient(
            patient.getId() == null ? null : patient.getId().toString(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getPhoneNumber(),
            patient.getEmail(),
            toGraphqlAddress(patient.getAddress()),
            patient.getDateOfBirth() == null ? null : patient.getDateOfBirth().toString(),
            patient.isHasUnpaidBill()
        );
    }

    private GraphqlAddress toGraphqlAddress(AddressResponse address) {
        if (address == null) {
            return null;
        }

        return new GraphqlAddress(
            address.getId() == null ? null : address.getId().toString(),
            address.getAddressLine(),
            address.getCity(),
            address.getState(),
            address.getPostalCode(),
            null
        );
    }
}
