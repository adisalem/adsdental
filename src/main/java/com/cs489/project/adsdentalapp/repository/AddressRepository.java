package com.cs489.project.adsdentalapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cs489.project.adsdentalapp.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("""
        select a from Address a
        left join fetch a.patient p
        order by a.city asc
        """)
    List<Address> findAllWithPatientOrderByCityAsc();

    @Query("""
        select distinct a from Address a
        left join fetch a.patient p
        where lower(coalesce(a.addressLine, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.city, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.state, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.postalCode, '')) like lower(concat('%', :searchString, '%'))
        order by a.city asc
        """)
    List<Address> searchByAnyField(@Param("searchString") String searchString);
}