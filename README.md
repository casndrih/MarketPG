# MarketPG

**A Papua New Guinea marketplace for local buying and selling.**

MarketPG is a full-stack marketplace platform designed to connect buyers and sellers throughout Papua New Guinea. It brings together a responsive web storefront, a Laravel-powered REST API, a web-based administration workspace and a native Android application.

The platform is designed around local listings, PGK pricing, PNG provinces and towns, buyer and seller accounts, moderated listings, bank-transfer checkout, order tracking, identity review and marketplace administration.

> **Project status:** MarketPG has a working application foundation and implemented marketplace workflows, but it is **not yet production-complete**. Features that depend on external credentials, persistent object storage, legal/privacy review or end-to-end device testing are identified below. A successful deployment or APK build alone does not mean every transaction flow is production-ready.

---

## Platform at a glance

| Layer | Technology | Purpose |
|---|---|---|
| Web application | Laravel 12, Blade, Tailwind CSS, Flowbite | Public marketplace, listing pages, seller storefronts and admin workspace |
| Backend | PHP 8.4 / Laravel REST API | Authentication, listings, profiles, messaging, orders, receipts, identity and moderation |
| Mobile application | Kotlin, Jetpack Compose, Material 3 | Native Android marketplace client |
| Authentication | Laravel Sanctum | API token authentication for mobile clients |
| Database | PostgreSQL | Dedicated MarketPG application data |
| Hosting | Render | Web service, PostgreSQL database and deployment |
| CI | GitHub Actions | PHP checks, smoke checks and Android release APK builds |
| Advertising | Google AdSense / Google AdMob integration points | Website and Android advertising, controlled by administrators |

## Product areas

### 1. Public web marketplace

The web storefront provides a responsive entry point for customers using desktop, tablet and mobile browsers.

Current web features include:

- Marketplace homepage displaying active listings.
- Listing search by title, category and location.
- Paginated listing results.
- Listing detail pages with item information and seller details.
- Seller storefront pages showing that seller's active listings.
- Top-seller presentation based on completed sales.
- PGK currency presentation using the Kina (K) symbol.
- Public About, Privacy Policy and Terms and Conditions pages.
- Public sign-in page and shared light/dark theme preference.
- Google AdSense integration points, including an `ads.txt` response when valid publisher settings are enabled.

The public web application is served by the Laravel application. The production URL configured in this repository is:

- **Marketplace:** https://marketpg.onrender.com

### 2. Android application

The Android client is a native Kotlin application built with Jetpack Compose and Material 3. It communicates with the Laravel API rather than maintaining a separate marketplace backend.

The mobile experience includes:

- Dedicated sign-in and account-registration screen.
- Mandatory authentication before entering the marketplace.
- Email and password authentication with persisted Sanctum session token.
- Password visibility toggle on password fields.
- Light and dark appearance with theme-aware system bars and navigation.
- Explore tab with featured listing carousel and listing grid.
- Search screen and listing search results.
- Pull-to-refresh for marketplace listings.
- Listing cards showing item title, price and location.
- Category, location/province, price and condition filtering, with sorting.
- Listing detail experience and seller contact entry points.
- Create listing form with title, description, price, condition, category and PNG location fields.
- Province selection using the configured PNG province list.
- Seller's own listing management, including editing and removal.
- Saved listings / wishlist.
- Buyer and seller order views and available order actions.
- Payment receipt selection and submission for administrator review.
- Inbox and listing-related buyer/seller messaging.
- Dispute reporting and dispute-history views.
- Profile information, account type and identity-verification status views.
- AdMob banner integration points controlled by the platform's advertising settings.
- Release APK build and artifact publishing through GitHub Actions.

**Google authentication note:** The Google button is present in the authentication UI, but Google OAuth is not yet an active sign-in method. A valid OAuth client configuration and server-side ID-token verification must be completed before it can authenticate users.

**Media note:** Listing-photo, profile-photo, cover-photo and private document workflows depend on correctly configured persistent storage. The Android UI/API foundations do not mean these uploads are ready for production while storage feature flags remain disabled.

### 3. Administration workspace

The web administration workspace is designed for authorized marketplace operators. It uses a separate admin login and an email allowlist configured through the server environment.

Admin entry points:

- **Admin sign-in:** https://marketpg.onrender.com/admin/login
- **Admin dashboard:** https://marketpg.onrender.com/admin

Current administration features include:

