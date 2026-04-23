package com.cs489.project.adsdentalapp.dto.appointment;

import com.cs489.project.adsdentalapp.dto.patient.PatientSummaryResponse;
import com.cs489.project.adsdentalapp.dto.dentist.DentistSummaryResponse;
import com.cs489.project.adsdentalapp.dto.surgery.SurgeryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private Long id;
    private PatientSummaryResponse patient;
    private DentistSummaryResponse dentist;
    private SurgeryResponse surgery;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
}
