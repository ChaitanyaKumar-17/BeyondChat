package com.manu.beyondchat.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtUtility {

    // 1. The Secret Key
    // JJWT requires a cryptographic key of at least 256 bits (32 bytes) for HMAC-SHA.
    // Keep it in application.properties!
    @Value("${spring.application.security.jwt.secret-key}")
    private String secretKey;

    // Token validity (e.g., 24 hours in milliseconds)
    @Value("${spring.application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Called by AuthController when a user successfully logs in.
     */

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey()) // Sign the token with our secret math
                .compact();
    }

    /**
     * Called by JwtAuthenticationFilter to see who owns the incoming token.
     */

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Called by JwtAuthenticationFilter to mathematically prove the token is real.
     */

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Is the username correct AND is the token unexpired?
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey()) // Validate the signature against our secret key
                .build()
                .parseSignedClaims(token) // Parses the token and checks for tampering
                .getPayload(); // Extracts the JSON body of the token
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