- Dashboard summary counts for users, active listings, pending listings and orders.
- Pending listing moderation with approve/reject decisions and moderation notes.
- Order review and status management.
- Order visibility including buyer, seller, payment method, shipment reference and payout status.
- Payment receipt review with access to the private receipt document route.
- Receipt approval or rejection, with a reason required for rejection.
- Identity verification queue with document type and masked document-number suffix.
- Protected viewing of submitted ID front, optional back and selfie documents.
- Identity approval/rejection with a recorded reason for rejection.
- Dispute review and resolution/rejection with a written resolution.
- Seller payout review and recording of a bank transfer reference after delivery confirmation.
- Google advertising controls for website AdSense and Android AdMob.
- Shared light/dark admin theme and sign-out.

Administrator access is restricted by the configured `MARKETPG_ADMIN_EMAILS` allowlist. The allowlist and credentials must remain in Render environment settings and must never be committed to source control. An unlinked or non-obvious admin URL is not a security boundary; authentication and authorization middleware provide the protection.

---

## Marketplace capabilities and workflows

### Accounts and profiles

- Email/password registration and sign-in through the API.
- Sanctum-authenticated API sessions for Android.
- Buyer, seller or combined account type.
- Profile details including name, phone and location.
- PNG province and town fields.
- Profile and cover image fields are part of the platform's profile requirements.
- Account ownership checks protect personal listings and order data.

### Listings and discovery

- Category-backed marketplace listings.
- Listing title, description, price, condition and location.
- Province-aware location data for PNG.
- Search, filters, sorting and paginated API results.
- Seller listing creation, editing and deletion.
- Listing moderation before public activation.
- Seller storefronts and seller listing history.
- Wishlist / saved listings.
- Listing reservation when an order is created, preventing competing purchases of the same active listing.

### Messaging

- Authenticated inbox and message retrieval.
- Listing-scoped buyer/seller conversations.
- Message sending and replies through the API.
- Access is limited to authenticated marketplace users and relevant conversation participants.

### Bank-transfer checkout and order lifecycle

MarketPG's configured checkout method is **bank transfer**. Cash-on-pickup is not the intended payment flow.

The implemented order workflow is:

1. The buyer must have an approved identity-verification record and the required profile and cover photos before placing an order.
2. The buyer places an order for an available listing. The listing is reserved and the order enters `awaiting_payment`.
3. The buyer transfers funds to the official MarketPG platform bank account using the configured payment instructions.
4. The buyer submits a payment receipt for review.
5. An administrator checks the receipt against the actual platform bank statement and approves or rejects it. Approval marks the associated order as paid.
6. The seller records shipment and a tracking reference. The order moves to `shipped`.
7. The buyer confirms receipt of the item. The order moves to `delivered` and the seller payout becomes pending.
8. An administrator records the seller payout release and bank transfer reference after the payout has actually been made.

Order states and payout states are kept separate so that delivery confirmation and payout release are distinct steps.

**Important payment configuration:** Checkout deliberately rejects the default bank-transfer setup template. The platform owner must configure verified MarketPG bank details in the Render environment variable `MARKETPG_BANK_TRANSFER_DETAILS` before accepting real payments. See [Bank-transfer setup](backend/MARKETPG_BANK_TRANSFER_SETUP.md).

This is a manually administered bank-transfer workflow. It is **not** an automated escrow service, payment gateway or automatic bank reconciliation integration.

### Identity verification

The API and admin review foundation supports these document categories:

- PNG National Identification Card (NID)
- Passport
- Driver's licence
- Other government-issued photo ID

A submission is designed to include the document front and a selfie; the reverse side is optional. The workflow records user consent and pending, approved or rejected review status. Administrators can inspect documents through protected, non-public routes. The application retains only the final four characters of a supplied document number.

**Production restriction:** Identity uploads are disabled by default. Do not collect real identity documents until private persistent object storage, access controls, encryption, retention/deletion procedures, backups, a privacy notice and PNG legal/privacy review are in place. A liveness/face-match provider or a documented manual identity comparison procedure is also required.

### Disputes

- Buyers and sellers who participate in an order can raise or view order disputes.
- Disputes include a reason and supporting details.
- Administrators can review open disputes and record a resolution or rejection.
- The previous order state is retained and restored when a dispute is resolved through the current workflow.

### Seller payout details

Seller payout bank details are stored as encrypted application data and are intended for authorized payout administration. A payout release can only be recorded for a delivered order awaiting payout and requires a payout reference. The platform must still verify the actual transfer through its banking records.

### Advertising

The platform includes administrator-managed integration settings for:

- **Website:** Google AdSense publisher ID and display ad slot.
- **Android:** Google AdMob application ID and banner ad unit ID.
- A shared enable/disable control for advertising.

