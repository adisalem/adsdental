package com.cs489.project.adsdentalapp.repository;

import com.cs489.project.adsdentalapp.model.Surgery;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurgeryRepository extends JpaRepository<Surgery, Long> {

    Optional<Surgery> findBySurgeryName(String surgeryName);

    @EntityGraph(attributePaths = "address")
    List<Surgery> findAllByOrderBySurgeryNameAsc();

    @Query("""
        select distinct s from Surgery s
        left join fetch s.address a
        where lower(coalesce(s.surgeryName, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(s.telephoneNumber, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.addressLine, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.city, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.state, '')) like lower(concat('%', :searchString, '%'))
           or lower(coalesce(a.postalCode, '')) like lower(concat('%', :searchString, '%'))
        order by s.surgeryName asc
        """)
    List<Surgery> searchByAnyField(@Param("searchString") String searchString);
}
