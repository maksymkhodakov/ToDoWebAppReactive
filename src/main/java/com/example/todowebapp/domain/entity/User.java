package com.example.todowebapp.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table("users")
public class User extends TimestampEntity {

    @Id
    @Column("id")
    private Long id;

    private String email;

    private String password;

    private String name;

    @Column("last_name")
    private String lastName;

    @Column("role_id")
    private Long roleId;     // FK to roles.id

    @Column("is_system")
    private Boolean system;
}
