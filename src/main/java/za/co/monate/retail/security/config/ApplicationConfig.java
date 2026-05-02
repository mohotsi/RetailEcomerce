package za.co.monate.retail.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.co.monate.retail.identity.repository.AppUserRepository;

/**
 * ============================================================================
 * CLASS: ApplicationConfig
 * PURPOSE: Holds the core "Beans" (global objects) that Spring Security needs 
 * to function. It wires together the Database, the Cryptography, and the 
 * Authentication Manager.
 * ============================================================================
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final AppUserRepository repository;

    /**
     * 1. THE FILING CABINET (UserDetailsService)
     * We tell Spring exactly how to look up a user. When Spring asks for a user 
     * by their "username" (which is their email in our system), we query our DB.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in the Monate Database"));
    }

    /**
     * 2. THE CRYPTOGRAPHER (PasswordEncoder)
     * We define the mathematical algorithm used to scramble passwords. 
     * BCrypt is the industry standard. It automatically salts the passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 3. THE DATA ACCESS ENGINE (AuthenticationProvider)
     * This object takes the raw password the user typed, grabs the hashed password 
     * from the Filing Cabinet, and gives them both to the Cryptographer to see 
     * if they match.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        // Hand it the cabinet
        authProvider.setPasswordEncoder(passwordEncoder());       // Hand it the math rules
        return authProvider;
    }

    /**
     * 4. THE BOSS (AuthenticationManager)
     * This is the manager we called inside our `AuthenticationService`. 
     * We just ask Spring to provide us its default manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}