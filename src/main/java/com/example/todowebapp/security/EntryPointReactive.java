package com.example.todowebapp.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class EntryPointReactive implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpResponse resp = exchange.getResponse();

        if (resp.isCommitted()) {
            // response already started; do not touch headers/body
            return Mono.empty();
        }

        resp.setStatusCode(HttpStatus.UNAUTHORIZED);
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body = (
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" +
                        (ex != null ? ex.getMessage() : "Unauthorized") + "\"}"
        ).getBytes(StandardCharsets.UTF_8);

        return resp.writeWith(Mono.just(resp.bufferFactory().wrap(body)));
    }
}
