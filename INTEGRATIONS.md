# HLSP — Market Integrations Roadmap

Repo: **Hyper-Local-Service-Provider** (Spring Boot 3.2.5, Java 17, Thymeleaf, MySQL/H2, Redis)

## Implemented in this push (scaffold, production-ready call sites)
1. **Payments — Razorpay** `payment/PaymentService.java` + `PaymentWebhookController`
   - Mock mode when `RAZORPAY_KEY_ID` blank (demo/viva), live orders when keys set.
   - Webhook `/api/payments/webhook` with HMAC-SHA256 verify, invoice breakup = items + ₹49 visiting fee (GST-ready).
2. **Notifications — WhatsApp-first** `notification/NotificationService.java`
   - Booking confirm + provider new-job alerts, `wa.me` deep link fallback, log-only without token.
   - Call sites ready for Meta Cloud API / MSG91 — fill `WHATSAPP_TOKEN` in `.env`.
3. **Address + Maps** `model/Location.java` (houseNo, landmark, pincode, city, label) + `static/js/maps.js`
   - OSM/Nominatim reverse geocode (free), GPS detect, Premnagar default (30.3429, 77.9620).
   - Google Maps upgrades automatically when `GOOGLE_MAPS_API_KEY` is set.
4. **Admin + KYC** `admin/AdminController.java`
   - `/api/admin/stats`, `/api/admin/kyc/pending`, verify/reject. Protected by `ROLE_ADMIN`.
   - Provider flow: register → unverified → admin verifies → visible in search.
5. **Prod hardening** `.env.example`, secrets via env only, Actuator health, Premnagar seed providers (Vikram Rawat, Sunita Devi).

## Next (one-by-one, in order)
- [x] Phone OTP login (MSG91) + forgot-password token flow — `auth/OtpService`, `PhoneAuthController`, 5-min TTL, 5 attempts, 30s cooldown, hashed OTP, dev log mode
- [x] Real payout split + provider wallet ledger — `wallet/WalletService` 80/20 split on completion, immutable `wallet_transactions`, settle + payout APIs (`/api/provider/wallet`). Monthly PDF statement is next micro-step
- [x] Provider availability calendar — `availability/` slot overrides, leaves, capacity, booking gate; public slots API for wizard. (Job-pool accept timeout is next micro-step)
- [x] Coupon engine + referral — `promo/PromoService` validate/redeem with caps, CouponRedemption audit, Referral PENDING→CREDITED on first completion, WhatsApp share link (`/api/promo/my-referral`)
- [x] PWA wrapper — `manifest.webmanifest`, `sw.js` (network-first pages, cache-first static, offline page), install prompt + iOS hint, icons, shortcuts
- [x] Hindi/Hinglish i18n — Spring MessageSource (`i18n/messages*.properties`, cookie `hlsp-lang`, `?lang=hi`), instant EN/Hindi/Hinglish toggle (`js/i18n.js`), Hinglish WhatsApp copy. Voice search remains a micro-step (Web Speech API)

## Run
```bash
cp .env.example .env   # fill keys as needed, blank = mock/demo mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev   # H2 + seed data
# open http://localhost:8080
```
