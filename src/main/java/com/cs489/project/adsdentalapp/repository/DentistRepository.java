package com.cs489.project.adsdentalapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cs489.project.adsdentalapp.model.Dentist;

public interface DentistRepository extends JpaRepository<Dentist, Long> {
    Optional<Dentist> findByEmail(String email);

    List<Dentist> findAllByOrderByLastNameAsc();

    @Query("SELECT d FROM Dentist d WHERE " +
           "LOWER(d.firstName) LIKE LOWER(CONCAT('%', :searchString, '%')) OR " +
           "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :searchString, '%')) OR " +
           "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :searchString, '%')) OR " +
           "d.phoneNumber LIKE CONCAT('%', :searchString, '%') OR " +
           "LOWER(d.email) LIKE LOWER(CONCAT('%', :searchString, '%'))")
    List<Dentist> searchByAnyField(@Param("searchString") String searchString);
}
