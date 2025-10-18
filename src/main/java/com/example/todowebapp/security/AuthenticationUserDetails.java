package com.example.todowebapp.security;

import com.example.todowebapp.domain.enumerated.UserRole;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collection;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AuthenticationUserDetails extends User {
    @Serial
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private final Long userId;
    private final UserRole userRole;
    private final boolean system;

    @Builder(builderMethodName = "authBuilder")
    public AuthenticationUserDetails(final String username,
                                     final String password,
                                     final boolean enabled,
                                     final boolean accountNonExpired,
                                     final boolean credentialsNonExpired,
                                     final boolean accountNonLocked,
                                     final Collection<? extends GrantedAuthority> authorities,
                                     final Long userId,
                                     final UserRole userRole,
                                     final boolean system) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
        this.userRole = userRole;
        this.system = system;
    }
}
