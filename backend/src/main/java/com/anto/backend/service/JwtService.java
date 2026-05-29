package com.anto.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String SECRET = "prepify-secret-key-that-is-long-enough-for-hs256";
    private static final long EXPIRATION = 30 * 60 * 1000;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(Integer userId, String email, String role, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("permissions", permissions);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Integer extractUserId(String token) {
        return (Integer) extractClaims(token).get("userId");
    }

    public String extractRole(String token) {
        return (String) extractClaims(token).get("role");
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public List<String> extractPermissions(String token) {
        Object perms = extractClaims(token).get("permissions");
        if (perms instanceof List<?>) {
            return ((List<?>) perms).stream().map(Object::toString).collect(Collectors.toList());
        }
        return List.of();
    }
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    public String generateRefreshToken(Integer userId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(extractClaims(token).get("type"));
        } catch (Exception e) {
            return false;
        }
    }

}