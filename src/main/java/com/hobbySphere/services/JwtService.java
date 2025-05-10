package com.hobbySphere.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

   
    private static final String SECRET_KEY = "yourJwtSecretKeyyourJwtSecretKey"; 
    private static final long EXPIRATION_TIME = 86400000L;

    public String generateToken(String email) {
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());  
        
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) 
                .signWith(key, SignatureAlgorithm.HS256) 
                .compact();
    }
}
