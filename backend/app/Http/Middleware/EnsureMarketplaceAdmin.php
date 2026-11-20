<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureMarketplaceAdmin
{
    public function handle(Request $request, Closure $next): Response
    {
        $user = $request->user();
        $admins = array_filter(array_map('trim', explode(',', (string) env('MARKETPG_ADMIN_EMAILS', ''))));
        abort_unless($user && in_array(strtolower($user->email), array_map('strtolower', $admins), true), 403, 'Administrator access required.');
        return $next($request);
    }
}
