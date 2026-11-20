<?php

return [
    'admin_emails' => array_values(array_filter(array_map(
        static fn ($email) => strtolower(trim($email)),
        explode(',', (string) env('MARKETPG_ADMIN_EMAILS', ''))
    ))),
    'identity_uploads_enabled' => (bool) env('IDENTITY_UPLOADS_ENABLED', false),
    'bank_transfer_details' => (string) env('MARKETPG_BANK_TRANSFER_DETAILS', ''),
];
