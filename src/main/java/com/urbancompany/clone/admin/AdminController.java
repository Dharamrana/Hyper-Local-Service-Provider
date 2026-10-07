package com.urbancompany.clone.admin;

import com.urbancompany.clone.model.ServiceProvider;
import com.urbancompany.clone.repository.ServiceProviderRepository;
import com.urbancompany.clone.repository.ServiceRepository;
import com.urbancompany.clone.repository.ServiceRequestRepository;
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

    public AdminController(ServiceProviderRepository p, ServiceRepository s, ServiceRequestRepository r) {
        this.providers = p; this.services = s; this.requests = r;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
            "providers", providers.count(),
            "services", services.count(),
            "bookings", requests.count(),
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
