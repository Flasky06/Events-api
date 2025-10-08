package com.tritva.assessment.controller;


import com.tritva.assessment.model.dto.*;
import com.tritva.assessment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto,
                                      BindingResult result) {
        try {
            if (result.hasErrors()) {
                Map<String, String> errors = result.getFieldErrors().stream()
                        .collect(Collectors.toMap(
                                error -> error.getField(),
                                error -> error.getDefaultMessage(),
                                (existing, replacement) -> existing
                        ));
                return ResponseEntity.badRequest().body(Map.of("errors", errors));
            }

            UserResponseDto user = authService.register(registerDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful! Please check your email to verify your account.");
            response.put("user", user);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto authDto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                Map<String, String> errors = result.getFieldErrors().stream()
                        .collect(Collectors.toMap(
                                error -> error.getField(),
                                error -> error.getDefaultMessage(),
                                (existing, replacement) -> existing
                        ));
                return ResponseEntity.badRequest().body(Map.of("errors", errors));
            }

            AuthResponseDto response = authService.login(authDto);

            Map<String, Object> loginResponse = new HashMap<>();
            loginResponse.put("success", true);
            loginResponse.put("message", "Login successful");
            loginResponse.put("data", response);

            return ResponseEntity.ok(loginResponse);

        } catch (RuntimeException e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid
                                                @RequestBody ForgotPasswordDto forgotPasswordDto) {
        try {
            authService.forgotPassword(forgotPasswordDto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password reset email sent. Please check your inbox and spam folder."
            ));
        } catch (RuntimeException e) {
            log.error("Forgot password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        try {
            authService.resetPassword(resetPasswordDto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password reset successful. You can now login with your new password."
            ));
        } catch (RuntimeException e) {
            log.error("Reset password error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailDto verifyEmailDto) {
        try {
            authService.verifyEmail(verifyEmailDto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email verified successfully! You can now login."
            ));
        } catch (RuntimeException e) {
            log.error("Email verification error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody ResendVerificationDto resendVerificationDto) {
        try {
            authService.resendVerificationEmail(resendVerificationDto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Verification email sent. Please check your inbox and spam folder."
            ));
        } catch (RuntimeException e) {
            log.error("Resend verification error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Profile endpoint for current user
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            // This would need SecurityContext to get current user
            // For now, return placeholder
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile retrieved successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Get profile error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Admin endpoints
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserResponseDto> users = authService.getAllUsers();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Users retrieved successfully",
                    "data", users,
                    "count", users.size()
            ));
        } catch (RuntimeException e) {
            log.error("Get all users error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable UUID userId) {
        try {
            UserResponseDto user = authService.getUser(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User retrieved successfully",
                    "data", user
            ));
        } catch (RuntimeException e) {
            log.error("Get user error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        try {
            UserResponseDto updatedUser = authService.updateUser(userId, updateUserDto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User updated successfully",
                    "data", updatedUser
            ));
        } catch (RuntimeException e) {
            log.error("Update user error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        try {
            authService.deleteUser(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User deleted successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Delete user error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Language Assessment Auth Service");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
}