Advertising is disabled until the platform owner enters valid IDs. Ad delivery, account approval and policy compliance depend on the corresponding Google publisher accounts and configuration.

---

## API overview

The Laravel REST API is the shared application layer for the Android client and other authorized integrations. API routes are versioned under the `/api` prefix and use Sanctum authentication for protected operations.

| Area | Representative endpoints |
|---|---|
| Health | `GET /api/health` |
| Authentication | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout` |
| Marketplace | `GET /api/categories`, `GET /api/listings`, `GET /api/listings/{listing}` |
| Seller listings | `POST /api/listings`, `GET /api/my/listings`, `PUT /api/listings/{listing}`, `DELETE /api/listings/{listing}` |
| Profile | `GET /api/profile`, `PATCH /api/profile` |
| Wishlist | `GET /api/wishlist`, `POST /api/listings/{listing}/wishlist`, `DELETE /api/listings/{listing}/wishlist` |
| Orders | `POST /api/listings/{listing}/orders`, `GET /api/orders`, `GET /api/orders/{order}`, `POST /api/orders/{order}/cancel` |
| Messages | `GET /api/messages` and listing-scoped message thread/send routes |
| Receipts | `POST /api/listings/{listing}/receipt` |
| Identity | `GET /api/identity-verification`, `POST /api/identity-verification` |
| Disputes | `GET /api/disputes`, `POST /api/orders/{order}/disputes` |
| Admin API | Admin listing, order, receipt, identity and dispute review routes under `/api/admin` |

Protected routes enforce authentication, ownership or order-participant checks and administrator authorization as appropriate. For exact request validation and route definitions, see `backend/routes/api.php`.

---

## Architecture and repository layout

```text
MarketPG/
├── android/                 # Kotlin / Jetpack Compose Android application
├── backend/
│   ├── app/                 # Laravel controllers, models and middleware
│   ├── bootstrap/            # Laravel application bootstrap
│   ├── config/               # Application and marketplace configuration
│   ├── database/              # PostgreSQL migrations and seeders
│   ├── resources/views/       # Public website and admin Blade views
│   ├── routes/                # Web and REST API routes
│   └── docs/                  # Completion and release checklist
├── .github/workflows/         # PHP checks, smoke checks and Android builds
├── Dockerfile                 # Render container build
├── render.yaml                # Render Blueprint
├── MarketPG.apk               # Published Android release APK
└── README.md
```

### Data and hosting

- The Render Blueprint provisions a dedicated PostgreSQL database named `marketpg-db` for MarketPG.
- The Laravel service uses the database connection supplied by Render through `DB_URL`.
- MarketPG data is separate from other projects and databases.
- The web service is configured for the `main` branch and uses the repository's Dockerfile.
- The Render health-check path is `/up`.
- GitHub Actions provides automated PHP checks, application smoke checks and Android release builds.

### File storage

Render's local application filesystem is ephemeral. Do not use it as permanent storage for user uploads.

Before enabling production uploads, configure an S3-compatible object-storage provider with separate public and private storage locations. Listing/profile/cover media may use the public disk as appropriate; identity documents and payment receipts must use private storage and authenticated access. The relevant feature flags are intentionally disabled by default:

- `PROFILE_UPLOADS_ENABLED=false`
- `IDENTITY_UPLOADS_ENABLED=false`
- `PRIVATE_UPLOADS_ENABLED=false`

Configure the storage credentials and disk names in Render environment settings. Never commit access keys or private storage credentials.

---

## Local development

### Requirements

- PHP 8.4 or a compatible supported PHP 8.x runtime.
- Composer.
- PostgreSQL.
- Node.js and npm if building frontend assets.
- Android Studio with a compatible Android SDK and JDK for the mobile client.

### Backend

1. Clone the repository.
2. Change into the backend directory.
3. Install PHP dependencies with Composer.
4. Create the local environment file and configure the application key, PostgreSQL connection, URL and required service settings.
5. Run database migrations against a development database.
6. Start the Laravel development server.

Example:

```bash
git clone https://github.com/casndrih/MarketPG.git
cd MarketPG/backend
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate
php artisan serve
```

The exact environment values depend on the local database and services. Do not copy production credentials into a local environment file or commit `.env`.

### Android

1. Open the repository's `android/` directory in Android Studio.
2. Allow Gradle sync to complete.
3. Configure the API base URL for the target environment in the Android networking configuration.
4. Run the app on an emulator or physical Android device.

GitHub Actions builds the release APK and publishes it as a workflow artifact. The repository-root `MarketPG.apk` is the published APK copy when the APK publishing workflow has completed successfully.

---

## Environment configuration

Set production configuration in Render's environment settings, not in Git.

| Variable | Purpose |
|---|---|
| `APP_KEY` | Laravel application encryption key |
| `APP_URL` | Public application URL |
| `DB_URL` | Render-provided PostgreSQL connection string |
| `MARKETPG_ADMIN_EMAILS` | Comma-separated administrator email allowlist |
| `MARKETPG_BANK_TRANSFER_DETAILS` | Verified platform bank-transfer instructions; checkout remains blocked while placeholders are present |
| `MARKETPG_PUBLIC_DISK` | Configured public object-storage disk |
| `MARKETPG_PRIVATE_DISK` | Configured private object-storage disk |
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` | Object-storage credentials |
| `AWS_DEFAULT_REGION`, `AWS_BUCKET`, `AWS_ENDPOINT`, `AWS_URL` | Object-storage configuration |
| `PROFILE_UPLOADS_ENABLED` | Enables profile/cover upload functionality after storage setup |
| `IDENTITY_UPLOADS_ENABLED` | Enables identity document uploads after privacy/security readiness |
| `PRIVATE_UPLOADS_ENABLED` | Enables private uploads such as payment receipts and identity files |

