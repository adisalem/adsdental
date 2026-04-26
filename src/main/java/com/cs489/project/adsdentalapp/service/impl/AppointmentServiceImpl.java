package com.cs489.project.adsdentalapp.service.impl;

import com.cs489.project.adsdentalapp.dto.address.AddressResponse;
import com.cs489.project.adsdentalapp.dto.appointment.AppointmentRequest;
import com.cs489.project.adsdentalapp.dto.appointment.AppointmentResponse;
import com.cs489.project.adsdentalapp.dto.appointment.RescheduleAppointmentRequest;
import com.cs489.project.adsdentalapp.dto.dentist.DentistSummaryResponse;
import com.cs489.project.adsdentalapp.dto.patient.PatientSummaryResponse;
import com.cs489.project.adsdentalapp.dto.surgery.SurgeryResponse;
import com.cs489.project.adsdentalapp.exception.DuplicateResourceException;
import com.cs489.project.adsdentalapp.exception.ResourceNotFoundException;
import com.cs489.project.adsdentalapp.model.Appointment;
import com.cs489.project.adsdentalapp.model.Dentist;
import com.cs489.project.adsdentalapp.model.Patient;
import com.cs489.project.adsdentalapp.model.Surgery;
import com.cs489.project.adsdentalapp.repository.AppointmentRepository;
import com.cs489.project.adsdentalapp.repository.DentistRepository;
import com.cs489.project.adsdentalapp.repository.PatientRepository;
import com.cs489.project.adsdentalapp.repository.SurgeryRepository;
import com.cs489.project.adsdentalapp.service.AppointmentService;
import com.cs489.project.adsdentalapp.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DentistRepository dentistRepository;
    private final SurgeryRepository surgeryRepository;
    private final EmailService emailService;

    @Value("${app.demo.validate-unpaid-bills:false}")
    private boolean validateUnpaidBills;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                 PatientRepository patientRepository,
                                 DentistRepository dentistRepository,
                                 SurgeryRepository surgeryRepository,
                                 EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.dentistRepository = dentistRepository;
        this.surgeryRepository = surgeryRepository;
        this.emailService = emailService;
    }

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // Fetch and validate patient
        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + request.getPatientId()));

        if (validateUnpaidBills && patient.isHasUnpaidBill()) {
            throw new DuplicateResourceException("Cannot create appointment. Patient has unpaid bills.");
        }

        Dentist dentist = dentistRepository.findById(request.getDentistId())
            .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id " + request.getDentistId()));

        Surgery surgery = surgeryRepository.findById(request.getSurgeryId())
            .orElseThrow(() -> new ResourceNotFoundException("Surgery not found with id " + request.getSurgeryId()));

        if (appointmentRepository.existsByDentistAndAppointmentDateAndAppointmentTime(
                dentist, request.getAppointmentDate(), request.getAppointmentTime())) {
            throw new DuplicateResourceException("Time slot is already booked for this dentist on the specified date and time");
        }

        long appointmentsThisWeek = appointmentRepository.countAppointmentsByDentistThisWeek(request.getDentistId());
        if (appointmentsThisWeek >= 5) {
            throw new DuplicateResourceException("Dentist has reached maximum of 5 appointments for this week");
        }

        // Create and save appointment
        Appointment appointment = Appointment.builder()
            .patient(patient)
            .dentist(dentist)
            .surgery(surgery)
            .appointmentDate(request.getAppointmentDate())
            .appointmentTime(request.getAppointmentTime())
            .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment created successfully for patient {} with dentist {}", request.getPatientId(), request.getDentistId());

        // Send confirmation emails
        emailService.sendAppointmentConfirmation(
            patient.getEmail(),
            patient.getFirstName() + " " + patient.getLastName(),
            dentist.getFirstName() + " " + dentist.getLastName(),
            surgery.getSurgeryName(),
            savedAppointment.getAppointmentDate().toString(),
            savedAppointment.getAppointmentTime().toString()
        );

        emailService.sendDentistAppointmentNotification(
            dentist.getEmail(),
            dentist.getFirstName() + " " + dentist.getLastName(),
            patient.getFirstName() + " " + patient.getLastName(),
            surgery.getSurgeryName(),
            savedAppointment.getAppointmentDate().toString(),
            savedAppointment.getAppointmentTime().toString()
        );

        return toResponse(savedAppointment);
    }

    @Override
    public AppointmentResponse updateAppointment(Long appointmentId, AppointmentRequest request) {
        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        // Fetch and validate patient
        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + request.getPatientId()));

        if (validateUnpaidBills && patient.isHasUnpaidBill()) {
            throw new DuplicateResourceException("Cannot update appointment. Patient has unpaid bills.");
        }

        // Fetch and validate dentist
        Dentist dentist = dentistRepository.findById(request.getDentistId())
            .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id " + request.getDentistId()));

        // Fetch and validate surgery
        Surgery surgery = surgeryRepository.findById(request.getSurgeryId())
            .orElseThrow(() -> new ResourceNotFoundException("Surgery not found with id " + request.getSurgeryId()));

        // Check if new appointment time is already booked (excluding current appointment)
        if (!existingAppointment.getAppointmentDate().equals(request.getAppointmentDate()) ||
            !existingAppointment.getAppointmentTime().equals(request.getAppointmentTime())) {
            if (appointmentRepository.existsByDentistAndAppointmentDateAndAppointmentTime(
                    dentist, request.getAppointmentDate(), request.getAppointmentTime())) {
                throw new DuplicateResourceException("Time slot is already booked for this dentist on the specified date and time");
            }
        }

        // Store old appointment details for email notification
        boolean dateChanged = !existingAppointment.getAppointmentDate().equals(request.getAppointmentDate());
        boolean timeChanged = !existingAppointment.getAppointmentTime().equals(request.getAppointmentTime());
        String oldDate = existingAppointment.getAppointmentDate().toString();
        String oldTime = existingAppointment.getAppointmentTime().toString();

        // Update appointment
        existingAppointment.setPatient(patient);
        existingAppointment.setDentist(dentist);
        existingAppointment.setSurgery(surgery);
        existingAppointment.setAppointmentDate(request.getAppointmentDate());
        existingAppointment.setAppointmentTime(request.getAppointmentTime());

        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);
        log.info("Appointment updated successfully: {}", appointmentId);

        // Send reschedule email if date or time changed
        if (dateChanged || timeChanged) {
            emailService.sendAppointmentRescheduleEmail(
                patient.getEmail(),
                patient.getFirstName() + " " + patient.getLastName(),
                dentist.getFirstName() + " " + dentist.getLastName(),
                surgery.getSurgeryName(),
                oldDate,
                oldTime,
                updatedAppointment.getAppointmentDate().toString(),
                updatedAppointment.getAppointmentTime().toString()
            );
        }

        return toResponse(updatedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByAppointmentDateAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id " + patientId);
        }
        return appointmentRepository.findByPatientIdOrderByAppointmentDateAsc(patientId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDentist(Long dentistId) {
        if (!dentistRepository.existsById(dentistId)) {
            throw new ResourceNotFoundException("Dentist not found with id " + dentistId);
        }
        return appointmentRepository.findByDentistIdOrderByAppointmentDateAsc(dentistId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsBySurgery(Long surgeryId) {
        if (!surgeryRepository.existsById(surgeryId)) {
            throw new ResourceNotFoundException("Surgery not found with id " + surgeryId);
        }
        return appointmentRepository.findBySurgeryIdOrderByAppointmentDateAsc(surgeryId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointmentsByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id " + patientId);
        }
        return appointmentRepository.findUpcomingAppointmentsByPatient(patientId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointmentsByDentist(Long dentistId) {
        if (!dentistRepository.existsById(dentistId)) {
            throw new ResourceNotFoundException("Dentist not found with id " + dentistId);
        }
        return appointmentRepository.findUpcomingAppointmentsByDentist(dentistId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> searchAppointments(String patientSearchString, String dentistSearchString, String surgerySearchString) {
        return appointmentRepository.searchAppointments(patientSearchString, dentistSearchString, surgerySearchString).stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public void deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        // Send cancellation email before deleting
        emailService.sendAppointmentCancellationEmail(
            appointment.getPatient().getEmail(),
            appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
            appointment.getDentist().getFirstName() + " " + appointment.getDentist().getLastName(),
            appointment.getSurgery().getSurgeryName(),
            appointment.getAppointmentDate().toString(),
            appointment.getAppointmentTime().toString()
        );

        try {
            appointmentRepository.delete(appointment);
            appointmentRepository.flush();
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.warn("Appointment could not be deleted because it no longer exists: {}", appointmentId, ex);
            throw new ResourceNotFoundException("Appointment not found with id " + appointmentId);
        }

        log.info("Appointment deleted successfully: {}", appointmentId);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
            .id(appointment.getId())
            .patient(toPatientSummary(appointment.getPatient()))
            .dentist(toDentistSummary(appointment.getDentist()))
            .surgery(toSurgeryResponse(appointment.getSurgery()))
            .appointmentDate(appointment.getAppointmentDate())
            .appointmentTime(appointment.getAppointmentTime())
            .build();
    }

    private PatientSummaryResponse toPatientSummary(Patient patient) {
        return PatientSummaryResponse.builder()
            .id(patient.getId())
            .firstName(patient.getFirstName())
            .lastName(patient.getLastName())
            .phoneNumber(patient.getPhoneNumber())
            .email(patient.getEmail())
            .build();
    }

    private DentistSummaryResponse toDentistSummary(Dentist dentist) {
        return DentistSummaryResponse.builder()
            .id(dentist.getId())
            .firstName(dentist.getFirstName())
            .lastName(dentist.getLastName())
            .phoneNumber(dentist.getPhoneNumber())
            .email(dentist.getEmail())
            .specialization(dentist.getSpecialization())
            .build();
    }

    private SurgeryResponse toSurgeryResponse(Surgery surgery) {
        return SurgeryResponse.builder()
            .id(surgery.getId())
            .surgeryName(surgery.getSurgeryName())
            .telephoneNumber(surgery.getTelephoneNumber())
            .address(toAddressResponse(surgery.getAddress()))
            .build();
    }

    private AddressResponse toAddressResponse(com.cs489.project.adsdentalapp.model.Address address) {
        return AddressResponse.builder()
            .id(address.getId())
            .addressLine(address.getAddressLine())
            .city(address.getCity())
            .state(address.getState())
            .postalCode(address.getPostalCode())
            .build();
    }

    @Override
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        Patient patient = appointment.getPatient();
        Dentist dentist = appointment.getDentist();
        Surgery surgery = appointment.getSurgery();

        // Send cancellation email
        emailService.sendAppointmentCancellationEmail(
            patient.getEmail(),
            patient.getFirstName() + " " + patient.getLastName(),
            dentist.getFirstName() + " " + dentist.getLastName(),
            surgery.getSurgeryName(),
            appointment.getAppointmentDate().toString(),
            appointment.getAppointmentTime().toString()
        );

        appointmentRepository.deleteById(appointmentId);
        log.info("Appointment cancelled successfully: {}", appointmentId);
    }

    @Override
    public AppointmentResponse rescheduleAppointment(Long appointmentId, RescheduleAppointmentRequest request) {
        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + appointmentId));

        Patient patient = existingAppointment.getPatient();
        Dentist dentist = existingAppointment.getDentist();
        Surgery surgery = existingAppointment.getSurgery();

        // Check if patient has unpaid bills
        if (patient.isHasUnpaidBill()) {
            throw new DuplicateResourceException("Cannot reschedule appointment. Patient has unpaid bills.");
        }

        // Check if new time slot is already booked
        if (appointmentRepository.existsByDentistAndAppointmentDateAndAppointmentTime(
                dentist, request.getNewAppointmentDate(), request.getNewAppointmentTime())) {
            throw new DuplicateResourceException("Time slot is already booked for this dentist on the specified date and time");
        }

        // Store old appointment details for email notification
        String oldDate = existingAppointment.getAppointmentDate().toString();
        String oldTime = existingAppointment.getAppointmentTime().toString();

        // Update appointment with new date and time
        existingAppointment.setAppointmentDate(request.getNewAppointmentDate());
        existingAppointment.setAppointmentTime(request.getNewAppointmentTime());

        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);
        log.info("Appointment rescheduled successfully: {}", appointmentId);

        // Send reschedule email
        emailService.sendAppointmentRescheduleEmail(
            patient.getEmail(),
            patient.getFirstName() + " " + patient.getLastName(),
            dentist.getFirstName() + " " + dentist.getLastName(),
            surgery.getSurgeryName(),
            oldDate,
            oldTime,
            updatedAppointment.getAppointmentDate().toString(),
            updatedAppointment.getAppointmentTime().toString()
        );

        return toResponse(updatedAppointment);
    }
}
