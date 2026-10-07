package com.urbancompany.clone.repository;

import com.urbancompany.clone.model.PhoneOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PhoneOtpRepository extends JpaRepository<PhoneOtp, Long> {
    Optional<PhoneOtp> findTopByPhoneOrderByCreatedAtDesc(String phone);
}
