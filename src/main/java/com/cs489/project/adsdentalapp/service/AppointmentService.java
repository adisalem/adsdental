package com.cs489.project.adsdentalapp.service;

import com.cs489.project.adsdentalapp.dto.appointment.AppointmentRequest;
import com.cs489.project.adsdentalapp.dto.appointment.AppointmentResponse;
import com.cs489.project.adsdentalapp.dto.appointment.RescheduleAppointmentRequest;

import java.util.List;

public interface AppointmentService {

    AppointmentResponse createAppointment(AppointmentRequest request);

    AppointmentResponse updateAppointment(Long appointmentId, AppointmentRequest request);

    AppointmentResponse getAppointmentById(Long appointmentId);

    List<AppointmentResponse> getAllAppointments();

    List<AppointmentResponse> getAppointmentsByPatient(Long patientId);

    List<AppointmentResponse> getAppointmentsByDentist(Long dentistId);

    List<AppointmentResponse> getAppointmentsBySurgery(Long surgeryId);

    List<AppointmentResponse> getUpcomingAppointmentsByPatient(Long patientId);

    List<AppointmentResponse> getUpcomingAppointmentsByDentist(Long dentistId);

    List<AppointmentResponse> searchAppointments(String patientSearchString, String dentistSearchString, String surgerySearchString);

    void deleteAppointment(Long appointmentId);

    void cancelAppointment(Long appointmentId);

    AppointmentResponse rescheduleAppointment(Long appointmentId, RescheduleAppointmentRequest request);
}
