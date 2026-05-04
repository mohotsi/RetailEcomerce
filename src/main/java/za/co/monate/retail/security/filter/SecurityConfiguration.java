package za.co.monate.retail.security.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import za.co.monate.retail.security.filter.JwtAuthenticationFilter;

/**
 * ============================================================================
 * CLASS: SecurityConfiguration
 * PURPOSE: The master rulebook. Defines which URLs require which AppRoles.
 * ============================================================================
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF. We don't need it because we use stateless JWT tokens.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Define the URL "Guest List" Rules
                .authorizeHttpRequests(auth -> auth

                                // --- PUBLIC ENDPOINTS (No Token Required) ---
                                // Allow anyone to register, log in, and view public product catalogs
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/catalog/**").permitAll()
                                .requestMatchers("/api/v1/public/catalog/**").permitAll()
                                // --- SYSTEM ADMIN ONLY: CATEGORY CONFIGURATION ---
                                // Only an Admin can build the "buckets" that the Merchandisers use.
                                .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").
                                hasAuthority("ROLE_SYSTEM_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**")
                                .hasAuthority("ROLE_SYSTEM_ADMIN")

// --- MERCHANDISER & ADMIN: PRODUCT INGESTION ---
                                .requestMatchers(HttpMethod.POST, "/api/v1/catalog/**").hasAnyAuthority("ROLE_MERCHANDISER", "ROLE_SYSTEM_ADMIN")
                                // --- ROLE-RESTRICTED ENDPOINTS (Strict Access) ---
                                // CRITICAL: Only staff/systems can add or edit products (API imports, CSV uploads, etc.)
                                .requestMatchers(HttpMethod.POST, "/api/v1/catalog/**")
                                .hasAnyAuthority("ROLE_MERCHANDISER", "ROLE_SYSTEM_ADMIN")
                                .requestMatchers("/api/v1/admin/catalog/**")
                                .hasAnyAuthority("ROLE_MERCHANDISER", "ROLE_SYSTEM_ADMIN")

                                // AI Bots get their own dedicated endpoints for high-speed negotiation
                                .requestMatchers("/api/v1/bots/negotiate/**").hasAnyAuthority("ROLE_MACHINE_AGENT")

                                // --- AUTHENTICATED ENDPOINTS (Requires valid Token) ---
                                // Everything else (like checkouts, viewing profile) requires a valid login
                                .anyRequest().authenticated()
                )

                // 3. Tell Spring we are using stateless APIs (No traditional server sessions)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Wire up our custom Authentication Provider (The Black Box)
                .authenticationProvider(authenticationProvider)

                // 5. Place our custom JWT Bouncer BEFORE the standard Spring Username/Password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}