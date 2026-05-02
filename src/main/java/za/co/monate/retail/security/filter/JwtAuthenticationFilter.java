package za.co.monate.retail.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import za.co.monate.retail.security.service.JwtService;

import java.io.IOException;

/**
 * ============================================================================
 * CLASS: JwtAuthenticationFilter
 * PURPOSE: Intercepts HTTP requests, extracts the JWT from the "Authorization" 
 * header, validates it, and tells Spring Security who the user is.
 * ============================================================================
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Check if the user brought their wristband (The Header)
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // If no header, or it doesn't start with "Bearer ", move along (they might be hitting a public endpoint like /login)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract the token (Remove "Bearer " from the string)
        jwt = authHeader.substring(7);
        
        // 3. Extract the email from the token
        userEmail = jwtService.extractUsername(jwt);

        // 4. If we have an email, and the user isn't already authenticated in this session
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Go to the database and pull up their full profile
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Mathematically verify the token hasn't been tampered with
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // 6. Give them the "All Clear" to access the restricted APIs
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Update the Security Context so the rest of the app knows who is logged in
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}