<?php

$adminEmails = array_values(array_filter(array_map(
        static fn ($email) => strtolower(trim($email)),
        explode(',', (string) env('MARKETPG_ADMIN_EMAILS', ''))
    )));

return [
    'admin_emails' => array_values(array_unique(array_merge($adminEmails ?? [], array_filter([strtolower(trim((string) env('MARKETPG_DEMO_ADMIN_EMAIL', '')))])))),
    'identity_uploads_enabled' => (bool) env('IDENTITY_UPLOADS_ENABLED', false),
    'private_uploads_enabled' => (bool) env('PRIVATE_UPLOADS_ENABLED', false),
    'profile_uploads_enabled' => (bool) env('PROFILE_UPLOADS_ENABLED', false),
    'private_disk' => (string) env('MARKETPG_PRIVATE_DISK', 'local'),
    'public_disk' => (string) env('MARKETPG_PUBLIC_DISK', 'public'),
    'bank_transfer_details' => (string) env('MARKETPG_BANK_TRANSFER_DETAILS', ''),
    'png_provinces' => ['Central', 'Gulf', 'Milne Bay', 'Northern (Oro)', 'Southern Highlands', 'Western Highlands', 'Enga', 'Simbu', 'Eastern Highlands', 'Morobe', 'Madang', 'East Sepik', 'West Sepik (Sandaun)', 'West New Britain', 'East New Britain', 'New Ireland', 'Manus', 'Hela', 'Jiwaka', 'Western', 'National Capital District', 'Autonomous Region of Bougainville'],
];
