<?php

use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        web: __DIR__.'/../routes/web.php',
        api: __DIR__.'/../routes/api.php',
        commands: __DIR__.'/../routes/console.php',
        health: '/up',
    )
    ->withMiddleware(function (Middleware $middleware): void {
        // Render terminates TLS at its edge proxy. Trust forwarded headers so Laravel
        // correctly detects HTTPS requests and generates secure URLs behind the proxy.
        $middleware->trustProxies(at: '*');
        // Render enforces HTTPS at the edge; redirectToHttps() is not available
        // in this project's installed Laravel framework version.
        // MarketPG API authentication uses Sanctum bearer tokens, not cookie-based SPA sessions.
        // Keep API routes stateless so browser Origin headers cannot trigger CSRF checks on /api/auth/login.
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        //
    })->create();
