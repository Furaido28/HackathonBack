package com.helha.thelostgrimoire.security;

import com.helha.thelostgrimoire.domain.Users;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private static final String SECRET = "FGkKY#LAJxg8&8pepaE4aLj4!ndxScXf6FJ@F3ja";
    private static final long EXPIRATION = 1000*60*60*24; // 24h

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(Users user) {
        return Jwts.builder()
                .setSubject(user.getEmail_address())
                .claim("id", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }
}
