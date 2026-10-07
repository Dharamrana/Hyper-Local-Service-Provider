package com.urbancompany.clone.repository;

import com.urbancompany.clone.model.ProviderAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProviderAvailabilityRepository extends JpaRepository<ProviderAvailability, Long> {
    List<ProviderAvailability> findByProviderIdAndDate(Long providerId, LocalDate date);
    Optional<ProviderAvailability> findByProviderIdAndDateAndSlot(Long providerId, LocalDate date, String slot);
    void deleteByProviderIdAndDateAndSlot(Long providerId, LocalDate date, String slot);
}
