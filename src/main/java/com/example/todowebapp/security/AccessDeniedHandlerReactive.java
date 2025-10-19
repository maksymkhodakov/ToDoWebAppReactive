package com.example.todowebapp.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AccessDeniedHandlerReactive implements ServerAccessDeniedHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        ServerHttpResponse resp = exchange.getResponse();

        if (resp.isCommitted()) {
            return Mono.empty();
        }

        resp.setStatusCode(HttpStatus.FORBIDDEN);
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body = (
                "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"" +
                        (denied != null ? denied.getMessage() : "Forbidden") + "\"}"
        ).getBytes(StandardCharsets.UTF_8);

        return resp.writeWith(Mono.just(resp.bufferFactory().wrap(body)));
    }
}
