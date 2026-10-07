package com.urbancompany.clone.repository;

import com.urbancompany.clone.model.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {
    List<CouponRedemption> findByUserId(Long userId);
    long countByCouponIdAndUserId(Long couponId, Long userId);
    long countByCouponId(Long couponId);
}
