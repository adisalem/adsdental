package com.cs489.project.adsdentalapp.dto.surgery;

import com.cs489.project.adsdentalapp.dto.address.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class SurgeryRequest {

    @NotBlank(message = "Surgery name is required")
    private String surgeryName;

    @NotBlank(message = "Telephone number is required")
    private String telephoneNumber;

    @NotNull(message = "Address is required")
    @Valid
    private AddressRequest address;
}
