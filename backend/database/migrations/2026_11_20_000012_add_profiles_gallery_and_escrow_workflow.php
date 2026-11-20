<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->string('town', 100)->nullable();
            $table->string('province', 100)->nullable();
            $table->string('profile_photo_path')->nullable();
            $table->string('cover_photo_path')->nullable();
            $table->text('payout_account_details')->nullable();
        });

        Schema::table('listings', function (Blueprint $table) {
            $table->string('town', 100)->nullable();
            $table->string('province', 100)->nullable();
        });

        Schema::table('orders', function (Blueprint $table) {
            $table->timestamp('shipped_at')->nullable();
            $table->timestamp('delivered_at')->nullable();
            $table->timestamp('released_at')->nullable();
            $table->string('tracking_reference', 120)->nullable();
            $table->string('payout_status', 20)->default('held')->index();
            $table->string('payout_reference', 120)->nullable();
        });

        Schema::create('listing_images', function (Blueprint $table) {
            $table->id();
            $table->foreignId('listing_id')->constrained()->cascadeOnDelete();
            $table->string('image_path');
            $table->unsignedSmallInteger('position')->default(0);
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('listing_images');
        Schema::table('orders', function (Blueprint $table) {
            $table->dropColumn(['shipped_at', 'delivered_at', 'released_at', 'tracking_reference', 'payout_status', 'payout_reference']);
        });
        Schema::table('listings', function (Blueprint $table) {
            $table->dropColumn(['town', 'province']);
        });
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['town', 'province', 'profile_photo_path', 'cover_photo_path']);
        });
    }
};
