package com.ivantrykosh.app.budgettracker.server.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtUtil class provides utility methods for JWT (JSON Web Token) handling.
 */
@Component
public class JwtUtil {
    @Value("${spring.security.secret-key}")
    private String SECRET_KEY; // Secret key
    private final long EXPIRATION_TIME = 7*24*60*60*1000; // 7 days in milliseconds

    /**
     * Extracts the username from a given JWT token.
     *
     * @param token The JWT token
     * @return The username extracted from the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a given JWT token.
     *
     * @param token The JWT token
     * @return The expiration date extracted from the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a given JWT token using a claims resolver function.
     *
     * @param token The JWT token
     * @param claimsResolver The function to resolve the desired claim
     * @param <T> The type of the claim
     * @return The extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a given JWT token.
     *
     * @param token The JWT token
     * @return All claims extracted from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if a given JWT token has expired.
     *
     * @param token The JWT token
     * @return True if the token has expired, otherwise false
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates a JWT token against a UserDetails object.
     *
     * @param token The JWT token
     * @param userDetails The UserDetails object to validate against
     * @return True if the token is valid for the given UserDetails, otherwise false
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Generates a new JWT token for a given username.
     *
     * @param username The username for which the token is generated
     * @return The generated JWT token
     */
    public String generateToken(String username){
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * Creates a new JWT token with specified claims and subject.
     *
     * @param claims The claims to include in the token
     * @param username The subject (username) for the token
     * @return The created JWT token
     */
    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    /**
     * Gets the signing key for JWT token verification.
     *
     * @return The signing key
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
