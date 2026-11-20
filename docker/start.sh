#!/bin/sh
set -eu
cd /var/www/html

: "${APP_KEY:?APP_KEY must be configured in Render}"
: "${DB_URL:?DB_URL must point to the dedicated MarketPG PostgreSQL database}"

mkdir -p storage/framework/cache storage/framework/sessions storage/framework/views storage/logs storage/app/private storage/app/public bootstrap/cache
chown -R www-data:www-data storage bootstrap/cache

php artisan config:cache
php artisan view:cache
php artisan migrate --force
php artisan storage:link || true

exec apache2-foreground
