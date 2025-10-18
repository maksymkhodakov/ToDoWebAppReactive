package com.example.todowebapp.repository;

import com.example.todowebapp.domain.entity.Role;
import com.example.todowebapp.domain.enumerated.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByUserRole(UserRole userRole);
}
