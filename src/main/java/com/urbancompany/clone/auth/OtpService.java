package com.urbancompany.clone.auth;

import com.urbancompany.clone.model.PhoneOtp;
import com.urbancompany.clone.repository.PhoneOtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional
public class OtpService {
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SEC = 30;

    private final PhoneOtpRepository repo;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    @Value("${msg91.api-key:}") private String msg91Key;
    @Value("${msg91.sender-id:HLSPIN}") private String senderId;
    @Value("${app.otp.dev-expose:false}") private boolean devExpose;

    public OtpService(PhoneOtpRepository repo, PasswordEncoder encoder) {
        this.repo = repo; this.encoder = encoder;
    }

    public record SendResult(String phone, boolean mock, String devOtp, LocalDateTime expiresAt) {}

    public SendResult sendOtp(String rawPhone) {
        String phone = normalize(rawPhone);
        // cooldown
        repo.findTopByPhoneOrderByCreatedAtDesc(phone).ifPresent(last -> {
            if (last.getCreatedAt() != null && last.getCreatedAt().plusSeconds(RESEND_COOLDOWN_SEC).isAfter(LocalDateTime.now())) {
                throw new IllegalStateException("Please wait " + RESEND_COOLDOWN_SEC + "s before resending OTP");
            }
        });
        String otp = String.format("%06d", random.nextInt(1_000_000));
        PhoneOtp entity = new PhoneOtp(null, phone, encoder.encode(otp),
                LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES), 0, false, LocalDateTime.now());
        repo.save(entity);
        boolean mock = msg91Key == null || msg91Key.isBlank();
        if (mock) {
            log.info("[OTP MOCK] phone={} otp={} (set MSG91_API_KEY for real SMS)", phone, otp);
        } else {
            // TODO(prod): POST https://control.msg91.com/api/v5/otp?template_id=...&mobile=91{phone}&otp={otp}
            // Headers: authkey={msg91Key}. Keep OTP server-side only once wired.
            log.info("[OTP MSG91 QUEUED] phone={} sender={}", phone, senderId);
        }
        return new SendResult(phone, mock, (mock && devExpose) ? otp : null, entity.getExpiresAt());
    }

    public boolean verifyOtp(String rawPhone, String otp) {
        String phone = normalize(rawPhone);
        PhoneOtp entity = repo.findTopByPhoneOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> new IllegalArgumentException("No OTP requested for this number"));
        if (Boolean.TRUE.equals(entity.getVerified())) throw new IllegalStateException("OTP already used");
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("OTP expired, request a new one");
        if (entity.getAttempts() >= MAX_ATTEMPTS) throw new IllegalStateException("Too many attempts, request a new OTP");
        entity.setAttempts(entity.getAttempts() + 1);
        if (!encoder.matches(otp, entity.getOtpHash())) {
            repo.save(entity);
            return false;
        }
        entity.setVerified(true);
        repo.save(entity);
        return true;
    }

    public static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("Phone is required");
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("91") && digits.length() == 12) digits = digits.substring(2);
        if (digits.length() != 10) throw new IllegalArgumentException("Enter a valid 10-digit mobile number");
        if (!digits.matches("[6-9][0-9]{9}")) throw new IllegalArgumentException("Enter a valid Indian mobile number");
        return digits;
    }
}
