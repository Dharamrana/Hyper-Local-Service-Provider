package com.urbancompany.clone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "phone_otps", indexes = @Index(name = "idx_phone_otp_phone", columnList = "phone"))
@Data @NoArgsConstructor @AllArgsConstructor
public class PhoneOtp {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String phone;        // normalized 10-digit
    @Column(nullable = false) private String otpHash;      // BCrypt/SHA, never plain in prod log beyond dev
    @Column(nullable = false) private LocalDateTime expiresAt;
    private Integer attempts = 0;
    private Boolean verified = false;
    private LocalDateTime createdAt;
}
