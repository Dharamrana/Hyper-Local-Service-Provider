package com.urbancompany.clone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity @Table(name = "coupons") @Data @NoArgsConstructor @AllArgsConstructor
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false) private String code; // FIRST100, PREMNAGAR50
    private String description;
    private Double percentOff;      // e.g. 20 = 20%
    private Double flatOff;         // e.g. 100 = ₹100 off (used when percentOff is null/0)
    private Double maxDiscount;     // cap for percent coupons
    private Double minOrder;        // minimum items total (before visiting fee)
    private LocalDate validFrom;
    private LocalDate validTill;
    private Boolean active = true;
    private Integer maxRedemptions;      // null = unlimited
    private Integer maxPerUser = 1;
    private Integer redeemedCount = 0;
    private Boolean firstOrderOnly = false;
}
