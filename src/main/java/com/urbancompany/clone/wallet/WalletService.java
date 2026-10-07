package com.urbancompany.clone.wallet;

import com.urbancompany.clone.model.ServiceRequest;
import com.urbancompany.clone.model.WalletTransaction;
import com.urbancompany.clone.repository.WalletTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Provider wallet + payout split ledger.
 * On job completion: customer pays finalPrice -> platform keeps commission% ->
 * provider credited the rest. Every movement is an immutable ledger row, so
 * monthly statements / disputes are just queries. Payouts settle T+1 by default.
 */
@Service
@Transactional
public class WalletService {
    private static final Logger log = LoggerFactory.getLogger(WalletService.class);
    private final WalletTransactionRepository repo;

    /** Platform commission percent (UC-style 20%). Override per service later if needed. */
    @Value("${app.wallet.commission-percent:20}")
    private double commissionPercent;

    /** Visiting fee goes to platform (covers travel/support), not split. */
    @Value("${app.wallet.visiting-fee-to-platform:true}")
    private boolean visitingFeeToPlatform;

    public WalletService(WalletTransactionRepository repo) { this.repo = repo; }

    /**
     * Called when a booking completes + payment is PAID/CASH collected.
     * Idempotent per booking: re-completing does not double-credit.
     */
    public List<WalletTransaction> creditOnCompletion(ServiceRequest booking) {
        if (booking.getProvider() == null || booking.getFinalPrice() == null) return List.of();
        if (!repo.findByBookingId(booking.getId()).isEmpty()) {
            log.info("[WALLET] booking {} already credited, skipping", booking.getId());
            return repo.findByBookingId(booking.getId());
        }
        double visiting = booking.getVisitingFee() != null ? booking.getVisitingFee() : 0;
        double jobAmount = Math.max(0, booking.getFinalPrice() - (visitingFeeToPlatform ? visiting : 0));
        double fee = round2(jobAmount * commissionPercent / 100.0);
        double providerShare = round2(jobAmount - fee);
        Long pid = booking.getProvider().getId();
        double bal = balance(pid);

        WalletTransaction credit = save(pid, booking.getId(), WalletTransaction.Type.CREDIT_JOB,
                providerShare, bal + providerShare,
                "Job #" + booking.getId() + " credit (₹" + jobAmount + " - " + commissionPercent + "% platform)");
        WalletTransaction feeRow = save(pid, booking.getId(), WalletTransaction.Type.PLATFORM_FEE,
                0.0, bal + providerShare, // informational; provider balance already net
                "Platform fee ₹" + fee + " on job #" + booking.getId());
        log.info("[WALLET] booking {} provider {} credited {} fee {} balance {}",
                booking.getId(), pid, providerShare, fee, bal + providerShare);
        return List.of(credit, feeRow);
    }

    /** Reverse credit if a paid booking is cancelled/refunded after completion edge cases. */
    public void reverseOnRefund(ServiceRequest booking) {
        if (booking.getProvider() == null) return;
        var rows = repo.findByBookingId(booking.getId()).stream()
                .filter(r -> r.getType() == WalletTransaction.Type.CREDIT_JOB
                        && r.getStatus() != WalletTransaction.Status.REVERSED).toList();
        Long pid = booking.getProvider().getId();
        for (var r : rows) {
            double bal = balance(pid);
            save(pid, booking.getId(), WalletTransaction.Type.REFUND_REVERSAL,
                    -r.getAmount(), bal - r.getAmount(), "Reversal for refunded job #" + booking.getId());
            r.setStatus(WalletTransaction.Status.REVERSED);
            repo.save(r);
        }
    }

    public double balance(Long providerId) {
        return repo.findByProviderIdOrderByCreatedAtDesc(providerId).stream()
                .filter(r -> r.getStatus() != WalletTransaction.Status.REVERSED)
                .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0)
                .sum();
    }

    public List<WalletTransaction> statement(Long providerId) {
        return repo.findByProviderIdOrderByCreatedAtDesc(providerId);
    }

    /** Mark all PENDING credits SETTLED (runs nightly / on payout day). Returns amount ready to pay out. */
    public double settlePending(Long providerId) {
        var pending = repo.findByProviderIdAndStatusOrderByCreatedAtDesc(providerId, WalletTransaction.Status.PENDING)
                .stream().filter(r -> r.getType() == WalletTransaction.Type.CREDIT_JOB).toList();
        double total = 0;
        for (var r : pending) { r.setStatus(WalletTransaction.Status.SETTLED); r.setSettledAt(LocalDateTime.now()); repo.save(r); total += r.getAmount(); }
        return round2(total);
    }

    /** Record a payout (RazorpayX / manual bank). Creates a negative ledger row. */
    public WalletTransaction payout(Long providerId, double amount, String payoutRef, String note) {
        if (amount <= 0) throw new IllegalArgumentException("Payout amount must be positive");
        double bal = balance(providerId);
        if (amount > bal) throw new IllegalStateException("Payout exceeds wallet balance ₹" + bal);
        return save(providerId, null, WalletTransaction.Type.PAYOUT, -amount, bal - amount,
                (note != null ? note : "Payout") + (payoutRef != null ? " ref " + payoutRef : ""), payoutRef);
    }

    private WalletTransaction save(Long pid, Long bookingId, WalletTransaction.Type type,
                                   double amount, double balanceAfter, String note) {
        return save(pid, bookingId, type, amount, balanceAfter, note, null);
    }
    private WalletTransaction save(Long pid, Long bookingId, WalletTransaction.Type type,
                                   double amount, double balanceAfter, String note, String payoutRef) {
        WalletTransaction tx = new WalletTransaction(null, pid, bookingId, type,
                WalletTransaction.Status.PENDING, round2(amount), round2(balanceAfter),
                note, payoutRef, LocalDateTime.now(), null);
        if (type == WalletTransaction.Type.PAYOUT) { tx.setStatus(WalletTransaction.Status.PAID_OUT); tx.setSettledAt(LocalDateTime.now()); }
        return repo.save(tx);
    }
    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    public double commissionPercent() { return commissionPercent; }
}
