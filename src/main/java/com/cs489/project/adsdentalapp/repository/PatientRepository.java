package com.cs489.project.adsdentalapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cs489.project.adsdentalapp.model.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    @EntityGraph(attributePaths = "address")
    List<Patient> findAllByOrderByLastNameAsc();

    @Query("""
        select distinct p from Patient p
        left join fetch p.address a
        where lower(coalesce(p.firstName, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(p.lastName, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(p.email, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(p.phoneNumber, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.addressLine, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.city, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.state, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.postalCode, '')) like lower(concat('%', :searchString, '%'))
        order by p.lastName asc
        """)
    List<Patient> searchByAnyField(@Param("searchString") String searchString);
}