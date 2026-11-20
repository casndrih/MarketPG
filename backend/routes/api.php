<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\MarketplaceController;

Route::get('/health', fn () => response()->json(['status' => 'ok', 'app' => 'MarketPG']));
Route::get('/categories', [MarketplaceController::class, 'categories']);
Route::get('/listings', [MarketplaceController::class, 'listings']);
Route::get('/listings/{listing}', [MarketplaceController::class, 'show']);
Route::middleware('auth:sanctum')->group(function () {
    Route::post('/listings', [MarketplaceController::class, 'store']);
    Route::post('/listings/{listing}/receipt', [MarketplaceController::class, 'uploadReceipt']);
});