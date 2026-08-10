package org.raghul.auth_engine.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


@Service
public class JwtService {
    //TODO - generateToken(UserDetails userDetails) - what it does
    // extractUsername(String token),
    // isTokenValid(String token, UserDetails userDetails).

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(Authentication authentication, CustomUserDetails customUserDetails) {
        var builder = Jwts.builder()
                .subject(customUserDetails.getEmail())
                .claim("authorities", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .claim("userId", customUserDetails.getUserId())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey());

        if (customUserDetails.getTenantId() != null) {
            builder.claim("tenantId", customUserDetails.getTenantId());
        }
        return builder.compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public Claims extractUserDetailsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public List<String> extractRoles(String token) {
        return extractUserDetailsFromToken(token).get("roles", List.class);
    }

    public List<String> extractAuthorities(String token) {
        return extractUserDetailsFromToken(token).get("authorities", List.class);
    }
    public boolean isTokenValid(String token) {

        try {
            System.out.println("isTokenValid is called");
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
