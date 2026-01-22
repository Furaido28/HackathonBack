package com.helha.thelostgrimoire.security;

import com.helha.thelostgrimoire.domain.models.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    /**
     * Secret key and expiration delay (24 hours) for JWT signing and validation
     */
    private static final String SECRET = "FGkKY#LAJxg8&8pepaE4aLj4!ndxScXf6FJ@F3ja";
    private static final long EXPIRATION = 1000L * 60 * 60 * 24; // 24h

    /**
     * cryptographic key generated from the secret string using HMAC algorithm
     */
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * Creates a signed JWT containing the user email as subject and the user ID as a custom claim
     */
    public String generateToken(Users user) {
        return Jwts.builder()
                .subject(user.getEmail_address())
                .claim("id", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    /**
     * Parses the token to retrieve the user ID stored in the payload's claims
     */
    public Long extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("id", Long.class);
    }

    /**
     * Verifies the token's integrity and expiration; returns false if the token is tampered with or expired
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}