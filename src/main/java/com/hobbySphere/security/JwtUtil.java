package com.hobbySphere.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Businesses;
import com.hobbySphere.entities.Users;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationTime;

    public JwtUtil(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
        this.expirationTime = jwtProperties.getExpirationTime();
    }

    public String generateToken(Users user) {
        String subject = user.getEmail() != null ? user.getEmail() : user.getPhoneNumber();

        return Jwts.builder()
                .setSubject(subject)
                .claim("id", user.getId())
                .claim("username", user.getUsername())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("profileImageUrl", user.getProfilePictureUrl())
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(Businesses business) {
        return Jwts.builder()
                .setSubject(business.getEmail())
                .claim("id", business.getId())
                .claim("businessName", business.getBusinessName())
                .claim("logoUrl", business.getBusinessLogoUrl())
                .claim("bannerUrl", business.getBusinessBannerUrl())
                .claim("role", "BUSINESS")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(AdminUsers adminUser) {
        return Jwts.builder()
                .setSubject(adminUser.getEmail())
                .claim("id", adminUser.getAdminId())
                .claim("username", adminUser.getUsername())
                .claim("role", adminUser.getRole().getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isBusinessToken(String token) {
        try {
            String role = extractRole(token);
            return "BUSINESS".equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }


    public String extractRole(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token.trim())
                    .getBody()
                    .get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Long extractId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);
    }

    public boolean isUserToken(String token) {
        try {
            String role = extractRole(token);
            return "USER".equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAdminToken(String token) {
        try {
            String role = extractRole(token);
            return "SUPER_ADMIN".equals(role) || "MANAGER".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSuperAdmin(String token) {
        try {
            String role = extractRole(token);
            return "SUPER_ADMIN".equals(role);
        } catch (Exception e) {
            return false;
        }
    }
}
