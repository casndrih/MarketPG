# MarketPG production completion checklist

MarketPG is not production-complete until every item below is implemented and verified. Do not represent a scaffold, mock screen, or API stub as a finished feature.

## Current implementation snapshot (not a completion claim)

The current code now includes public listing/category search, seller listing create/edit/delete and moderation, Sanctum registration/login/logout, buyer/seller profiles, wishlist, cash-on-pickup orders, manual payment review endpoints, buyer/seller messaging, order disputes, an allowlisted web admin dashboard, and Android API-backed Explore, account, seller, inbox, wishlist and order screens. Public API, authentication-boundary, homepage and admin-login smoke checks pass on Render. The Android debug/release builds compile in GitHub Actions.

The unchecked release gates below remain authoritative: private/persistent storage, payment receipt and identity upload enablement, production identity verification, external notification/payment providers, full user security controls, comprehensive automated transaction tests, and physical-device end-to-end acceptance are still outstanding.

## Core marketplace
- [ ] Email/password registration, verification, password reset, Google OAuth, secure sessions and token revocation
- [ ] Buyer and seller profiles, location, contact preferences and account controls
- [ ] Categories, search, filters, sorting, pagination and listing details
- [ ] Listing create/edit/delete, multiple photos, stock/availability, moderation and seller ownership checks
- [ ] Wishlist, cart, checkout, order lifecycle, cancellation, returns/disputes and notifications
- [ ] Buyer/seller messaging with abuse reporting and blocking
- [ ] PGK pricing, fees, receipts, payment reconciliation and auditable payment status
- [ ] Seller dashboard and order management

## Identity verification
- [x] PNG NID, passport, driver's licence and other government photo ID submission choices
- [x] Front image/document, optional reverse side, selfie, consent, pending/approved/rejected state
- [x] Private authenticated document retrieval and admin decision API
- [ ] Admin web UI for document review and reasoned decisions
- [ ] Production private object storage with encryption, signed short-lived access and retention/deletion policy
- [ ] Face comparison/liveness provider integration and documented manual fallback
- [ ] Verification audit log, rate limits, duplicate-account checks and appeal/resubmission workflow
- [ ] Privacy notice, consent wording and PNG legal/privacy review before collecting real identity documents

## Administration and operations
- [ ] Role/permission system, protected admin web login, MFA and least-privilege access
- [ ] Admin CRUD for users, listings, categories, orders, payments, receipts, identity checks and reports
- [ ] Moderation queue, fraud signals, audit log, support/ticket handling and configurable platform settings
- [ ] Email/SMS/push notifications, queues, scheduled jobs, backups and restore drills
- [ ] Error monitoring, structured logs, uptime/health checks and security headers

## Android
- [ ] Real API integration, secure token storage, sign-in/Google sign-in and logout
- [ ] Fully working navigation, listing detail, images, search, filters, seller flows and profile
- [ ] Identity upload/status, cart/orders, receipt upload, inbox, notifications and deep links
- [ ] Offline/error/loading/empty states, accessibility, permissions and release signing
- [ ] End-to-end tests on physical Android devices

## Production release gates
- [x] Dedicated `marketpg-db` PostgreSQL database is provisioned for MarketPG; no shared Growgreen/Casper CV database writes
- [ ] Private persistent file storage configured; never store identity documents or receipts in public storage
- [ ] Database migrations and rollback tested against a disposable PostgreSQL database
- [ ] API authorization, validation, rate-limit, upload and ownership tests pass
- [ ] Render deploy is live and health check succeeds
- [ ] Android APK installs and all critical flows pass against the deployed API
- [ ] No demo data, test credentials, secrets or placeholder payment instructions in production
