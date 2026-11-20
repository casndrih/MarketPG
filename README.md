# MarketPG

A Papua New Guinea local marketplace for buying and selling new and pre-loved goods.

## Architecture
- Laravel 12 backend, web storefront and admin foundation
- REST API with Sanctum-protected seller and receipt endpoints
- Native Android client (Kotlin / Jetpack Compose)
- Render deployment (Singapore region)

## Backend
The Render Docker build bootstraps Laravel 12 and overlays the application code in `backend/`. Configure production APP settings and PostgreSQL credentials before production use.

## Android
Open `android/` in Android Studio. CI builds a release APK and publishes it as a GitHub Actions artifact.

## Production requirements
Configure persistent object storage for listing images and private payment receipts, Google OAuth credentials, SMTP, database credentials, and admin accounts. Receipt uploads must remain private and be reviewed by authorized administrators; never trust a client-side payment confirmation.
