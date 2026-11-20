FROM php:8.3-apache

RUN apt-get update \
    && apt-get install -y --no-install-recommends git unzip libpq-dev libzip-dev libicu-dev libonig-dev curl \
    && docker-php-ext-install -j"$(nproc)" pdo_pgsql zip intl mbstring \
    && a2enmod rewrite headers \
    && rm -rf /var/lib/apt/lists/*

COPY --from=composer:2 /usr/bin/composer /usr/bin/composer
WORKDIR /var/www/html

RUN composer create-project laravel/laravel . "^12.0" --no-interaction \
    && composer require laravel/sanctum league/flysystem-aws-s3-v3:^3.0 --no-interaction \
    && php artisan install:api --no-interaction

COPY backend/ /tmp/marketpg-backend/
RUN cp -a /tmp/marketpg-backend/. /var/www/html/ \
    && mkdir -p storage/framework/cache storage/framework/sessions storage/framework/views storage/logs storage/app/private storage/app/public \
    && chown -R www-data:www-data storage bootstrap/cache

COPY docker/apache-vhost.conf /etc/apache2/sites-available/000-default.conf
COPY docker/start.sh /usr/local/bin/marketpg-start
RUN chmod +x /usr/local/bin/marketpg-start

EXPOSE 10000
CMD ["/usr/local/bin/marketpg-start"]
