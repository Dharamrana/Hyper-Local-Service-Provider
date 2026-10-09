package com.urbancompany.clone.admin;

import com.urbancompany.clone.model.ServiceProvider;
import com.urbancompany.clone.repository.ServiceProviderRepository;
import com.urbancompany.clone.repository.ServiceRepository;
import com.urbancompany.clone.repository.ServiceRequestRepository;
import com.urbancompany.clone.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin APIs — protect with ROLE_ADMIN in SecurityConfig.
 * KYC flow: provider registers -> isVerified=false -> admin verifies/rejects.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ServiceProviderRepository providers;
    private final ServiceRepository services;
    private final ServiceRequestRepository requests;
    private final UserRepository users;

    public AdminController(ServiceProviderRepository p, ServiceRepository s, ServiceRequestRepository r,
                           UserRepository u) {
        this.providers = p; this.services = s; this.requests = r; this.users = u;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        double revenue = requests.findAll().stream()
                .filter(r -> "COMPLETED".equalsIgnoreCase(String.valueOf(r.getStatus())))
                .mapToDouble(r -> {
                    Double v = r.getFinalPrice(); // total charged on completion
                    return v != null ? v : 0.0;
                }).sum();
        return Map.of(
            "providers", providers.count(),
            "totalProviders", providers.count(),
            "services", services.count(),
            "bookings", requests.count(),
            "totalBookings", requests.count(),
            "totalUsers", users.count(),
            "totalRevenue", revenue,
            "pendingKyc", providers.findAll().stream().filter(x -> !Boolean.TRUE.equals(x.getIsVerified())).count()
        );
    }

    @GetMapping("/kyc/pending")
    public List<ServiceProvider> pendingKyc() {
        return providers.findAll().stream().filter(x -> !Boolean.TRUE.equals(x.getIsVerified())).toList();
    }

    @PutMapping("/kyc/{providerId}/verify")
    public ResponseEntity<?> verify(@PathVariable Long providerId) {
        ServiceProvider p = providers.findById(providerId).orElseThrow();
        p.setIsVerified(true);
        return ResponseEntity.ok(providers.save(p));
    }

    @PutMapping("/kyc/{providerId}/reject")
    public ResponseEntity<?> reject(@PathVariable Long providerId) {
        ServiceProvider p = providers.findById(providerId).orElseThrow();
        p.setIsVerified(false); p.setIsAvailable(false);
        return ResponseEntity.ok(providers.save(p));
    }
}
