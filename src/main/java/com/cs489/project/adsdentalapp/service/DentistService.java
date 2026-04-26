package com.cs489.project.adsdentalapp.service;

import java.util.List;

import com.cs489.project.adsdentalapp.dto.auth.DentistRegistrationRequest;
import com.cs489.project.adsdentalapp.dto.dentist.DentistRequest;
import com.cs489.project.adsdentalapp.dto.dentist.DentistResponse;
import com.cs489.project.adsdentalapp.model.Dentist;

public interface DentistService {
    Dentist registerDentist(DentistRegistrationRequest request);

    DentistResponse createDentist(DentistRequest request);

    DentistResponse updateDentist(Long dentistId, DentistRequest request);

    DentistResponse getDentistById(Long dentistId);

    List<DentistResponse> getAllDentists();

    List<DentistResponse> searchDentists(String searchString);

    void deleteDentist(Long dentistId);
}
