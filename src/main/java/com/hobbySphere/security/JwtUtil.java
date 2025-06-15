package com.hobbySphere.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import com.hobbySphere.entities.AdminUsers;
import com.hobbySphere.entities.Businesses;
import com.hobbySphere.entities.Users;

import java.security.Key;
import java.util.Date;





@Component
public class JwtUtil {

    private final String secret = "H0bbYSpHeRe@2025_T0k3n!SecUr3Key#"; // Must be 32+ characters
    private final Key key = Keys.hmacShaKeyFor(secret.getBytes());
    private final long EXPIRATION_TIME = 86400000; // 1 day in milliseconds

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
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
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
                .claim ("role", "BUSINESS")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
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
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
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
            token = token.trim();
            String businessName = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("businessName", String.class);

            return businessName != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String extractRole(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key) // ✅ use your HMAC secret key
                    .build()
                    .parseClaimsJws(token.trim())
                    .getBody()
                    .get("role", String.class); // 👈 looks for the claim "role"
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
            token = token.trim();
            String role = extractRole(token);
            // If no role and no businessName, it's a user
            boolean isBusiness = isBusinessToken(token);
            return role == null && !isBusiness;
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
