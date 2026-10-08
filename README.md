# Hyper-Local Service Provider (HLSP)

**Hyperlocal service booking built for Tier‑3 India — launch ground: Prem Nagar, Suddowala & Kheri Gaon, Dehradun.**

Book verified Electricians, Plumbers, Carpenters, Cleaners, AC Technicians & more — right from your neighbourhood. Transparent pricing, OTP‑started jobs, WhatsApp‑first updates, and pros who actually show up.

> Urban Company proved the model in metros. HLSP takes it where the metros forgot: **Tier‑3 cities and growing towns**, where your electrician is still found through "someone who knows someone."

![Stack](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen) ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-SSR-blue) ![MySQL](https://img.shields.io/badge/MySQL-8-blue) ![Redis](https://img.shields.io/badge/Redis-cache-red) ![Razorpay](https://img.shields.io/badge/Payments-Razorpay-0f52ff)

> **Live demo:** https://hyper-local-service-provider.onrender.com — free tier, first load after idle can take ~30s to wake. Runs the `dev` profile (H2 seed data, mock payments), so no real keys are needed to try the full booking flow.

---

## Table of Contents
- [Motivation](#motivation)
- [The Problem](#the-problem)
- [Our Solution](#our-solution)
- [How It's Different (Tier‑3 Edge)](#how-its-different-tier3-edge)
- [Features](#features)
- [The Three Roles](#the-three-roles)
- [System Architecture](#system-architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration (`.env`)](#configuration-env)
- [Core Flows](#core-flows)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Seed Data (Dehradun)](#seed-data-dehradun)
- [Deployment](#deployment)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Motivation

In Prem Nagar or Suddowala, when your fan stops working in May, your options today are:

1. Call the *one* electrician whose number is saved on someone's phone — and hope he picks up.
2. Ask the hardware shop to "send someone" — no price, no time, no guarantee.
3. Wait. Sometimes for days.

Meanwhile, a skilled carpenter two lanes away, who could fix your door in 30 minutes, sits idle because nobody outside his mohalla knows he exists.

**That gap — between real local demand and real local skill — is pure friction.** National platforms (Urban Company, etc.) optimised for metro density: high order volumes, English‑first UI, card‑first payments, and provider onboarding built for big cities. Tier‑3 India runs differently:

- **Cash & UPI first**, cards second
- **WhatsApp is the internet** — calls and chats, not emails
- **Trust is personal** — "is he from our area?" matters more than a glossy profile
- **Hindi / Hinglish**, not English‑only
- **Patchy connectivity** — the app must work light and offline‑tolerant

HLSP exists to close that gap: **a marketplace designed from the ground up for how Tier‑3 India actually books services.**

## The Problem

| # | Problem | Who feels it |
|---|---------|--------------|
| 1 | **Discovery is word‑of‑mouth only.** No way to find a plumber/carpenter near you with ratings, prices, or availability. | Customers |
| 2 | **No price transparency.** "Dekh ke batate hain" (we'll see and tell you) leads to haggling and disputes after the job is done. | Both sides |
| 3 | **No accountability.** If the pro doesn't show up, or does a bad job, there's no record, no review, no recourse. | Customers |
| 4 | **Pros are invisible & underpaid.** Skilled local workers depend on middlemen and shopkeepers who take informal cuts; no steady pipeline of jobs. | Providers |
| 5 | **No scheduling or trust system.** No bookings, no slots, no proof the job started/finished — everything runs on phone calls and promises. | Both sides |
| 6 | **Metro platforms don't fit.** English‑only, card‑first, metro‑only serviceability and onboarding leave Tier‑3 towns unserved. | The whole market |

## Our Solution

HLSP is a complete **role‑based marketplace** (Customer · Service Provider · Admin) that brings the Urban Company playbook to Tier‑3 ground, rebuilt for local realities:

1. **Browse & book in 4 steps** — pick services → choose slot → *Select on Map (Rapido‑style)* to pin your exact location → review price & pay. No phone tag.
2. **Verified neighbourhood pros** — every provider is KYC‑reviewed by an admin before going live, carries ratings/certifications, and serves a defined coverage area (Prem Nagar, Suddowala, Kheri Gaon…).
3. **Transparent money** — fixed catalogue prices + a flat ₹49 visiting fee, coupon/referral discounts shown upfront, and an itemised breakup before you pay. Razorpay (UPI/Card) or cash after service.
4. **Trust through OTP** — the job starts only when *you* share the 4‑digit OTP with the provider standing at your door. No OTP, no "completed" billing.
5. **WhatsApp‑first communication** — booking confirmations and job alerts on WhatsApp, where Tier‑3 users already live.
6. **Providers earn properly** — an 80/20 wallet ledger credits the pro automatically on completion (platform keeps 20%), with earnings history, availability calendar, and payout requests.
7. **Runs in your language** — English / हिंदी / Hinglish toggle, installable PWA that works on low‑end phones and patchy networks.

### How It's Different (Tier‑3 Edge)

| Metro playbook | HLSP's Tier‑3 play |
|---|---|
| English‑only UI | English / Hindi / Hinglish, one tap |
| Card‑first payments | UPI + cash‑after‑service + Razorpay |
| Email notifications | WhatsApp‑first (Meta/MSG91 + `wa.me` fallback) |
| Metro serviceability only | Neighbourhood‑level coverage maps (Prem Nagar, Suddowala, Kheri Gaon, Ballupur…) |
| Heavy native apps | Lightweight installable **PWA**, offline page, demo mode with zero keys |
| Opaque onboarding | Admin KYC queue with approve/reject + reasons — trust stays human and local |

## Features

### Customer
- 8‑service catalogue with photos, search & filters (self‑healing seed — catalogue can never render empty)
- Multi‑service cart with a single ₹49 visiting fee
- **Rapido‑style Select on Map**: fixed centre pin, drag the Dehradun map underneath, live *Selected: Suddowala · 30.33500, 77.95500*, locality search/chips, GPS, reverse‑geocoded address
- Provider selection via interactive map with circular photo markers, or auto‑assign nearest
- Slot booking (08:00–20:00, 2‑hour windows) with provider availability checks
- Coupons (`FIRST100`, `PREMNAGAR50`, `HLSP10`) & ₹200‑both‑sides referral programme
- Mock UPI/Card payment with refund‑on‑cancel; cash after service
- 4‑digit OTP job start, reschedule/cancel, star ratings that roll into provider averages
- Phone‑OTP login + forgot‑password flow (hashed OTP, 5‑min TTL)

### Service Provider
- Dedicated Partner Portal: open job pool, OTP job start, job management
- Revenue dashboard mindset: total/monthly earnings, completed vs pending bookings, transaction history
- **80/20 wallet ledger** — immutable `wallet_transactions`, credited on completion, settle & payout (RazorpayX‑ready)
- Availability calendar: per‑day slot overrides, capacity, leave ranges — booking engine respects them
- Service‑area selection on map + coverage localities
- KYC submission & verification status

### Admin
- Platform analytics: total users, providers, bookings, revenue, pending KYC
- KYC verification queue: view documents/details, approve / reject with reason
- Provider management: search, filter, sort, status controls (approve / suspend / activate / deactivate / remove)
- Provider detail view: profile, contact, KYC, services, ratings/reviews, booking stats, individual revenue
- Booking, customer, revenue & platform‑settings sections

### Platform
- Razorpay orders + HMAC‑SHA256 webhook verification (mock mode when keys are blank — viva/demo safe)
- Redis caching (services, providers, nearby search) with sensible TTLs
- Spring Security role‑based route protection (`CUSTOMER` / `PROVIDER` / `ADMIN`)
- PWA: installable, offline fallback page, service‑worker caching (APIs never cached)
- Secrets only via environment variables — zero secrets in code

## The Three Roles

| Capability | Customer | Provider | Admin |
|---|:---:|:---:|:---:|
| Browse services & providers | ✅ | ✅ | ✅ |
| Book with Select‑on‑Map | ✅ | — | — |
| Cart, coupons, referral | ✅ | — | — |
| Job pool & OTP start | — | ✅ | — |
| Wallet, earnings, payouts | — | ✅ | — |
| Availability & service area | — | ✅ | — |
| KYC submit | — | ✅ | — |
| KYC approve/reject | — | — | ✅ |
| Platform analytics & controls | — | — | ✅ |

## System Architecture

```
┌──────────────────────────── PWA (Thymeleaf + JS) ───────────────────────────┐
│  Customer booking wizard · Partner Portal · Admin dashboard · Maps (OSM)  │
└───────────────┬───────────────────────────────────────────┬─────────────────┘
                │ REST + SSR                                │ WhatsApp / wa.me
┌───────────────▼─────────────────────────────┐   ┌─────────▼─────────┐
│ Spring Boot 3.2.5 (Java 17)                 │   │ Razorpay orders   │
│  Controllers · Security (roles) · Services  ├──►│ + webhook HMAC    │
│  Razorpay · Wallet 80/20 · Availability     │   └───────────────────┘
│  Coupons/Referral · OTP auth · i18n · Admin │
└──────┬──────────────────┬───────────────────┘
       │ JPA              │ Cache
┌──────▼──────┐    ┌──────▼──────┐
│ MySQL 8     │    │ Redis       │
│ (H2 in dev) │    │ TTL caches  │
└─────────────┘    └─────────────┘
```

**Geolocation:** Haversine distance (`ServiceProviderService`) ranks nearest eligible pros from the customer's pinned location; provider availability + leave calendar gates bookable slots.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2.5, Spring Security, Spring Data JPA, Validation |
| Frontend | Thymeleaf SSR, DM Sans, Font Awesome, Leaflet + OpenStreetMap (Nominatim), vanilla JS |
| Database | MySQL 8 (prod) · H2 (dev/test profile) |
| Cache | Redis (per‑cache TTL) |
| Payments | Razorpay Java SDK 1.4.6 (mock mode without keys) |
| Notifications | WhatsApp‑first service (Meta Cloud API / MSG91 ready, `wa.me` fallback, log‑only in demo) |
| PWA | Web App Manifest, Service Worker, offline page |
| i18n | Spring MessageSource — English, Hindi (Devanagari), Hinglish |
| Build/Deploy | Maven, Docker, `render.yaml` (Render free tier) |

## Project Structure

```
src/main/java/com/urbancompany/clone/
├── admin/          Admin stats + KYC verify/reject APIs
├── auth/           Phone OTP login + password-reset tokens
├── availability/   Provider calendar, slot overrides, leaves
├── config/         Security, Redis cache, DataInitializer (Dehradun seed)
├── controller/     Web pages + REST (services, providers, requests, users)
├── i18n/           Locale config (hlsp-lang cookie)
├── model/          User, Service, ServiceProvider, ServiceRequest(+Item),
│                   Location, WalletTransaction, Coupon(+Redemption),
│                   Referral, ProviderAvailability/Leave, PhoneOtp, …
├── notification/   WhatsApp-first NotificationService
├── payment/        Razorpay PaymentService + webhook controller
├── promo/          Coupon validation/redemption + referral engine
├── repository/     Spring Data JPA repositories
├── service/        Business logic (Haversine matching, bookings, users)
└── wallet/         80/20 provider wallet ledger + payout APIs

src/main/resources/
├── templates/      index, services, providers, request-service (booking
│                   wizard), provider-portal, requests, login/signup, pay…
├── static/js/      booking.js, maps.js (Rapido picker), cart.js, pwa.js, i18n.js
└── static/         sw.js, manifest.webmanifest, css/style.css, icons/
```

## Getting Started

### Prerequisites
- Java 17+, Maven 3.9+
- MySQL 8 (production) — *not needed for dev profile*
- Redis (optional in dev; caching degrades gracefully)

### Run (dev — zero config)
```bash
git clone https://github.com/Dharamrana/Hyper-Local-Service-Provider.git
cd Hyper-Local-Service-Provider
cp .env.example .env        # blank keys = demo/mock mode, everything still runs
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# → http://localhost:8080  (H2 in-memory + Dehradun seed data)
```

### Run (production)
```bash
# Create the database first:  CREATE DATABASE hlsp_db;
mvn spring-boot:run   # uses .env: MySQL + Redis + live Razorpay/WhatsApp keys
```

### Docker
```bash
docker compose up --build
```

## Configuration (`.env`)

Copy `.env.example` and fill what you have — **every integration degrades to a safe mock when its keys are blank**, so the full flow always demos:

| Variable | Purpose | Blank = |
|---|---|---|
| `SPRING_DATASOURCE_*` | MySQL connection | H2 (dev profile) |
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | Live payments | Mock UPI/Card flow |
| `RAZORPAY_WEBHOOK_SECRET` | Webhook HMAC verify | Mock webhooks accepted locally |
| `WHATSAPP_TOKEN`, `MSG91_API_KEY` | WhatsApp/SMS sending | Log‑only + `wa.me` links |
| `GOOGLE_MAPS_API_KEY` | Google Maps upgrade | OpenStreetMap/Nominatim (free) |
| `DEFAULT_LAT` / `DEFAULT_LNG` | Map centre | Prem Nagar `30.3429, 77.9620` |
| `WALLET_COMMISSION_PERCENT` | Platform cut | `20` (provider gets 80%) |
| `OTP_DEV_EXPOSE` | Return OTP in API for local testing | `false` (never in prod) |

## Core Flows

**Booking (Customer)**
1. Add services to cart → 2. Pick date/slot (availability‑checked) → 3. **Select on Map**: drag map under the fixed pin, confirm *Selected: <locality> · lat,lng* → 4. Nearest pros ranked by Haversine distance; pick one on the photo‑marker map or auto‑assign → 5. Price breakup (items + ₹49 visiting fee − coupon) → 6. Pay (Razorpay/cash) → 7. Share the 4‑digit OTP with the pro at your door to start → 8. Rate the job.

**Provider**
Register → submit KYC → admin approves → set availability + service area → accept jobs from the pool → enter customer OTP to start → complete → **80% auto‑credited** to wallet ledger → request payout.

**Admin**
Sign in (ADMIN role) → review KYC queue (approve / reject with reason) → monitor platform analytics → manage providers (suspend/activate/remove) → track bookings & revenue.

## API Reference

*Full endpoint tables live in [AGENTS.md](AGENTS.md). Highlights:*

| Area | Endpoints |
|---|---|
| Services | `GET /api/services`, `/search?name=`, CRUD |
| Providers | `GET /api/providers/nearest?serviceId&lat&lng`, `/nearby`, detail |
| Bookings | `POST /api/requests`, assign, status, complete, cancel, reschedule |
| Payments | `POST /api/payments/webhook` (HMAC‑SHA256) |
| Auth | `POST /api/auth/phone/send-otp`, `/phone/verify`, `/forgot-password`, `/reset-password` |
| Wallet | `GET /api/provider/wallet`, `/balance`, `POST /settle`, `/payout` |
| Availability | `GET /api/provider/availability/public/{id}?date=`, provider day/slot/leave APIs |
| Promo | `POST /api/promo/validate`, `GET /api/promo/my-referral` |
| Admin | `GET /api/admin/stats`, `/api/admin/kyc/pending`, verify/reject (`ROLE_ADMIN`) |

## Database Schema

Core tables: `users`, `services`, `service_providers` (+ `provider_services`, certifications), `service_requests` (+ `service_request_items`), `locations`, `wallet_transactions` (immutable ledger), `coupons` + `coupon_redemptions`, `referrals`, `provider_availability` + `provider_leaves`, `phone_otps`, `password_reset_tokens`.

Example money flow: booking ₹499 + ₹49 visiting fee → on completion, provider wallet is credited **₹399.20** (80% of service price; visiting fee stays with the platform) — recorded as an immutable ledger entry.

## Seed Data (Dehradun)

8 services — Carpenter ₹499 · Electrician ₹399 · Plumber ₹349 · Massage Therapist ₹799 · House Cleaning ₹499 · AC Repair ₹699 · Appliance Repair ₹599 · Painter ₹449 — served by local pros across **Prem Nagar (30.3429, 77.9620), Suddowala, Kheri Gaon, Ballupur & Kaulagarh**, with ratings, certifications and availability. Starter coupons: `FIRST100` (₹100 off first order ₹499+), `PREMNAGAR50` (20% up to ₹150), `HLSP10` (10% up to ₹100).

## Deployment

Ships with `render.yaml` (Docker, free tier, health check) and a `Dockerfile`. Set env vars in the Render dashboard; blank keys keep demo mode. Bind port via `${PORT:8080}`.

**Manual one‑time step:** PWA PNG icons (`icon-192.png`, `icon-512.png`) must be uploaded via GitHub web *Add file → Upload* from `src/main/resources/static/icons/` — the API corrupts binaries; the bundled SVG icon carries installability until then.

## Roadmap

- [x] Razorpay payments + webhook, WhatsApp‑first notifications
- [x] Admin + KYC verification, role‑based dashboards
- [x] Phone OTP + forgot‑password, provider wallet (80/20) + payouts
- [x] Availability calendar, coupons + ₹200 referral
- [x] PWA (installable, offline), Hindi/Hinglish UI
- [x] Dehradun launch areas + Rapido‑style Select on Map
- [ ] Monthly PDF wallet statements · voice search (Web Speech API)
- [ ] Masked chat/call between customer & provider
- [ ] Recurring bookings, photo reviews, multi‑city expansion (next: other Tier‑3 towns of Uttarakhand)

*Detailed integration notes: [INTEGRATIONS.md](INTEGRATIONS.md) · Dev guide: [AGENTS.md](AGENTS.md)*

## Contributing

Fork, branch, PR. Keep secrets in `.env` (never commit real keys), keep the catalogue seed accurate, and run `mvn test` before pushing. See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT — see [LICENSE](LICENSE).

---

**Presented by Dharam Rana** · Dept. of Computer Science & Engineering · Built with ♥ for the neighbourhoods metros forgot.
