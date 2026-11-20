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
        $admins = config('marketpg.admin_emails', []);
        abort_unless($user && in_array(strtolower($user->email), $admins, true), 403, 'Administrator access required.');
        return $next($request);
    }
}
