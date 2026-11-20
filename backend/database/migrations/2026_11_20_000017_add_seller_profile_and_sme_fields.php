<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->text('seller_bio')->nullable();
            $table->string('seller_website', 2048)->nullable();
            $table->string('seller_facebook_url', 2048)->nullable();
            $table->string('seller_x_url', 2048)->nullable();
            $table->string('seller_linkedin_url', 2048)->nullable();
            $table->text('sme_tin')->nullable();
            $table->string('sme_status', 20)->default('not_submitted');
            $table->text('sme_review_note')->nullable();
            $table->timestamp('sme_reviewed_at')->nullable();
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['seller_bio', 'seller_website', 'seller_facebook_url', 'seller_x_url', 'seller_linkedin_url', 'sme_tin', 'sme_status', 'sme_review_note', 'sme_reviewed_at']);
        });
    }
};
