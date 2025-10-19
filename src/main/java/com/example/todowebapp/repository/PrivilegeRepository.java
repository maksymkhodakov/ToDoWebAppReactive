package com.example.todowebapp.repository;

import com.example.todowebapp.domain.entity.Privilege;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivilegeRepository extends R2dbcRepository<Privilege, Long> {
}
