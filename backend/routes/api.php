<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\MarketplaceController;
use App\Http\Controllers\IdentityVerificationController;
use App\Http\Middleware\EnsureMarketplaceAdmin;

Route::get('/health', fn () => response()->json(['status' => 'ok', 'app' => 'MarketPG']));
Route::get('/categories', [MarketplaceController::class, 'categories']);
Route::get('/listings', [MarketplaceController::class, 'listings']);
Route::get('/listings/{listing}', [MarketplaceController::class, 'show']);

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/listings', [MarketplaceController::class, 'store']);
    Route::post('/listings/{listing}/receipt', [MarketplaceController::class, 'uploadReceipt']);
    Route::get('/identity-verification', [IdentityVerificationController::class, 'status']);
    Route::post('/identity-verification', [IdentityVerificationController::class, 'submit'])
        ->middleware('throttle:3,60');

    Route::prefix('admin')->middleware(EnsureMarketplaceAdmin::class)->group(function () {
        Route::get('/identity-verifications', [IdentityVerificationController::class, 'queue']);
        Route::post('/identity-verifications/{verification}/review', [IdentityVerificationController::class, 'review']);
        Route::get('/identity-verifications/{verification}/documents/{kind}', [IdentityVerificationController::class, 'document']);
    });
});
