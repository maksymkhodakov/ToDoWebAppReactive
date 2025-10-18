package com.example.todowebapp.service.impl;

import com.example.todowebapp.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private int expirationTime;

    @Override
    public String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolveClaims) {
        var claims = getAllInfoFromToken(token);
        return resolveClaims.apply(claims);
    }

    @Override
    public Claims getAllInfoFromToken(String token) {
        final SecretKey key = getKey();
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token).getPayload();
    }

    private SecretKey getKey() {
        final byte[] array = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(array);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        final Map<String, Object> map = new HashMap<>();
        map.put("roles", userDetails.getAuthorities());
        return generate(map, userDetails);
    }

    private String generate(Map<String, Object> map, UserDetails userDetails) {
        return Jwts
                .builder()
                .claims(map)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public boolean isValidToken(String token) {
        final Claims claims = getAllInfoFromToken(token);
        final Date expirationDate = claims.getExpiration();
        return !expirationDate.before(new Date());
    }
}
