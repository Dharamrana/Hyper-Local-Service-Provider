package com.urbancompany.clone.repository;

import com.urbancompany.clone.model.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReferralRepository extends JpaRepository<Referral, Long> {
    Optional<Referral> findByReferralCode(String referralCode);
    List<Referral> findByReferrerId(Long referrerId);
    Optional<Referral> findByReferredUserId(Long referredUserId);
}
