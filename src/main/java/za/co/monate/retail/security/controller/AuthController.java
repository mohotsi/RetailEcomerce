package za.co.monate.retail.security.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.monate.retail.security.dto.AuthenticationRequest;
import za.co.monate.retail.security.dto.AuthenticationResponse;
import za.co.monate.retail.security.dto.RegisterRequest;
import za.co.monate.retail.security.service.AuthenticationService;

/**
 * ============================================================================
 * CLASS: AuthController
 * PURPOSE: Exposes the login endpoints to the internet.
 * Notice the URL maps to "/api/v1/auth" which we explicitly permitted
 * everyone to access in our SecurityConfig earlier!
 * ============================================================================
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService service;

    // NEW: The Sign-Up Endpoint
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        // Hand the request payload to our service, and return the token with a 200 OK status
        return ResponseEntity.ok(service.authenticate(request));
    }
}