package com.cs489.project.adsdentalapp.repository;

import com.cs489.project.adsdentalapp.model.Appointment;
import com.cs489.project.adsdentalapp.model.Dentist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDentistAndAppointmentDateAndAppointmentTime(Dentist dentist, LocalDate date, LocalTime time);

    @EntityGraph(attributePaths = {"patient", "dentist", "surgery"})
    List<Appointment> findAllByOrderByAppointmentDateAsc();

    @EntityGraph(attributePaths = {"patient", "dentist", "surgery"})
    List<Appointment> findByPatientIdOrderByAppointmentDateAsc(Long patientId);

    @EntityGraph(attributePaths = {"patient", "dentist", "surgery"})
    List<Appointment> findByDentistIdOrderByAppointmentDateAsc(Long dentistId);

    @EntityGraph(attributePaths = {"patient", "dentist", "surgery"})
    List<Appointment> findBySurgeryIdOrderByAppointmentDateAsc(Long surgeryId);

    @Query("""
        select count(a) from Appointment a
        where a.dentist.id = :dentistId
           and YEAR(a.appointmentDate) = YEAR(CURRENT_DATE)
           and WEEK(a.appointmentDate) = WEEK(CURRENT_DATE)
        """)
    long countAppointmentsByDentistThisWeek(@Param("dentistId") Long dentistId);

    @Query("""
        select a from Appointment a
        where a.patient.id = :patientId
           and a.appointmentDate >= CURRENT_DATE
        order by a.appointmentDate asc
        """)
    @EntityGraph(attributePaths = {"patient", "dentist", "surgery"})
    List<Appointment> findUpcomingAppointmentsByPatient(@Param("patientId") Long patientId);

    @Query("""
        select a from Appointment a
        where a.dentist.id = :dentistId
           and a.appointmentDate >= CURRENT_DATE
        order by a.appointmentDate asc
        """)
    @EntityGraph(attributePaths = {"patient", "dentist", "surgery"})
    List<Appointment> findUpcomingAppointmentsByDentist(@Param("dentistId") Long dentistId);

    @Query("""
        select a from Appointment a
        where lower(a.patient.firstName) like lower(concat('%', :searchString, '%'))
           or lower(a.patient.lastName) like lower(concat('%', :searchString, '%'))
           or lower(a.patient.email) like lower(concat('%', :searchString, '%'))
           or lower(a.dentist.firstName) like lower(concat('%', :dentistString, '%'))
           or lower(a.dentist.lastName) like lower(concat('%', :dentistString, '%'))
           or lower(a.surgery.surgeryName) like lower(concat('%', :surgeryString, '%'))
        order by a.appointmentDate asc
        """)
    @EntityGraph(attributePaths = {"patient", "dentist", "surgery"})
    List<Appointment> searchAppointments(@Param("searchString") String searchString,
                                         @Param("dentistString") String dentistString,
                                         @Param("surgeryString") String surgeryString);
}
