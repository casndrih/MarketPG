<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        if (!Schema::hasColumn('users', 'payout_account_details')) {
            Schema::table('users', function (Blueprint $table) {
                $table->text('payout_account_details')->nullable();
            });
        }
    }

    public function down(): void
    {
        if (Schema::hasColumn('users', 'payout_account_details')) {
            Schema::table('users', function (Blueprint $table) {
                $table->dropColumn('payout_account_details');
            });
        }
    }
};
