package com.urbancompany.clone.controller;

import com.urbancompany.clone.model.Location;
import com.urbancompany.clone.model.ServiceProvider;
import com.urbancompany.clone.model.User;
import com.urbancompany.clone.repository.ServiceProviderRepository;
import com.urbancompany.clone.repository.UserRepository;
import com.urbancompany.clone.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * JSON auth API used by the signup page and booking wizard.
 * Browser form login is handled by Spring Security itself (POST /login).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final ServiceProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService,
                          ServiceProviderRepository providerRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Sign up + immediate auto-login (UC-style: one step into booking). */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody User signup, HttpServletRequest request) {
        if (signup.getPassword() == null || signup.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password must be at least 6 characters"));
        }
        String requestedRole = signup.getRole() == null ? "CUSTOMER" : signup.getRole().trim().toUpperCase();
        if ("ADMIN".equals(requestedRole)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Admin accounts are created by the platform. Please sign up as Customer or Service Provider."));
        }
        if (!"CUSTOMER".equals(requestedRole) && !"PROVIDER".equals(requestedRole)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Unknown role: " + requestedRole));
        }
        try {
            if ("PROVIDER".equals(requestedRole)) {
                if (userRepository.findByEmail(signup.getEmail()).isPresent()
                        || providerRepository.findByEmail(signup.getEmail()).isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "An account with this email already exists"));
                }
                ServiceProvider provider = new ServiceProvider();
                provider.setName(signup.getName());
                provider.setEmail(signup.getEmail());
                provider.setPhone(signup.getPhone());
                provider.setPassword(passwordEncoder.encode(signup.getPassword()));
                provider.setRole("PROVIDER");
                provider.setLocation(signup.getLocation() != null ? signup.getLocation()
                        : new Location(30.3429, 77.9620, "Prem Nagar, Dehradun", "248007", "Near Prem Nagar Market"));
                provider.setRating(0.0);
                provider.setTotalReviews(0);
                provider.setIsAvailable(false); // live only after admin KYC verification
                provider.setIsVerified(false);
                ServiceProvider savedProvider = providerRepository.save(provider);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        savedProvider.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_PROVIDER")));
                SecurityContextHolder.getContext().setAuthentication(auth);
                request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                return ResponseEntity.ok(Map.of(
                        "id", savedProvider.getId(), "name", savedProvider.getName(),
                        "email", savedProvider.getEmail(),
                        "phone", savedProvider.getPhone() != null ? savedProvider.getPhone() : "",
                        "role", "PROVIDER", "kycStatus", "PENDING"));
            }
            signup.setRole("CUSTOMER");
            User saved = userService.createUser(signup);
            // Auto-login so the user lands straight in the booking flow.
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    saved.getEmail(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + (saved.getRole() != null ? saved.getRole() : "CUSTOMER"))));
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            return ResponseEntity.ok(publicProfile(saved));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    /** Current session profile; 401 when logged out (used to prefill booking + header). */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "Not logged in"));
        }
        return userService.getUserByEmail(authentication.getName())
                .map(u -> ResponseEntity.ok(publicProfile(u)))
                .orElse(ResponseEntity.status(401).body(Map.of("message", "Account not found")));
    }

    private Map<String, Object> publicProfile(User u) {
        return Map.of(
                "id", u.getId(),
                "name", u.getName(),
                "email", u.getEmail(),
                "phone", u.getPhone() != null ? u.getPhone() : "",
                "role", u.getRole() != null ? u.getRole() : "CUSTOMER");
    }
}
