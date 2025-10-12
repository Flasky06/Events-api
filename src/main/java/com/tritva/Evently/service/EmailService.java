package com.tritva.Evently.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.from:noreply@language-assessment.com}")
    private String fromEmail;

    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Language Assessment - Password Reset Request");
            message.setText(buildPasswordResetEmailBody(token));
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    public void sendVerificationEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Language Assessment - Email Verification");
            message.setText(buildVerificationEmailBody(token));
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
            // Don't throw exception to prevent registration failure
            log.warn("Registration will continue without email verification");
        }
    }

    private String buildPasswordResetEmailBody(String token) {
        return String.format("""
            Hello,
            
            We received a request to reset your password for your Language Assessment account.
            
            If you made this request, please click the following link to reset your password:
            %s/reset-password?token=%s
            
            This link will expire in 1 hour for security reasons.
            
            If you didn't request a password reset, please ignore this email.
            
            Best regards,
            Language Assessment Team
            """, frontendUrl, token);
    }

    private String buildVerificationEmailBody(String token) {
        return String.format("""
            Welcome to Language Assessment!
            
            Thank you for creating an account. To complete your registration, please verify your email address by clicking the link below:
            
            %s/verify-email?token=%s
            
            This verification link will expire in 24 hours.
            
            Once verified, you'll be able to access all language assessment features.
            
            Best regards,
            Language Assessment Team
            """, frontendUrl, token);
    }
}