Use the Render Blueprint in `render.yaml` as the infrastructure reference. Review [the bank-transfer setup guide](backend/MARKETPG_BANK_TRANSFER_SETUP.md) before enabling checkout.

---

## Security and operational safeguards

- Keep administrator allowlist values, application keys and storage credentials out of source control.
- Use HTTPS for production traffic.
- Keep identity documents and payment receipts on private storage, never public URLs.
- Use authenticated, authorized document access and no-store response headers.
- Verify payment receipts against the actual platform bank statement before approving payment.
- Release seller payouts only after buyer delivery confirmation and record the transfer reference.
- Keep listing ownership and order-participant authorization checks enabled.
- Do not enable uploads while persistent storage and privacy controls are incomplete.
- Do not treat a successful build, smoke test or deployment as proof of end-to-end production readiness.

---

## Current production-readiness gates

The following work remains necessary before MarketPG should be represented as a fully production-ready marketplace:

- Configure and test persistent S3-compatible public/private storage.
- Complete privacy, retention, deletion, backup and PNG legal review for identity information.
- Implement and verify production Google OAuth and server-side token validation.
- Complete end-to-end buyer, seller, bank-transfer, receipt-review, shipment, delivery and payout testing.
- Complete automated authorization, validation, upload and transaction tests.
- Configure and verify email, SMS or push notification delivery where required.
- Complete Android physical-device acceptance testing, including permissions, loading, offline and error states.
- Confirm live AdSense/AdMob account approval and ad delivery using real publisher IDs.
- Verify production backups, restore procedures, monitoring and operational support.

The detailed release checklist is maintained in [backend/docs/COMPLETION_CHECKLIST.md](backend/docs/COMPLETION_CHECKLIST.md).

---

## Project links

- **Live marketplace:** https://marketpg.onrender.com
- **Admin sign-in:** https://marketpg.onrender.com/admin/login
- **GitHub repository:** https://github.com/casndrih/MarketPG
- **Latest Android APK:** https://github.com/casndrih/MarketPG/raw/refs/heads/main/MarketPG.apk

## License

See [LICENSE](LICENSE).

## Password reset email configuration

MarketPG password recovery uses Laravel's password broker. Reset requests send a time-limited link to the account's registered email address; the user must follow that link and confirm a new password. Reset tokens are stored hashed in the dedicated `password_reset_tokens` table and expire after 60 minutes.

Configure the following environment variables in the MarketPG Render web service before enabling password recovery:

```env
APP_URL=https://marketpg.onrender.com
MAIL_MAILER=smtp
MAIL_HOST=[YOUR SMTP HOST]
MAIL_PORT=587
MAIL_SCHEME=smtp
MAIL_USERNAME=[YOUR SMTP USERNAME]
MAIL_PASSWORD=[YOUR SMTP PASSWORD]
MAIL_FROM_ADDRESS=[YOUR VERIFIED SENDER EMAIL]
MAIL_FROM_NAME=MarketPG
```

Use the SMTP host, port, security mode and verified sender supplied by your email provider. Do not commit real SMTP credentials to GitHub. After deployment, run the normal Laravel database migrations so the password reset token table is created. Password recovery cannot deliver messages until valid SMTP credentials and sender-domain authentication are configured.
