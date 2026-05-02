package za.co.monate.retail.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import za.co.monate.retail.security.filter.JwtAuthenticationFilter;

/**
 * ============================================================================
 * CLASS: SecurityConfig
 * PURPOSE: The master rulebook. Defines which URLs require which AppRoles.
 * ============================================================================
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // We disable CSRF because JWTs are immune to it (they are stored in local storage, not cookies)
            .csrf(csrf -> csrf.disable())
            
            // Define exactly who can access what
            .authorizeHttpRequests(auth -> auth
                // Anyone can view the login page, register, or browse public products
                .requestMatchers("/api/v1/auth/**", "/api/v1/public/catalog/**").permitAll()
                
                // Only staff can add or edit products
                .requestMatchers("/api/v1/admin/catalog/**").hasAnyRole("MERCHANDISER", "SYSTEM_ADMIN")
                
                // AI Bots get their own dedicated endpoints for high-speed negotiation
                .requestMatchers("/api/v1/bots/negotiate/**").hasRole("MACHINE_AGENT")
                
                // Everything else (like checkouts, viewing profile) requires a valid login
                .anyRequest().authenticated()
            )
            
            // Tell Spring we are using stateless APIs (No traditional server sessions)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authenticationProvider(authenticationProvider)
            
            // Place our custom JWT Bouncer *before* the standard Spring Username/Password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}