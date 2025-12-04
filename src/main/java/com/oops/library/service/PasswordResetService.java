package com.oops.library.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.oops.library.enchanted.exception.EnchantedLibraryException;
import com.oops.library.entity.PasswordResetToken;
import com.oops.library.entity.User;
import com.oops.library.repository.PasswordResetTokenRepository;
import com.oops.library.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private static final int OTP_LENGTH = 6;
    private static final int OTP_TTL_MINUTES = 10;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {

        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void requestPasswordReset(String email) {
        logger.info("Password reset requested for email: {}", email);

        if (email == null || email.isBlank()) {
            logger.warn("Password reset request failed: email is null or blank");
            return;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("Password reset request ignored: no user found for email {}", email);
            return; // Prevent user enumeration
        }

        User user = userOpt.get();
        logger.info("User found for password reset: {} (ID: {})", user.getEmail(), user.getId());

        // Invalidate previous unused OTPs
        tokenRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .ifPresent(existing -> {
                    existing.setUsed(true);
                    tokenRepository.save(existing);
                    logger.debug("Previous OTP invalidated for user {}", user.getEmail());
                });

        // Create new OTP token
        String otp = generateOtp();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setOtp(otp);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES));

        tokenRepository.save(token);
        logger.info("New OTP generated for user {}: {}", user.getEmail(), otp);

        emailService.sendPasswordResetOtp(user.getEmail(), otp);
        logger.info("Password reset OTP email sent to {}", user.getEmail());
    }

    public void resetPassword(String email, String otp, String newPassword) throws EnchantedLibraryException {
        logger.info("Password reset attempt for email: {}", email);

        if (email == null || otp == null || newPassword == null ||
                email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            logger.error("Password reset failed: missing required fields");
            throw new EnchantedLibraryException("Invalid reset request");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Password reset failed: user not found for email {}", email);
                    return new EnchantedLibraryException("Invalid email or OTP");
                });

        PasswordResetToken token = tokenRepository.findByUserAndOtpAndUsedFalse(user, otp)
                .orElseThrow(() -> {
                    logger.error("Password reset failed: invalid OTP for user {}", user.getEmail());
                    return new EnchantedLibraryException("Invalid email or OTP");
                });

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("Password reset failed: OTP expired for user {}", user.getEmail());
            throw new EnchantedLibraryException("OTP has expired");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password updated successfully for user {}", user.getEmail());

        // Mark OTP as used
        token.setUsed(true);
        tokenRepository.save(token);
        logger.debug("OTP marked as used for user {}", user.getEmail());

        // Clean up old expired tokens
        tokenRepository.deleteByUserAndExpiresAtBefore(user, LocalDateTime.now().minusHours(1));
        logger.debug("Old expired tokens cleaned for user {}", user.getEmail());
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int number = random.nextInt(bound);
        String otp = String.format("%0" + OTP_LENGTH + "d", number);
        logger.debug("OTP generated: {}", otp);
        return otp;
    }
}
