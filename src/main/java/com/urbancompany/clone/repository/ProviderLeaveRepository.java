package com.urbancompany.clone.repository;

import com.urbancompany.clone.model.ProviderLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ProviderLeaveRepository extends JpaRepository<ProviderLeave, Long> {
    List<ProviderLeave> findByProviderId(Long providerId);
    List<ProviderLeave> findByProviderIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long providerId, LocalDate date, LocalDate date2);
}
