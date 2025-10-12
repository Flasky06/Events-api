package com.tritva.Evently.controller;

import com.tritva.Evently.model.dto.*;
import com.tritva.Evently.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    // USER REGISTRATION
    @Operation(summary = "Register a new user", description = "Registers a user and sends an email verification link.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto,
                                      BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            e -> e.getField(),
                            e -> e.getDefaultMessage(),
                            (existing, replacement) -> existing
                    ));
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        UserResponseDto user = authService.register(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Registration successful! Please verify your email.",
                "user", user
        ));
    }

    // LOGIN
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token on success.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto authDto, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            e -> e.getField(),
                            e -> e.getDefaultMessage(),
                            (existing, replacement) -> existing
                    ));
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        AuthResponseDto response = authService.login(authDto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "data", response
        ));
    }

    // PASSWORD MANAGEMENT
    @Operation(summary = "Forgot password", description = "Sends a password reset email to the user.")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) {
        authService.forgotPassword(forgotPasswordDto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset email sent. Please check your inbox."
        ));
    }

    @Operation(summary = "Reset password", description = "Resets user password after token verification.")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        authService.resetPassword(resetPasswordDto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset successful."
        ));
    }

    // EMAIL VERIFICATION
    @Operation(summary = "Verify email", description = "Verifies user email using verification code.")
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailDto verifyEmailDto) {
        authService.verifyEmail(verifyEmailDto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email verified successfully!"
        ));
    }

    @Operation(summary = "Resend verification email", description = "Resends verification link to the user's email.")
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationDto dto) {
        authService.resendVerificationEmail(dto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification email sent again."
        ));
    }

    // USER PROFILE
    @Operation(summary = "Get current user profile", description = "Fetches details of the currently logged-in user.")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile retrieved successfully"
        ));
    }

    // ADMIN: USER MANAGEMENT
    @Operation(summary = "List all users", description = "Fetches all registered users (admin only).")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers() {
        List<UserResponseDto> users = authService.getAllUsers();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Users retrieved successfully",
                "count", users.size(),
                "data", users
        ));
    }

    @Operation(summary = "Get user by ID", description = "Fetch a specific user by ID (admin only).")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable UUID userId) {
        UserResponseDto user = authService.getUser(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User retrieved successfully",
                "data", user
        ));
    }

    @Operation(summary = "Update user", description = "Updates a user's information (admin only).")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserDto updateUserDto) {
        UserResponseDto updatedUser = authService.updateUser(userId, updateUserDto);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User updated successfully",
                "data", updatedUser
        ));
    }

    @Operation(summary = "Delete user", description = "Deletes a user by ID (admin only).")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deleted successfully"
        ));
    }

    // ======================
    // HEALTH CHECK
    // ======================
    @Operation(summary = "Health check", description = "Checks the status of the authentication service.")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Evently Auth Service",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
