package com.cs489.project.adsdentalapp.service;

import java.util.List;

import com.cs489.project.adsdentalapp.dto.auth.PatientRegistrationRequest;
import com.cs489.project.adsdentalapp.dto.patient.PatientRequest;
import com.cs489.project.adsdentalapp.dto.patient.PatientResponse;
import com.cs489.project.adsdentalapp.model.Patient;

public interface PatientService {

    Patient registerPatient(PatientRegistrationRequest request);

    PatientResponse createPatient(PatientRequest request);

    PatientResponse updatePatient(Long patientId, PatientRequest request);

    PatientResponse getPatientById(Long patientId);

    List<PatientResponse> getAllPatients();

    List<PatientResponse> searchPatients(String searchString);

    void deletePatient(Long patientId);
}