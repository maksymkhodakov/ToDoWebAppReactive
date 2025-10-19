package com.example.todowebapp.security;

import com.example.todowebapp.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomSecurityFilter implements WebFilter {

    private static final String BEARER = "Bearer ";
    private final JwtService jwtService;
    private final ReactiveUserDetailsService userDetailsService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            return chain.filter(exchange); // no token -> continue
        }

        final String token = authHeader.substring(BEARER.length());
        final String email = jwtService.getEmailFromToken(token);
        if (email == null || !jwtService.isValidToken(token)) {
            return chain.filter(exchange); // invalid -> continue unauthenticated
        }

        // load user reactively and set Authentication into Reactor Context
        return userDetailsService.findByUsername(email)
                .map(this::asAuth)
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                .switchIfEmpty(chain.filter(exchange));
    }

    private Authentication asAuth(UserDetails user) {
        return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
    }
}
