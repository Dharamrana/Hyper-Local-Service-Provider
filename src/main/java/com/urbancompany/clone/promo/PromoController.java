package com.urbancompany.clone.promo;

import com.urbancompany.clone.model.Coupon;
import com.urbancompany.clone.repository.CouponRepository;
import com.urbancompany.clone.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/promo")
public class PromoController {
    private final PromoService promo;
    private final CouponRepository coupons;
    private final UserRepository users;

    public PromoController(PromoService promo, CouponRepository coupons, UserRepository users) {
        this.promo = promo; this.coupons = coupons; this.users = users;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody Map<String, Object> body, Authentication auth) {
        try {
            double items = Double.parseDouble(String.valueOf(body.get("itemsTotal")));
            boolean first = body.get("firstOrder") != null && Boolean.parseBoolean(String.valueOf(body.get("firstOrder")));
            Long uid = auth != null ? users.findByEmail(auth.getName()).map(u -> u.getId()).orElse(null) : null;
            var q = promo.validate(String.valueOf(body.get("code")), uid, items, first);
            return ResponseEntity.ok(Map.of("code", q.code() != null ? q.code() : "",
                    "discount", q.discount(), "payableItems", q.payableItems(), "message", q.message()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my-referral")
    public ResponseEntity<?> myReferral(Authentication auth) {
        var me = users.findByEmail(auth.getName()).orElseThrow();
        String code = promo.referralCodeFor(me.getId(), me.getName());
        String share = "Get ₹200 off your first home service in Premnagar! Use my code " + code
                + " on HLSP - Electrician, Plumber, Cleaning & more. Book now!";
        String wa = "https://wa.me/?text=" + java.net.URLEncoder.encode(share, java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok(Map.of("code", code, "shareText", share, "whatsappLink", wa,
                "creditPerReferral", 200));
    }

    // Admin helper (ROLE_ADMIN via /api/admin/** only; this stays authenticated for coupon listing hygiene)
    @GetMapping("/coupons")
    public ResponseEntity<?> list() { return ResponseEntity.ok(coupons.findAll()); }
}
