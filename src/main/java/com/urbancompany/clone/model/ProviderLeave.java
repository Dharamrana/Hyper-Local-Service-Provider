package com.urbancompany.clone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "provider_leaves", indexes = @Index(name = "idx_leave_provider", columnList = "providerId"))
@Data @NoArgsConstructor @AllArgsConstructor
public class ProviderLeave {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long providerId;
    @Column(nullable = false) private LocalDate fromDate;
    @Column(nullable = false) private LocalDate toDate;
    private String reason;
}
