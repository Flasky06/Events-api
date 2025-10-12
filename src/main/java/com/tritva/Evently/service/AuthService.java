package com.tritva.Evently.service;


import com.tritva.Evently.model.dto.*;

import java.util.List;
import java.util.UUID;

public interface AuthService {

    AuthResponseDto login(AuthDto authDto);
    UserResponseDto register(RegisterDto registerDto);
    void forgotPassword(ForgotPasswordDto forgotPasswordDto);
    void resetPassword(ResetPasswordDto resetPasswordDto);

    // Email verification methods (updated signatures)
    void verifyEmail(VerifyEmailDto verifyEmailDto);
    void resendVerificationEmail(ResendVerificationDto resendVerificationDto);

    // User profile method
    UserResponseDto getCurrentUserProfile(String email);

    // Admin methods
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUser(UUID userId);
    UserResponseDto updateUser(UUID userId, UpdateUserDto userUpdateDto);
    void deleteUser(UUID userId);
}
