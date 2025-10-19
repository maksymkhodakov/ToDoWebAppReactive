package com.example.todowebapp.security.impl;

import com.example.todowebapp.domain.dao.RolePrivilegeDao;
import com.example.todowebapp.domain.dao.RolePrivilegeRow;
import com.example.todowebapp.domain.entity.Role;
import com.example.todowebapp.domain.entity.User;
import com.example.todowebapp.domain.enumerated.UserRole;
import com.example.todowebapp.exceptions.ApiException;
import com.example.todowebapp.exceptions.ErrorCode;
import com.example.todowebapp.repository.PrivilegeRepository;
import com.example.todowebapp.repository.RoleRepository;
import com.example.todowebapp.repository.UserRepository;
import com.example.todowebapp.security.AuthenticationUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final RolePrivilegeDao rolePrivilegeDao;

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new ApiException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(this::buildDetailsForUser);
    }

    private Mono<UserDetails> buildDetailsForUser(User u) {
        final Long roleId = u.getRoleId();
        final boolean system = Boolean.TRUE.equals(u.getSystem());

        Mono<java.util.Optional<UserRole>> roleOptMono =
                (roleId == null)
                        ? Mono.just(java.util.Optional.empty())
                        : roleRepository.findById(roleId)
                        .map(Role::getUserRole)
                        .map(java.util.Optional::of)
                        .defaultIfEmpty(java.util.Optional.empty());

        Mono<List<GrantedAuthority>> authoritiesMono =
                (roleId == null)
                        ? Mono.just(java.util.Collections.emptyList())
                        : rolePrivilegeDao.findByRoleId(roleId)
                        .map(RolePrivilegeRow::getPrivilegeId)
                        .collectList()
                        .flatMap(this::mapAuthorities);

        return Mono.zip(roleOptMono, authoritiesMono)
                .map(tuple -> AuthenticationUserDetails.authBuilder()
                        .userId(u.getId())
                        .username(java.util.Objects.requireNonNullElse(u.getEmail(), ""))
                        .password(java.util.Objects.requireNonNullElse(u.getPassword(), ""))
                        .system(system)
                        .enabled(true)
                        .accountNonExpired(true)
                        .accountNonLocked(true)
                        .credentialsNonExpired(true)
                        .userRole(tuple.getT1().orElse(null))  // safely turn Optional into nullable
                        .authorities(tuple.getT2())
                        .build());
    }

    private Mono<List<GrantedAuthority>> mapAuthorities(final List<Long> ids) {
        return ids.isEmpty() ?
                Mono.just(java.util.Collections.emptyList()) :
                privilegeRepository.findAllById(ids)
                        .map(p -> new SimpleGrantedAuthority(p.getUserPrivilege().name()))
                        .cast(GrantedAuthority.class)
                        .sort(Comparator.comparing(GrantedAuthority::getAuthority))
                        .collectList();
    }
}
