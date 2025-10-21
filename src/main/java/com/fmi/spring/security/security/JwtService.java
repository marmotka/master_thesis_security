package com.fmi.spring.security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    //todo fix later to use val instead of hardcoded
//    @Value("${jwt.expiration}")
//    private String jwtExpirationMs;


    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(getSigningKey())
                .compact();
    }

    public List<GrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);

        Object rolesObj = claims.get("roles");
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (rolesObj instanceof Collection<?>) {
            for (Object role : (Collection<?>) rolesObj) {
                if (role instanceof Map<?, ?> roleMap) {
                    Object authority = roleMap.get("authority");
                    if (authority != null) {
                        authorities.add(new SimpleGrantedAuthority(authority.toString()));
                    }
                }
            }
        }

        return authorities;
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }


}

