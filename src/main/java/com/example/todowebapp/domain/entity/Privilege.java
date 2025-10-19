package com.example.todowebapp.domain.entity;

import com.example.todowebapp.domain.enumerated.UserPrivilege;
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
@Table("privileges")
public class Privilege extends TimestampEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("user_privilege")
    private UserPrivilege userPrivilege;
}
