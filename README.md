# Hyper-Local-Service-Provider — HLSP

Hyperlocal service booking for Tier-3 cities (launch: **Premnagar & Suddowala, Dehradun**). Urban Company-style flow, built for low-trust, WhatsApp-first, cash/UPI India.

Book Electrician, Plumber, Carpenter, Cleaning, AC Repair & more — verified local pros, OTP start, transparent pricing.

## Features
- **Catalog & Discovery:** 8 services, nearest-pro matching (Haversine), ratings/certs
- **Booking:** Multi-service cart, ₹49 visiting fee, slot picker, pick-a-pro or auto-assign
- **Trust:** 4-digit OTP to start job, verified KYC pros, reviews
- **Payments:** Razorpay (mock mode without keys), UPI/Card/Cash, refund on cancel
- **Notifications:** WhatsApp-first alerts (Meta/MSG91 ready), `wa.me` fallback
- **Provider Portal:** Job pool, OTP start, earnings
- **Admin:** `/api/admin` stats + KYC verify/reject
- **Maps:** Structured address (house/landmark/pincode), OSM geocode, Premnagar default

## Tech Stack
Java 17 · Spring Boot 3.2.5 · Thymeleaf · MySQL/H2 · Redis · Spring Security · Maven · Docker

## Run
```bash
cp .env.example .env   # blank keys = demo/mock mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# http://localhost:8080  (H2 + seed: Delhi + Premnagar pros)
```

## Integrations
See [INTEGRATIONS.md](INTEGRATIONS.md). Keys in `.env.example`: Razorpay, WhatsApp, MSG91, Google Maps.

## License
MIT
