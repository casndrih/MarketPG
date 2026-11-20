<?php

use Illuminate\Support\Facades\Route;
use Illuminate\Support\Facades\DB;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\MarketplaceController;
use App\Http\Controllers\OrderController;
use App\Http\Controllers\MessageController;
use App\Http\Controllers\WishlistController;
use App\Http\Controllers\PaymentReceiptController;
use App\Http\Controllers\ProfileController;
use App\Http\Controllers\OrderDisputeController;
use App\Http\Controllers\IdentityVerificationController;
use App\Http\Middleware\EnsureMarketplaceAdmin;

Route::get('/health', function () {
    DB::select('select 1');
    return response()->json(['status' => 'ok', 'app' => 'MarketPG', 'database' => 'connected']);
});
Route::post('/auth/register', [AuthController::class, 'register'])->middleware('throttle:10,1');
Route::post('/auth/login', [AuthController::class, 'login'])->middleware('throttle:5,1');
Route::get('/categories', [MarketplaceController::class, 'categories']);
Route::get('/listings', [MarketplaceController::class, 'listings']);
Route::get('/listings/{listing}', [MarketplaceController::class, 'show']);

Route::middleware('auth:sanctum')->group(function () {
    Route::get('/auth/me', [AuthController::class, 'me']);
    Route::get('/profile', [ProfileController::class, 'show']);
    Route::patch('/profile', [ProfileController::class, 'update']);
    Route::post('/auth/logout', [AuthController::class, 'logout']);
    Route::get('/my/listings', [MarketplaceController::class, 'mine']);
    Route::post('/listings', [MarketplaceController::class, 'store']);
    Route::put('/listings/{listing}', [MarketplaceController::class, 'update']);
    Route::delete('/listings/{listing}', [MarketplaceController::class, 'remove']);
    Route::get('/orders', [OrderController::class, 'index']);
    Route::get('/disputes', [OrderDisputeController::class, 'index']);
    Route::post('/orders/{order}/disputes', [OrderDisputeController::class, 'store']);
    Route::get('/wishlist', [WishlistController::class, 'index']);
    Route::post('/listings/{listing}/wishlist', [WishlistController::class, 'store']);
    Route::delete('/listings/{listing}/wishlist', [WishlistController::class, 'destroy']);
    Route::get('/messages', [MessageController::class, 'index']);
    Route::get('/listings/{listing}/messages/{participant}', [MessageController::class, 'thread']);
    Route::post('/listings/{listing}/messages', [MessageController::class, 'store'])->middleware('throttle:30,1');
    Route::post('/listings/{listing}/orders', [OrderController::class, 'store']);
    Route::get('/orders/{order}', [OrderController::class, 'show']);
    Route::post('/orders/{order}/cancel', [OrderController::class, 'cancel']);
    Route::post('/orders/{order}/confirm-cash', [OrderController::class, 'confirmCash']);
    Route::post('/orders/{order}/complete', [OrderController::class, 'complete']);
    Route::post('/listings/{listing}/receipt', [MarketplaceController::class, 'uploadReceipt']);
    Route::get('/identity-verification', [IdentityVerificationController::class, 'status']);
    Route::post('/identity-verification', [IdentityVerificationController::class, 'submit'])->middleware('throttle:3,60');

    Route::prefix('admin')->middleware(EnsureMarketplaceAdmin::class)->group(function () {
        Route::get('/listings', [MarketplaceController::class, 'adminQueue']);
        Route::post('/listings/{listing}/review', [MarketplaceController::class, 'adminReview']);
        Route::get('/orders', [OrderController::class, 'adminIndex']);
        Route::get('/disputes', [OrderDisputeController::class, 'adminIndex']);
        Route::post('/disputes/{dispute}/resolve', [OrderDisputeController::class, 'adminResolve']);
        Route::get('/payment-receipts', [PaymentReceiptController::class, 'index']);
        Route::get('/payment-receipts/{receipt}/document', [PaymentReceiptController::class, 'document']);
        Route::post('/payment-receipts/{receipt}/review', [PaymentReceiptController::class, 'review']);
        Route::patch('/orders/{order}', [OrderController::class, 'adminUpdate']);
        Route::get('/identity-verifications', [IdentityVerificationController::class, 'queue']);
        Route::post('/identity-verifications/{verification}/review', [IdentityVerificationController::class, 'review']);
        Route::get('/identity-verifications/{verification}/documents/{kind}', [IdentityVerificationController::class, 'document']);
    });
});
