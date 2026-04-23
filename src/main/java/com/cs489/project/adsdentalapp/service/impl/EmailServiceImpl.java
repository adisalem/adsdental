package com.cs489.project.adsdentalapp.service.impl;

import com.cs489.project.adsdentalapp.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@adsdental.com}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendAppointmentConfirmation(String patientEmail, String patientName,
                                           String dentistName, String surgeryName,
                                           String appointmentDate, String appointmentTime) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(patientEmail);
            message.setSubject("Appointment Confirmation - ADS Dental Clinic");
            message.setText(buildAppointmentConfirmationBody(patientName, dentistName, surgeryName, appointmentDate, appointmentTime));

            mailSender.send(message);
            log.info("Appointment confirmation email sent to {}", patientEmail);
        } catch (Exception e) {
            log.error("Failed to send appointment confirmation email to {}: {}", patientEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendAppointmentCancellationEmail(String patientEmail, String patientName,
                                                String dentistName, String surgeryName,
                                                String appointmentDate, String appointmentTime) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(patientEmail);
            message.setSubject("Appointment Cancellation - ADS Dental Clinic");
            message.setText(buildAppointmentCancellationBody(patientName, dentistName, surgeryName, appointmentDate, appointmentTime));

            mailSender.send(message);
            log.info("Appointment cancellation email sent to {}", patientEmail);
        } catch (Exception e) {
            log.error("Failed to send appointment cancellation email to {}: {}", patientEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendAppointmentRescheduleEmail(String patientEmail, String patientName,
                                              String dentistName, String surgeryName,
                                              String oldAppointmentDate, String oldAppointmentTime,
                                              String newAppointmentDate, String newAppointmentTime) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(patientEmail);
            message.setSubject("Appointment Rescheduled - ADS Dental Clinic");
            message.setText(buildAppointmentRescheduleBody(patientName, dentistName, surgeryName,
                    oldAppointmentDate, oldAppointmentTime, newAppointmentDate, newAppointmentTime));

            mailSender.send(message);
            log.info("Appointment reschedule email sent to {}", patientEmail);
        } catch (Exception e) {
            log.error("Failed to send appointment reschedule email to {}: {}", patientEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendDentistAppointmentNotification(String dentistEmail, String dentistName,
                                                  String patientName, String surgeryName,
                                                  String appointmentDate, String appointmentTime) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(dentistEmail);
            message.setSubject("New Appointment Scheduled - ADS Dental Clinic");
            message.setText(buildDentistNotificationBody(dentistName, patientName, surgeryName, appointmentDate, appointmentTime));

            mailSender.send(message);
            log.info("Appointment notification email sent to dentist {}", dentistEmail);
        } catch (Exception e) {
            log.error("Failed to send appointment notification email to {}: {}", dentistEmail, e.getMessage(), e);
        }
    }

    private String buildAppointmentConfirmationBody(String patientName, String dentistName, String surgeryName,
                                                    String appointmentDate, String appointmentTime) {
        return String.format("""
                Dear %s,

                Your appointment has been successfully confirmed!

                Appointment Details:
                =====================
                Date: %s
                Time: %s
                Dentist: Dr. %s
                Location: %s

                Please arrive 10 minutes early. If you need to reschedule or cancel, please contact us at least 24 hours in advance.

                Best regards,
                ADS Dental Clinic Team
                """, patientName, appointmentDate, appointmentTime, dentistName, surgeryName);
    }

    private String buildAppointmentCancellationBody(String patientName, String dentistName, String surgeryName,
                                                   String appointmentDate, String appointmentTime) {
        return String.format("""
                Dear %s,

                Your appointment has been cancelled.

                Cancelled Appointment Details:
                ==============================
                Date: %s
                Time: %s
                Dentist: Dr. %s
                Location: %s

                If you would like to reschedule, please contact us or use our online appointment booking system.

                Best regards,
                ADS Dental Clinic Team
                """, patientName, appointmentDate, appointmentTime, dentistName, surgeryName);
    }

    private String buildAppointmentRescheduleBody(String patientName, String dentistName, String surgeryName,
                                                 String oldDate, String oldTime, String newDate, String newTime) {
        return String.format("""
                Dear %s,

                Your appointment has been rescheduled!

                Previous Appointment:
                =====================
                Date: %s
                Time: %s

                New Appointment:
                ================
                Date: %s
                Time: %s
                Dentist: Dr. %s
                Location: %s

                Please arrive 10 minutes early. If you need further changes, please contact us.

                Best regards,
                ADS Dental Clinic Team
                """, patientName, oldDate, oldTime, newDate, newTime, dentistName, surgeryName);
    }

    private String buildDentistNotificationBody(String dentistName, String patientName, String surgeryName,
                                               String appointmentDate, String appointmentTime) {
        return String.format("""
                Dear Dr. %s,

                A new appointment has been scheduled for you.

                Appointment Details:
                =====================
                Date: %s
                Time: %s
                Patient: %s
                Location: %s

                Please review and prepare accordingly.

                Best regards,
                ADS Dental Clinic System
                """, dentistName, appointmentDate, appointmentTime, patientName, surgeryName);
    }
}
