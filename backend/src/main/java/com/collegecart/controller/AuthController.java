package com.collegecart.controller;

import com.collegecart.dto.StudentRegistrationRequest;
import com.collegecart.dto.VendorRegistrationRequest;
import com.collegecart.service.AuthService;
import com.collegecart.dto.LoginRequest;
import com.collegecart.dto.AuthResponse;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/student")
    public ResponseEntity<String> registerStudent(
            @Valid @RequestBody StudentRegistrationRequest request) {

        authService.registerStudent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Student registered successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            Authentication authentication) {

        return ResponseEntity.ok(
                Map.of(
                        "email", authentication.getName(),
                        "authorities", authentication.getAuthorities()
                )
        );
    }

    @PostMapping("/register/vendor")
    public ResponseEntity<String> registerVendor(
            @Valid @RequestBody VendorRegistrationRequest request) {

        authService.registerVendor(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Vendor registration submitted successfully. Awaiting admin approval.");
    }

    @PostMapping("/login")
public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request) {

    AuthResponse response = authService.login(request);

    return ResponseEntity.ok(response);
}
}
