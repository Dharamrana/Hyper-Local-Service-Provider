package com.urbancompany.clone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One row = one provider, one date, one slot.
 * No row -> fall back to weekly default (provider isAvailable + not on leave).
 * Row with available=false -> blocked (leave / day off / slot off).
 */
@Entity
@Table(name = "provider_availability", uniqueConstraints =
        @UniqueConstraint(columnNames = {"providerId", "date", "slot"}),
        indexes = @Index(name = "idx_avail_provider_date", columnList = "providerId,date"))
@Data @NoArgsConstructor @AllArgsConstructor
public class ProviderAvailability {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long providerId;
    @Column(nullable = false) private LocalDate date;
    @Column(nullable = false) private String slot;       // "10:00-12:00" from DAILY_SLOTS
    @Column(nullable = false) private Boolean available = true;
    private Integer capacity = 1;                       // max concurrent jobs in this slot
    private String note;                                // "Leave - family function"
}
