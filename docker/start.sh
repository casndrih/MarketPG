#!/bin/sh
set -eu
cd /var/www/html
if [ -z "${APP_KEY:-}" ]; then
  php artisan key:generate --force
fi
if [ -n "${DATABASE_URL:-}" ] || { [ -n "${DB_HOST:-}" ] && [ -n "${DB_DATABASE:-}" ]; }; then
  php artisan migrate --force
fi
exec apache2-foreground