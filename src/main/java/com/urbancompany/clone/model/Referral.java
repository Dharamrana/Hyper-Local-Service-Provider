package com.urbancompany.clone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "referrals", indexes = {
        @Index(name = "idx_referral_code", columnList = "referralCode"),
        @Index(name = "idx_referral_referrer", columnList = "referrerId")})
@Data @NoArgsConstructor @AllArgsConstructor
public class Referral {
    public enum Status { PENDING, CREDITED, EXPIRED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long referrerId;       // who shared
    private Long referredUserId;                             // who signed up / first booked
    @Column(nullable = false) private String referralCode;   // e.g. DHARAM200
    @Enumerated(EnumType.STRING) private Status status = Status.PENDING;
    private Double referrerCredit = 200.0;
    private Double referredCredit = 200.0;
    private Long creditedBookingId;                          // first completed booking that triggered credit
    private LocalDateTime createdAt;
    private LocalDateTime creditedAt;
}
