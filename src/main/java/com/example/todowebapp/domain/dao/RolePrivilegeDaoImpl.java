package com.example.todowebapp.domain.dao;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class RolePrivilegeDaoImpl implements RolePrivilegeDao {

    private final DatabaseClient client;

    public RolePrivilegeDaoImpl(DatabaseClient client) {
        this.client = client;
    }

    @Override
    public Flux<RolePrivilegeRow> findByRoleId(Long roleId) {
        return client.sql("""
                SELECT role_id, privilege_id
                FROM public.roles_privileges
                WHERE role_id = :roleId
                """)
                .bind("roleId", roleId)
                .map((row, meta) -> new RolePrivilegeRow(
                        row.get("role_id", Long.class),
                        row.get("privilege_id", Long.class)))
                .all();
    }
}
