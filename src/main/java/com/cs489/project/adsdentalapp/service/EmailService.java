package com.cs489.project.adsdentalapp.service;

public interface EmailService {

    void sendAppointmentConfirmation(String patientEmail, String patientName, 
                                     String dentistName, String surgeryName, 
                                     String appointmentDate, String appointmentTime);

    void sendAppointmentCancellationEmail(String patientEmail, String patientName, 
                                         String dentistName, String surgeryName, 
                                         String appointmentDate, String appointmentTime);

    void sendAppointmentRescheduleEmail(String patientEmail, String patientName, 
                                       String dentistName, String surgeryName, 
                                       String oldAppointmentDate, String oldAppointmentTime,
                                       String newAppointmentDate, String newAppointmentTime);

    void sendDentistAppointmentNotification(String dentistEmail, String dentistName,
                                           String patientName, String surgeryName,
                                           String appointmentDate, String appointmentTime);
}
