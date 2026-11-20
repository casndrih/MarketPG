#!/bin/sh
set -eu
cd /var/www/html
if [ -z "${APP_KEY:-}" ]; then
  php artisan key:generate --force
fi
php artisan migrate --force
php artisan storage:link || true
exec apache2-foreground
