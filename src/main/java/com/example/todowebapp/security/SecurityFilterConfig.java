package com.example.todowebapp.security;

import com.example.todowebapp.config.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityFilterConfig {

    private final CorsProperties corsProperties;
    private final CustomSecurityFilter customSecurityFilter;
    private final EntryPointReactive entryPoint;
    private final AccessDeniedHandlerReactive accessDeniedHandler;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(corsProperties.getOrigins());
        cfg.setAllowedMethods(corsProperties.getMethods());
        cfg.setAllowCredentials(true);
        cfg.setAllowedHeaders(Arrays.asList(
                "Content-Type","content-type","x-requested-with",
                "Access-Control-Allow-Origin","Access-Control-Allow-Headers",
                "x-auth-token","x-app-id","Origin","Accept","X-Requested-With",
                "Access-Control-Request-Method","Access-Control-Request-Headers",
                "Authorization"
        ));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsWebFilter(source);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> { /* enabled via CorsWebFilter bean */ })
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .authorizeExchange(ex -> ex
                        .pathMatchers(
                                "/api/me",
                                "/api/register",
                                "/api/login",
                                "/swagger-ui/**",
                                "/api-docs*/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyExchange().authenticated()
                )

                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // place JWT filter at the authentication phase
                .addFilterAt(customSecurityFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .build();
    }
}
