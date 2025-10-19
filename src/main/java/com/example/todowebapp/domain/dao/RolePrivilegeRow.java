package com.example.todowebapp.domain.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("roles_privileges")
public class RolePrivilegeRow {

    @Column("role_id")
    private Long roleId;

    @Column("privilege_id")
    private Long privilegeId;
}
