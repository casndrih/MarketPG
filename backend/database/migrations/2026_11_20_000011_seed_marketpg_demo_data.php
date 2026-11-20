<?php

use Illuminate\\Database\\Migrations\\Migration;

return new class extends Migration {
    public function up(): void
    {
        // Demo accounts and listings are provisioned by the optional background seeder.
    }

    public function down(): void
    {
        // Never remove demo or user-created marketplace data automatically.
    }
};
