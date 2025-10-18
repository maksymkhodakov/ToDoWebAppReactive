package com.example.todowebapp.security.impl;

import com.example.todowebapp.domain.entity.User;
import com.example.todowebapp.domain.enumerated.UserRole;
import com.example.todowebapp.exceptions.ApiException;
import com.example.todowebapp.exceptions.ErrorCode;
import com.example.todowebapp.repository.UserRepository;
import com.example.todowebapp.security.AuthenticationUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

@Service
@Primary
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final String email) {
        final User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        return AuthenticationUserDetails.authBuilder()
                .userId(user.getId())
                .username(user.getEmail())
                .password(user.getPassword())
                .system(user.isSystem())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .userRole(getUserRole(user))
                .authorities(getAuthorities(user))
                .build();
    }

    private UserRole getUserRole(final User user) {
        return user.getRole() == null ? null : user.getRole().getUserRole();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(final User user) {
        if (user.getRole() == null || user.getRole().getPrivileges() == null) {
            return new ArrayList<>();
        }
        return user
                .getRole()
                .getPrivileges()
                .stream()
                .map(privilege -> new SimpleGrantedAuthority(privilege.getUserPrivilege().name()))
                .sorted(Comparator.comparing(GrantedAuthority::getAuthority))
                .toList();
    }
}
