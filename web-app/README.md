# UrbanService Web App (React + TypeScript)

This folder is the hosted web-app version of UrbanService — the same app shown in the Muse artifact: complete role-based marketplace (Customer / Service Provider / Admin), Rapido-style Select on Map for Dehradun (Prem Nagar, Sudhowala, Kheri Gaon), 8 services, provider revenue analytics, admin KYC management.

The Java/Spring Boot version of the same product lives at the repo root; this `web-app/` is the TypeScript port and both coexist without conflict.

## Stack
React 19 + TypeScript, Bun, Drizzle ORM (SQLite), Tailwind CSS 4, Recharts. Server actions in `server/src/actions.ts`, schema in `server/src/schema.ts`, migrations in `drizzle/`.

## Structure
- `client/src/App.tsx` — the full app (roles, booking wizard, dashboards)
- `client/src/components/ProviderMap.tsx` — circular photo-marker map
- `client/src/maps.ts` — Dehradun localities + map helpers
- `server/src/actions.ts` — all backend actions (auth, bookings, wallet, KYC, admin)
- `drizzle/` — DB migrations (services, providers, roles/KYC/admin)

## Run (Muse artifact runtime)
This app was built for the Muse TypeScript space runtime (`space.json`, `@hatch/space-sdk`). To run standalone, install Bun, `bun install`, apply the drizzle migrations to a SQLite DB, and wire the server actions to your host.

## Notes
- `client/src/vendor/hatch-maps.js.txt` (1.3 MB vendored map bundle) is not in this push — it exceeds the GitHub connector's single-file push limit. The map falls back to the Leaflet CDN / search+chips+GPS; drop the file in from the live artifact export if you need the fully-offline bundle.
- Service photos `client/src/assets/service-carpenter.jpg` and `service-ac-repair.jpg` are binary files: upload them via GitHub web (Add file → Upload files) into `web-app/client/src/assets/` — the API corrupts binaries.
