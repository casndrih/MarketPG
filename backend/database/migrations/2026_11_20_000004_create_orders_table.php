<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('orders', function (Blueprint $table) {
            $table->id();
            $table->string('order_number', 32)->unique();
            $table->foreignId('listing_id')->constrained()->restrictOnDelete();
            $table->foreignId('buyer_id')->constrained('users')->restrictOnDelete();
            $table->foreignId('seller_id')->constrained('users')->restrictOnDelete();
            $table->decimal('amount', 12, 2);
            $table->char('currency', 3)->default('PGK');
            $table->string('status', 24)->default('awaiting_payment')->index();
            $table->text('buyer_note')->nullable();
            $table->text('admin_note')->nullable();
            $table->timestamp('paid_at')->nullable();
            $table->timestamp('completed_at')->nullable();
            $table->timestamp('cancelled_at')->nullable();
            $table->timestamps();
            $table->index(['buyer_id', 'created_at']);
            $table->index(['seller_id', 'created_at']);
        });

        Schema::table('payment_receipts', function (Blueprint $table) {
            $table->foreignId('order_id')->nullable()->after('listing_id')
                ->constrained('orders')->nullOnDelete();
        });
    }

    public function down(): void
    {
        Schema::table('payment_receipts', function (Blueprint $table) {
            $table->dropConstrainedForeignId('order_id');
        });
        Schema::dropIfExists('orders');
    }
};
