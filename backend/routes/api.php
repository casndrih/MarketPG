<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\MarketplaceController;
use App\Http\Controllers\OrderController;
use App\Http\Controllers\MessageController;
use App\Http\Controllers\IdentityVerificationController;
use App\Http\Middleware\EnsureMarketplaceAdmin;

Route::get('/health', fn () => response()->json(['status' => 'ok', 'app' => 'MarketPG']));
Route::post('/auth/register', [AuthController::class, 'register'])->middleware('throttle:5,1');
Route::post('/auth/login', [AuthController::class, 'login'])->middleware('throttle:10,1');
Route::get('/categories', [MarketplaceController::class, 'categories']);
Route::get('/listings', [MarketplaceController::class, 'listings']);
Route::get('/listings/{listing}', [MarketplaceController::class, 'show']);

Route::middleware('auth:sanctum')->group(function () {
    Route::get('/auth/me', [AuthController::class, 'me']);
    Route::post('/auth/logout', [AuthController::class, 'logout']);
    Route::get('/my/listings', [MarketplaceController::class, 'mine']);
    Route::post('/listings', [MarketplaceController::class, 'store']);
    Route::put('/listings/{listing}', [MarketplaceController::class, 'update']);
    Route::delete('/listings/{listing}', [MarketplaceController::class, 'remove']);
    Route::get('/orders', [OrderController::class, 'index']);
    Route::get('/messages', [MessageController::class, 'index']);
    Route::get('/listings/{listing}/messages/{participant}', [MessageController::class, 'thread']);
    Route::post('/listings/{listing}/messages', [MessageController::class, 'store'])->middleware('throttle:30,1');
    Route::post('/listings/{listing}/orders', [OrderController::class, 'store']);
    Route::get('/orders/{order}', [OrderController::class, 'show']);
    Route::post('/orders/{order}/cancel', [OrderController::class, 'cancel']);
    Route::post('/orders/{order}/complete', [OrderController::class, 'complete']);
    Route::post('/listings/{listing}/receipt', [MarketplaceController::class, 'uploadReceipt']);
    Route::get('/identity-verification', [IdentityVerificationController::class, 'status']);
    Route::post('/identity-verification', [IdentityVerificationController::class, 'submit'])->middleware('throttle:3,60');

    Route::prefix('admin')->middleware(EnsureMarketplaceAdmin::class)->group(function () {
        Route::get('/listings', [MarketplaceController::class, 'adminQueue']);
        Route::post('/listings/{listing}/review', [MarketplaceController::class, 'adminReview']);
        Route::get('/orders', [OrderController::class, 'adminIndex']);
        Route::patch('/orders/{order}', [OrderController::class, 'adminUpdate']);
        Route::get('/identity-verifications', [IdentityVerificationController::class, 'queue']);
        Route::post('/identity-verifications/{verification}/review', [IdentityVerificationController::class, 'review']);
        Route::get('/identity-verifications/{verification}/documents/{kind}', [IdentityVerificationController::class, 'document']);
    });
});
