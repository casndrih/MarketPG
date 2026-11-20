<?php

use App\Http\Controllers\AdminWebController;
use App\Http\Middleware\EnsureMarketplaceAdmin;
use App\Models\Listing;
use Illuminate\Support\Facades\Route;
use Illuminate\Support\Facades\DB;

Route::get('/', function () {
    $query = Listing::query()->where('status', 'active')->latest();
    if (request()->filled('q')) {
        $query->where('title', 'ilike', '%' . request('q') . '%');
    }
    $listings = $query->take(24)->get();
    $rawAds = DB::table('marketpg_settings')->where('key', 'ads')->value('value');
    $ads = is_string($rawAds) ? json_decode($rawAds, true) : [];
    $ads = is_array($ads) ? $ads : [];
    return view('marketplace', compact('listings', 'ads'));
});

Route::get('/login', [AdminWebController::class, 'loginForm'])->name('login');
Route::get('/admin/login', [AdminWebController::class, 'loginForm'])->name('admin.login');
Route::post('/admin/login', [AdminWebController::class, 'login'])->middleware('throttle:5,1')->name('admin.login.submit');

Route::prefix('admin')->name('admin.')->middleware(['auth', EnsureMarketplaceAdmin::class])->group(function () {
    Route::get('/', [AdminWebController::class, 'dashboard'])->name('dashboard');
    Route::post('/logout', [AdminWebController::class, 'logout'])->name('logout');
    Route::post('/ads', [AdminWebController::class, 'updateAds'])->name('ads.update');
    Route::post('/listings/{listing}/review', [AdminWebController::class, 'reviewListing'])->name('listings.review');
    Route::post('/orders/{order}/review', [AdminWebController::class, 'reviewOrder'])->name('orders.review');
    Route::post('/orders/{order}/release-payout', [AdminWebController::class, 'releasePayout'])->name('orders.release-payout');
    Route::post('/disputes/{dispute}/resolve', [AdminWebController::class, 'resolveDispute'])->name('disputes.resolve');
    Route::post('/receipts/{receipt}/review', [AdminWebController::class, 'reviewReceipt'])->name('receipts.review');
    Route::get('/receipts/{receipt}/document', [AdminWebController::class, 'receiptDocument'])->name('receipts.document');
    Route::post('/identity/{verification}/review', [AdminWebController::class, 'reviewIdentity'])->name('identity.review');
    Route::get('/identity/{verification}/documents/{kind}', [AdminWebController::class, 'identityDocument'])->name('identity.document');
});
