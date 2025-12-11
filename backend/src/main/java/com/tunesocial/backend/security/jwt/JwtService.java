package com.tunesocial.backend.security.jwt;

import com.nimbusds.jose.Algorithm;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private static final long EXPIRATION_MS = 1000 * 60 * 15;

    public String generateToken(Long userId) {
        String subject = userId.toString();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, algorithm)
                .compact();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(
                Jwts.parserBuilder()
                        .setSigningKey(secret.getBytes())
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        );
    }

}

