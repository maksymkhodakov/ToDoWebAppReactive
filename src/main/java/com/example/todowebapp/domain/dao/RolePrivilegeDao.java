package com.example.todowebapp.domain.dao;

import reactor.core.publisher.Flux;

public interface RolePrivilegeDao {
    Flux<RolePrivilegeRow> findByRoleId(Long roleId);
}
