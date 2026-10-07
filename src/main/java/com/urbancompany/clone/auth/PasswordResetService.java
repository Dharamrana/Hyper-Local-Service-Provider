package com.urbancompany.clone.auth;

import com.urbancompany.clone.model.PasswordResetToken;
import com.urbancompany.clone.repository.PasswordResetTokenRepository;
import com.urbancompany.clone.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private final PasswordResetTokenRepository tokens;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public PasswordResetService(PasswordResetTokenRepository tokens, UserRepository users, PasswordEncoder encoder) {
        this.tokens = tokens; this.users = users; this.encoder = encoder;
    }

    /** Always returns ok (no email enumeration). Link is logged in dev / emailed in prod. */
    public String requestReset(String email) {
        if (email == null || email.isBlank()) return null;
        var user = users.findByEmail(email.trim().toLowerCase());
        if (user.isEmpty()) { log.info("[PWD RESET] no account for {}", email); return null; }
        tokens.deleteByEmail(email.trim().toLowerCase());
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString().substring(0, 8);
        tokens.save(new PasswordResetToken(null, email.trim().toLowerCase(), token,
                LocalDateTime.now().plusMinutes(30), false, LocalDateTime.now()));
        String link = "/reset-password?token=" + token;
        log.info("[PWD RESET LINK] email={} link={} (email this via NotificationService in prod)", email, link);
        return token; // controller decides whether to expose (dev only)
    }

    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        PasswordResetToken t = tokens.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));
        if (Boolean.TRUE.equals(t.getUsed()) || t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired reset link");
        }
        var user = users.findByEmail(t.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        user.setPassword(encoder.encode(newPassword));
        users.save(user);
        t.setUsed(true); tokens.save(t);
    }
}
