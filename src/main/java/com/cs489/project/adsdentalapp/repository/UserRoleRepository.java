package com.cs489.project.adsdentalapp.repository;

import com.cs489.project.adsdentalapp.model.UserRole;
import com.cs489.project.adsdentalapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    List<UserRole> findByUser(User user);
}
