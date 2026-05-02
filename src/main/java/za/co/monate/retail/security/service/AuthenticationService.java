package za.co.monate.retail.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.monate.retail.identity.model.AppUser;
import za.co.monate.retail.identity.model.enums.AccountStatus;
import za.co.monate.retail.identity.repository.AppUserRepository; // You'll need a basic Spring Data JPA repository for AppUser
import za.co.monate.retail.security.dto.AuthenticationRequest;
import za.co.monate.retail.security.dto.AuthenticationResponse;
import za.co.monate.retail.security.dto.RegisterRequest;

/**
 * ============================================================================
 * CLASS: AuthenticationService
 * PURPOSE: The VIP Manager. It verifies passwords and issues tokens.
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AppUserRepository repository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder; // NEW: The BCrypt Tool

    /**
     * NEW: THE REGISTRATION FLOW
     */
    public AuthenticationResponse register(RegisterRequest request) {

        // 1. Build the new user profile
        var user = AppUser.builder()
                .email(request.getEmail())
                // CRITICAL: Never save request.getPassword() directly!
                // We use the encoder to turn "password123" into a mathematical hash.
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                // By default, let's make them active immediately
                .status(AccountStatus.ACTIVE)
                .build();

        // 2. Save them to the PostgreSQL database
        repository.save(user);

        // 3. Automatically log them in!
        // We generate a JWT right now so the frontend doesn't force them
        // to type their password a second time just to log in.
        var jwtToken = jwtService.generateToken(user);

        // 4. Return the VIP Wristband
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(user.getRole().name())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        
        // 1. The Heavy Lifting (Password Verification)
        // This AuthenticationManager talks to Spring Security under the hood.
        // It takes the raw password, hashes it, and compares it to the database.
        // If the password is wrong, or the account is banned, this line throws an Exception instantly.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Load the Profile
        // If we reach this line, the password was 100% correct.
        AppUser user = repository.findByEmail(request.getEmail())
                .orElseThrow(); // We know they exist because authenticationManager just verified them

        // 3. Print the Wristband
        // We pass the full user profile to our Token Factory.
        String jwtToken = jwtService.generateToken(user);

        // 4. Hand it to the User
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(user.getRole().name())
                .build();
    }
}