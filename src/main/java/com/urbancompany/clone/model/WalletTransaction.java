package com.urbancompany.clone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wallet_provider", columnList = "providerId"),
        @Index(name = "idx_wallet_booking", columnList = "bookingId")})
@Data @NoArgsConstructor @AllArgsConstructor
public class WalletTransaction {
    public enum Type { CREDIT_JOB, PLATFORM_FEE, PAYOUT, PAYOUT_FEE, REFUND_REVERSAL, BONUS, ADJUSTMENT }
    public enum Status { PENDING, SETTLED, PAID_OUT, REVERSED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long providerId;
    private Long bookingId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Type type;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status = Status.PENDING;
    /** Signed rupees: + credit to provider, - debit. */
    @Column(nullable = false) private Double amount;
    private Double balanceAfter;
    private String note;        // e.g. "Job #42 Electrician - platform 20%"
    private String payoutRef;  // RazorpayX / bank UTR once paid
    private LocalDateTime createdAt;
    private LocalDateTime settledAt;
}
