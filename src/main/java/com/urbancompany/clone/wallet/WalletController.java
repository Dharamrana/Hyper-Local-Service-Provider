package com.urbancompany.clone.wallet;

import com.urbancompany.clone.repository.ServiceProviderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/provider/wallet")
public class WalletController {
    private final WalletService wallet;
    private final ServiceProviderRepository providers;

    public WalletController(WalletService wallet, ServiceProviderRepository providers) {
        this.wallet = wallet; this.providers = providers;
    }

    private Long myProviderId(Authentication auth) {
        return providers.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Provider account not found")).getId();
    }

    @GetMapping
    public Map<String, Object> myWallet(Authentication auth) {
        Long pid = myProviderId(auth);
        return Map.of("providerId", pid, "balance", wallet.balance(pid),
                "commissionPercent", wallet.commissionPercent(),
                "statement", wallet.statement(pid));
    }

    @GetMapping("/balance")
    public Map<String, Object> balance(Authentication auth) {
        Long pid = myProviderId(auth);
        return Map.of("providerId", pid, "balance", wallet.balance(pid));
    }

    /** Admin/ops: settle + payout. Providers only see their own wallet above. */
    @PostMapping("/{providerId}/settle")
    public ResponseEntity<?> settle(@PathVariable Long providerId) {
        double amt = wallet.settlePending(providerId);
        return ResponseEntity.ok(Map.of("providerId", providerId, "settled", amt, "balance", wallet.balance(providerId)));
    }

    @PostMapping("/{providerId}/payout")
    public ResponseEntity<?> payout(@PathVariable Long providerId, @RequestBody Map<String, Object> body) {
        double amount = Double.parseDouble(String.valueOf(body.get("amount")));
        String ref = body.get("payoutRef") != null ? String.valueOf(body.get("payoutRef")) : null;
        String note = body.get("note") != null ? String.valueOf(body.get("note")) : "Weekly payout";
        var tx = wallet.payout(providerId, amount, ref, note);
        return ResponseEntity.ok(tx);
    }
}
