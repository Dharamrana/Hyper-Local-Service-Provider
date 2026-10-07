package com.urbancompany.clone.auth;

import com.urbancompany.clone.model.User;
import com.urbancompany.clone.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PhoneAuthController {
    private final OtpService otpService;
    private final PasswordResetService resetService;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.otp.dev-expose:false}") private boolean devExpose;

    public PhoneAuthController(OtpService otpService, PasswordResetService resetService,
                               UserRepository users, PasswordEncoder encoder) {
        this.otpService = otpService; this.resetService = resetService;
        this.users = users; this.encoder = encoder;
    }

    @PostMapping("/phone/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        try {
            var r = otpService.sendOtp(body.get("phone"));
            return ResponseEntity.ok(Map.of(
                    "phone", r.phone(), "mock", r.mock(),
                    "expiresAt", String.valueOf(r.expiresAt()),
                    "devOtp", r.devOtp() != null ? r.devOtp() : "",
                    "message", r.mock() ? "OTP sent (check server log in dev)" : "OTP sent on SMS/WhatsApp"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Verify OTP. If phone exists -> login. If not, requires name+email to create account (Tier-3: phone-first signup).
     */
    @PostMapping("/phone/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            String phone = OtpService.normalize(body.get("phone"));
            boolean ok = otpService.verifyOtp(phone, body.get("otp"));
            if (!ok) return ResponseEntity.badRequest().body(Map.of("message", "Incorrect OTP"));

            var existing = users.findAll().stream()
                    .filter(u -> phone.equals(OtpService.normalize(u.getPhone())))
                    .findFirst();
            User user;
            if (existing.isPresent()) {
                user = existing.get();
            } else {
                String name = body.get("name");
                String email = body.get("email");
                if (name == null || name.isBlank() || email == null || email.isBlank()) {
                    return ResponseEntity.status(202).body(Map.of(
                            "needsProfile", true, "phone", phone,
                            "message", "OTP verified — tell us your name & email to finish signup"));
                }
                user = new User();
                user.setName(name.trim());
                user.setEmail(email.trim().toLowerCase());
                user.setPhone(phone);
                // Phone-verified accounts get a random password; login is via OTP next time.
                user.setPassword(encoder.encode("OTP-" + phone + "-" + System.nanoTime()));
                user.setRole("CUSTOMER");
                user = users.save(user);
            }
            var auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + (user.getRole() != null ? user.getRole() : "CUSTOMER"))));
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            return ResponseEntity.ok(Map.of("id", user.getId(), "name", user.getName(),
                    "email", user.getEmail(), "phone", user.getPhone(), "message", "Logged in"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgot(@RequestBody Map<String, String> body) {
        String token = resetService.requestReset(body.get("email"));
        // Never reveal whether account exists; dev token only when explicitly enabled.
        return ResponseEntity.ok(Map.of("message", "If an account exists, a reset link has been sent",
                "devToken", (devExpose && token != null) ? token : ""));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> reset(@RequestBody Map<String, String> body) {
        try {
            resetService.resetPassword(body.get("token"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password updated — log in with your new password"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
