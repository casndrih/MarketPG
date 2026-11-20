<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void {
        Schema::create('listings', function (Blueprint $table) {
            $table->id(); $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->unsignedBigInteger('category_id')->nullable(); $table->string('title',120);
            $table->text('description'); $table->decimal('price',12,2); $table->string('condition',20);
            $table->string('location',120); $table->string('image_path')->nullable();
            $table->string('status',20)->default('pending'); $table->timestamps();
            $table->index(['status','created_at']);
        });
        Schema::create('payment_receipts', function (Blueprint $table) {
            $table->id(); $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->foreignId('listing_id')->constrained()->cascadeOnDelete();
            $table->string('order_reference',80); $table->string('file_path');
            $table->string('status',20)->default('pending'); $table->text('notes')->nullable(); $table->timestamps();
        });
    }
    public function down(): void { Schema::dropIfExists('payment_receipts'); Schema::dropIfExists('listings'); }
};