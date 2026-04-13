package com.cs489.project.adsdentalapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cs489.project.adsdentalapp.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("""
        select a from Address a
        left join fetch a.patient p
        order by a.city asc
        """)
    List<Address> findAllWithPatientOrderByCityAsc();
}