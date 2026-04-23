package com.cs489.project.adsdentalapp.dto.surgery;

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
public class SurgeryResponse {

    private Long id;
    private String surgeryName;
    private String telephoneNumber;
    private AddressResponse address;
}
