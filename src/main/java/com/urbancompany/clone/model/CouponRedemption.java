package com.urbancompany.clone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_redemptions", indexes = @Index(name = "idx_redemption_user", columnList = "userId"))
@Data @NoArgsConstructor @AllArgsConstructor
public class CouponRedemption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long couponId;
    @Column(nullable = false) private Long userId;
    private Long bookingId;
    @Column(nullable = false) private Double discount;
    private LocalDateTime redeemedAt;
}
