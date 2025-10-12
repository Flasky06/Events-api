package com.tritva.Evently.service.impl;

import com.tritva.Evently.mapper.UserMapper;
import com.tritva.Evently.model.Role;
import com.tritva.Evently.model.dto.*;
import com.tritva.Evently.model.entity.User;
import com.tritva.Evently.repository.UserRepository;
import com.tritva.Evently.service.AuthService;
import com.tritva.Evently.service.EmailService;
import com.tritva.Evently.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Override
    public AuthResponseDto login(AuthDto authDto) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword())
            );

            // Get user details
            User user = userRepository.findByEmail(authDto.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            // Check if email is verified
            if (Boolean.FALSE.equals(user.getEmailVerified())) {
                throw new RuntimeException("Please verify your email before logging in. Check your inbox for verification email.");
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

            log.info("User logged in successfully: {}", user.getEmail());

            return new AuthResponseDto(
                    token,
                    user.getEmail(),
                    user.getRole(),
                    user.getId(),
                    "Login successful"
            );

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", authDto.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
    }

    @Override
    public UserResponseDto register(RegisterDto registerDto) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email already exists. Please use a different email or try logging in.");
        }

        // Create new user
        User user = User.builder()
                .email(registerDto.getEmail())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .fullName(registerDto.getFullName())
                .role(Role.USER)
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .build();

        User savedUser = userRepository.save(user);

        // Send verification email
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationToken());
        } catch (Exception e) {
            log.error("Failed to send verification email during registration", e);
            // Don't fail registration if email fails
        }

        log.info("New user registered: {}", savedUser.getEmail());
        return userMapper.toDto(savedUser);
    }

    @Override
    public void forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        User user = userRepository.findByEmail(forgotPasswordDto.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        // Send reset email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        } catch (Exception e) {
            log.error("Failed to send password reset email", e);
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }

        log.info("Password reset email sent to: {}", user.getEmail());
    }

    @Override
    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        User user = userRepository.findByResetToken(resetPasswordDto.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Check if token is expired
        if (user.getResetTokenExpiry() != null && user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new password reset.");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Override
    public void verifyEmail(VerifyEmailDto verifyEmailDto) {
        User user = userRepository.findByVerificationToken(verifyEmailDto.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email is already verified");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);
        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    @Override
    public void resendVerificationEmail(ResendVerificationDto resendVerificationDto) {
        User user = userRepository.findByEmail(resendVerificationDto.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email is already verified");
        }

        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);

        userRepository.save(user);

        // Send verification email
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        } catch (Exception e) {
            log.error("Failed to resend verification email", e);
            throw new RuntimeException("Failed to send verification email. Please try again later.");
        }

        log.info("Verification email resent to: {}", user.getEmail());
    }

    @Override
    public UserResponseDto getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto getUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateUser(UUID userId, UpdateUserDto userUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields if provided
        if (userUpdateDto.getFullName() != null && !userUpdateDto.getFullName().isBlank()) {
            user.setFullName(userUpdateDto.getFullName());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getEmail());
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
        log.info("User deleted: {}", user.getEmail());
    }
}