package za.co.monate.retail.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ============================================================================
 * CLASS: JwtService
 * PURPOSE: The cryptographic engine of the B2B Hub. 
 * It signs tokens (wristbands) so hackers cannot forge a fake login, and it 
 * reads incoming tokens to verify who is making the API request.
 * ============================================================================
 */
@Service
public class JwtService {

    /*
     * TEACHING MOMENT: The @Value annotation
     * Instead of hardcoding our secret key in the Java file (which is a massive 
     * security risk if pushed to GitHub), Spring Boot reads the application.properties 
     * file when the server starts and safely injects the values right here.
     */
    @Value("${monate.security.jwt.secret-key}")
    private String secretKey;

    @Value("${monate.security.jwt.expiration-time-ms}")
    private long jwtExpirationMs;

    /**
     * Extracts the email (Username) from the JWT string.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Helper method to generate a standard token with no extra claims.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * The Token Factory. Builds the JWT with the user's details, issue date, 
     * expiration date, and signs it mathematically using our Secret Key.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * The Bouncer Check. Ensures the token belongs to the user trying to use it 
     * and that the token hasn't expired yet.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the token. If a hacker tampers with the token payload, this method 
     * will throw an exception because the Signature won't match the Secret Key.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Converts our text-based secret key into a cryptographic Key object 
     * that the JJWT library can use for its math.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}