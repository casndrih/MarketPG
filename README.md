# MarketPG

A Papua New Guinea marketplace for buying and selling new and pre-loved goods, priced in PGK (K).

## Current architecture

- Laravel 12 backend with REST API and web storefront/admin foundation
- Laravel Sanctum token authentication for Android clients
- PostgreSQL on Render, using the dedicated `marketpg-db` database exclusively for MarketPG
- Native Android client (Kotlin / Jetpack Compose)
- Render Blueprint in `render.yaml`

## Identity verification foundation

MarketPG accepts these verification document types:
- PNG National Identification Card (NID)
- Passport
- Driver's licence
- Other government-issued photo ID (manual admin review)

A submission requires the document front and a selfie; the reverse side is optional. The API records pending, approved or rejected status, requires user consent, limits submission attempts, and provides admin-only review/document endpoints. Only the final four characters of a supplied document number are retained. Uploaded identity documents are stored on Laravel's non-public local disk and are never returned as public URLs.

**Production release blocker:** Identity documents, payment receipts and listing images are intentionally disabled by default (`IDENTITY_UPLOADS_ENABLED=false`, `PRIVATE_UPLOADS_ENABLED=false`) until persistent private/public object storage is configured. The Android listing form currently submits text-only listings. Render's local filesystem is ephemeral. Before collecting real identity documents, configure encrypted private object storage with short-lived authorized access, retention/deletion controls and backups. Add a liveness/face-match provider or a documented manual identity comparison procedure. Obtain PNG legal/privacy review and publish a privacy notice before collecting real ID data.

Set `MARKETPG_ADMIN_EMAILS` in Render to a comma-separated allowlist of administrator email addresses. Do not commit this value or any credentials to Git.

## API foundation

- `GET /api/health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me` (Sanctum)
- `POST /api/auth/logout` (Sanctum)
- `GET /api/categories`
- `GET /api/listings`
- `GET /api/listings/{listing}`
- `GET /api/my/listings` (Sanctum)
- `PUT /api/listings/{listing}` and `DELETE /api/listings/{listing}` (owner only)
- `POST /api/listings/{listing}/orders` (Sanctum; reserves an available listing)
- `GET /api/profile` and `PATCH /api/profile` (Sanctum; name, phone, location, buyer/seller account type)
- `GET /api/orders` and `GET /api/orders/{order}` (buyer/seller only)
- `GET /api/wishlist`, `POST /api/listings/{listing}/wishlist` and `DELETE /api/listings/{listing}/wishlist` (Sanctum)
- `POST /api/orders/{order}/cancel` (buyer only)
- `POST /api/orders/{order}/confirm-cash` (seller confirms cash-on-pickup payment)
- Checkout supports cash on pickup; bank transfer remains unavailable until `MARKETPG_BANK_TRANSFER_DETAILS` is configured.
- `POST /api/orders/{order}/complete` (seller after payment confirmation)
- `GET /api/disputes` and `POST /api/orders/{order}/disputes` (order participants)
- `GET /api/admin/disputes` and `POST /api/admin/disputes/{dispute}/resolve` (admin allowlist)
- `GET /api/messages` and listing-scoped message thread/send endpoints (Sanctum)
- `POST /api/listings` (Sanctum)
- `POST /api/listings/{listing}/receipt` (Sanctum)
- `GET /api/identity-verification` (Sanctum)
- `POST /api/identity-verification` (Sanctum)
- `GET /api/admin/listings` and `POST /api/admin/listings/{listing}/review` (Sanctum + admin allowlist)
- `GET /api/admin/orders` and `PATCH /api/admin/orders/{order}` (Sanctum + admin allowlist)
- `GET /api/admin/payment-receipts`, `GET /api/admin/payment-receipts/{receipt}/document` and `POST /api/admin/payment-receipts/{receipt}/review` (Sanctum + admin allowlist)
- `GET /api/admin/identity-verifications` (Sanctum + admin allowlist)
- `POST /api/admin/identity-verifications/{verification}/review` (Sanctum + admin allowlist)
- `GET /api/admin/identity-verifications/{verification}/documents/{kind}` (Sanctum + admin allowlist)

## Shared database safety

The Render Blueprint provisions/adopts the dedicated `marketpg-db` PostgreSQL database and injects its internal connection string as `DB_URL`. MarketPG does not share its database with Growgreen or Casper CV. Laravel migrations run against this dedicated database only.

## Completion and release gates

See [backend/docs/COMPLETION_CHECKLIST.md](backend/docs/COMPLETION_CHECKLIST.md). The project is not production-complete merely because the Android APK compiles or the Laravel container deploys. Release requires working buyer/seller/order/payment/admin flows, private persistent file storage, secure identity review, Google OAuth credentials, notification delivery, automated tests, Android-to-API integration and end-to-end verification.

## Android

Open `android/` in Android Studio. The Compose client now connects to the REST API for live listing/category search, account registration/sign-in with persisted Sanctum tokens, wishlist, listing creation, seller listing editing/removal, inbox/replies, cash-on-pickup order placement, buyer/seller order actions, dispute reporting and dispute-history tracking. GitHub Actions compiles the release APK and publishes it as a workflow artifact. Remaining Android work includes listing image upload and richer listing-detail flows, identity verification UI after secure storage is configured, push notifications, and end-to-end device testing. The client is not yet production-complete.
