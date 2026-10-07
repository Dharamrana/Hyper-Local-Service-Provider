package com.urbancompany.clone.promo;

import com.urbancompany.clone.model.Coupon;
import com.urbancompany.clone.model.CouponRedemption;
import com.urbancompany.clone.model.Referral;
import com.urbancompany.clone.repository.CouponRedemptionRepository;
import com.urbancompany.clone.repository.CouponRepository;
import com.urbancompany.clone.repository.ReferralRepository;
import com.urbancompany.clone.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Coupon + referral engine.
 * Coupons discount the items total (visiting fee stays full - protects unit economics).
 * Referral: referred user gets ₹200 off first completed booking; referrer gets
 * ₹200 wallet-style credit recorded on the Referral row (spendable as a coupon
 * on their next booking via REF-CREDIT check in validate()).
 */
@Service
@Transactional
public class PromoService {
    private final CouponRepository coupons;
    private final CouponRedemptionRepository redemptions;
    private final ReferralRepository referrals;
    private final ServiceRequestRepository requests;

    public PromoService(CouponRepository coupons, CouponRedemptionRepository redemptions,
                        ReferralRepository referrals, ServiceRequestRepository requests) {
        this.coupons = coupons; this.redemptions = redemptions;
        this.referrals = referrals; this.requests = requests;
    }

    public record Quote(String code, double discount, double payableItems, String message) {}

    public Quote validate(String rawCode, Long userId, double itemsTotal, boolean isFirstOrder) {
        if (rawCode == null || rawCode.isBlank()) {
            return new Quote(null, 0, itemsTotal, "No coupon");
        }
        String code = rawCode.trim().toUpperCase(Locale.ROOT);
        Coupon c = coupons.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code"));
        if (!Boolean.TRUE.equals(c.getActive())) throw new IllegalArgumentException("Coupon is no longer active");
        LocalDate today = LocalDate.now();
        if (c.getValidFrom() != null && today.isBefore(c.getValidFrom())) throw new IllegalArgumentException("Coupon not started yet");
        if (c.getValidTill() != null && today.isAfter(c.getValidTill())) throw new IllegalArgumentException("Coupon expired");
        if (c.getMinOrder() != null && itemsTotal < c.getMinOrder()) {
            throw new IllegalArgumentException("Coupon needs a minimum order of ₹" + c.getMinOrder().intValue());
        }
        if (Boolean.TRUE.equals(c.getFirstOrderOnly()) && !isFirstOrder) {
            throw new IllegalArgumentException("Coupon is for first orders only");
        }
        if (c.getMaxRedemptions() != null && redemptions.countByCouponId(c.getId()) >= c.getMaxRedemptions()) {
            throw new IllegalArgumentException("Coupon fully redeemed");
        }
        if (userId != null && c.getMaxPerUser() != null
                && redemptions.countByCouponIdAndUserId(c.getId(), userId) >= c.getMaxPerUser()) {
            throw new IllegalArgumentException("You have already used this coupon");
        }
        double discount;
        if (c.getPercentOff() != null && c.getPercentOff() > 0) {
            discount = itemsTotal * c.getPercentOff() / 100.0;
            if (c.getMaxDiscount() != null) discount = Math.min(discount, c.getMaxDiscount());
        } else {
            discount = c.getFlatOff() != null ? c.getFlatOff() : 0;
        }
        discount = Math.min(round2(discount), itemsTotal); // never discount more than the cart
        return new Quote(code, discount, round2(itemsTotal - discount),
                "Coupon applied: -₹" + (int) discount);
    }

    /** Record redemption once the booking is created. Returns discount actually applied. */
    public double redeem(String code, Long userId, Long bookingId, double itemsTotal, boolean isFirstOrder) {
        Quote q = validate(code, userId, itemsTotal, isFirstOrder);
        if (q.code() == null || q.discount() <= 0) return 0;
        Coupon c = coupons.findByCodeIgnoreCase(q.code()).orElseThrow();
        redemptions.save(new CouponRedemption(null, c.getId(), userId, bookingId, q.discount(), LocalDateTime.now()));
        c.setRedeemedCount((c.getRedeemedCount() != null ? c.getRedeemedCount() : 0) + 1);
        coupons.save(c);
        return q.discount();
    }

    // ---------- Referral ----------

    public String referralCodeFor(Long userId, String name) {
        String base = (name != null ? name.replaceAll("[^A-Za-z]", "") : "HLSP").toUpperCase(Locale.ROOT);
        if (base.length() > 6) base = base.substring(0, 6);
        if (base.isBlank()) base = "HLSP";
        return base + userId + "IN"; // e.g. DHARAM42IN - readable on WhatsApp shares
    }

    /** Call at signup when a referral code arrives. Safe to call with blank code. */
    public Referral linkReferral(String code, Long referredUserId) {
        if (code == null || code.isBlank() || referredUserId == null) return null;
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        // Code encodes referrer id as trailing digits before IN; validate it parses and isn't self-referral.
        Long referrerId = parseReferrerId(normalized);
        if (referrerId == null || referrerId.equals(referredUserId)) return null;
        if (referrals.findByReferredUserId(referredUserId).isPresent()) {
            return referrals.findByReferredUserId(referredUserId).get();
        }
        return referrals.save(new Referral(null, referrerId, referredUserId, normalized,
                Referral.Status.PENDING, 200.0, 200.0, null, LocalDateTime.now(), null));
    }

    /** Call when the referred user's first booking completes. Credits both sides (recorded; referred discount applied at that booking). */
    public Referral creditOnFirstCompletion(Long referredUserId, Long bookingId) {
        return referrals.findByReferredUserId(referredUserId)
                .filter(r -> r.getStatus() == Referral.Status.PENDING)
                .map(r -> {
                    r.setStatus(Referral.Status.CREDITED);
                    r.setCreditedBookingId(bookingId);
                    r.setCreditedAt(LocalDateTime.now());
                    return referrals.save(r);
                }).orElse(null);
    }

    private Long parseReferrerId(String code) {
        try {
            String c = code.endsWith("IN") ? code.substring(0, code.length() - 2) : code;
            StringBuilder digits = new StringBuilder();
            for (int i = c.length() - 1; i >= 0 && Character.isDigit(c.charAt(i)); i--) digits.insert(0, c.charAt(i));
            return digits.length() == 0 ? null : Long.parseLong(digits.toString());
        } catch (Exception e) { return null; }
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
