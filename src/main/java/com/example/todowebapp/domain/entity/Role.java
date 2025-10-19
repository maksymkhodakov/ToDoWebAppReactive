package com.example.todowebapp.domain.entity;

import com.example.todowebapp.domain.enumerated.UserRole;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table("roles")
public class Role extends TimestampEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("user_role")
    private UserRole userRole;
}
