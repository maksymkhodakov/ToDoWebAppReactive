package com.example.todowebapp.domain.dto;

import com.example.todowebapp.domain.enumerated.UserRole;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private UserRole userRole;
    private Collection<GrantedAuthority> privileges;
}
