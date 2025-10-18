package com.example.todowebapp.domain.entity;

import com.example.todowebapp.domain.enumerated.UserPrivilege;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, exclude = "roles")
@Entity
@Table(name = "privileges", schema = "public")
public class Privilege extends TimestampEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_privilege")
    @Enumerated(value = EnumType.STRING)
    private UserPrivilege userPrivilege;

    @ManyToMany(mappedBy = "privileges")
    private List<Role> roles = new ArrayList<>();
}
