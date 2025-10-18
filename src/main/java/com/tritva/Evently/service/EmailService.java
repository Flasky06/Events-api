package com.tritva.Evently.service;

import com.tritva.Evently.model.entity.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.from:noreply@evently.com}")
    private String fromEmail;

    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Evently - Password Reset Request");
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
            message.setSubject("Evently - Email Verification");
            message.setText(buildVerificationEmailBody(token));
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
            log.warn("Registration will continue without email verification");
        }
    }

    public void sendTicketEmail(String to, Ticket ticket) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your Ticket for " + ticket.getEvent().getName());
            helper.setText(buildTicketEmailBody(ticket), true); // true = HTML
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            log.info("Ticket email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send ticket email to: {}", to, e);
            throw new RuntimeException("Failed to send ticket email: " + e.getMessage());
        }
    }

    private String buildPasswordResetEmailBody(String token) {
        return String.format("""
            Hello,
            
            We received a request to reset your password for your Evently account.
            
            If you made this request, please click the following link to reset your password:
            %s/reset-password?token=%s
            
            This link will expire in 1 hour for security reasons.
            
            If you didn't request a password reset, please ignore this email.
            
            Best regards,
            Evently Team
            """, frontendUrl, token);
    }

    private String buildVerificationEmailBody(String token) {
        return String.format("""
            Welcome to Evently!
            
            Thank you for creating an account. To complete your registration, please verify your email address by clicking the link below:
            
            %s/verify-email?token=%s
            
            This verification link will expire in 24 hours.
            
            Once verified, you'll be able to access all features.
            
            Best regards,
            Evently Team
            """, frontendUrl, token);
    }

    private String buildTicketEmailBody(Ticket ticket) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a");
        String eventDate = ticket.getEvent().getStartDateTime().format(formatter);

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; }
                    .ticket-info { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }
                    .qr-code { text-align: center; margin: 20px 0; }
                    .qr-code img { max-width: 300px; border: 2px solid #ddd; padding: 10px; }
                    .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                    .important { color: #d32f2f; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Your Ticket is Ready!</h1>
                    </div>
                    <div class="content">
                        <h2>Thank you for your purchase!</h2>
                        <p>Here are your ticket details for:</p>
                        
                        <div class="ticket-info">
                            <h3>%s</h3>
                            <p><strong>Date & Time:</strong> %s</p>
                            <p><strong>Location:</strong> %s, %s</p>
                            <p><strong>Ticket Number:</strong> %s</p>
                            <p><strong>Verification Code:</strong> %s</p>
                            <p><strong>Price:</strong> KES %.2f</p>
                        </div>

                        <div class="qr-code">
                            <h3>Your QR Code</h3>
                            <p>Please present this QR code at the event entrance:</p>
                            <img src="%s" alt="Ticket QR Code" />
                        </div>

                        <div class="ticket-info">
                            <p class="important">⚠️ IMPORTANT INFORMATION:</p>
                            <ul>
                                <li>Save this email or take a screenshot of the QR code</li>
                                <li>Arrive at least 30 minutes before the event starts</li>
                                <li>This ticket is non-transferable and non-refundable</li>
                                <li>You may be asked to show a valid ID along with this ticket</li>
                            </ul>
                        </div>

                        <p>If you have any questions, please contact the event organizer or reply to this email.</p>
                        
                        <p>See you at the event!</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email from Evently. Please do not reply directly to this email.</p>
                        <p>&copy; 2025 Evently. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                ticket.getEvent().getName(),
                eventDate,
                ticket.getEvent().getLocation(),
                ticket.getEvent().getCounty(),
                ticket.getTicketNumber(),
                ticket.getVerificationCode(),
                ticket.getPrice(),
                ticket.getQrCodeUrl()
        );
    }
}