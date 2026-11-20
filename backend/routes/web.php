<?php

use App\Http\Controllers\AdminWebController;
use App\Http\Middleware\EnsureMarketplaceAdmin;
use App\Models\Listing;
use App\Models\User;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    $query = Listing::query()->with(['images', 'user'])->where('status', 'active');
    if (request()->filled('q')) $query->where('title', 'ilike', '%' . request('q') . '%');
    if (request()->filled('category')) $query->where('category_id', request('category'));
    if (request()->filled('location')) $query->where('location', 'ilike', '%' . request('location') . '%');
    $listings = $query->latest()->paginate(24)->withQueryString();
    $rawAds = DB::table('marketpg_settings')->where('key', 'ads')->value('value');
    $ads = is_string($rawAds) ? json_decode($rawAds, true) : [];
    $ads = is_array($ads) ? $ads : [];
    return view('marketplace', compact('listings', 'ads'));
})->name('home');

Route::get('/listing/{listing}', function (Listing $listing) {
    abort_unless($listing->status === 'active', 404);
    $listing->load(['images', 'user']);
    $sellerListings = Listing::where('user_id', $listing->user_id)->where('status', 'active')->where('id', '!=', $listing->id)->latest()->take(4)->get();
    $sellerSales = $listing->user->completedSales()->count();
    return view('listing-detail', compact('listing', 'sellerListings', 'sellerSales'));
})->name('listing.show');

Route::get('/seller/{user}', function (User $user) {
    $listings = Listing::with('images')->where('user_id', $user->id)->where('status', 'active')->latest()->paginate(24);
    $sales = $user->completedSales()->count();
    return view('seller', compact('user', 'listings', 'sales'));
})->name('seller.show');

Route::view('/login', 'login')->name('login');
Route::view('/about', 'pages.about')->name('about');
Route::view('/privacy-policy', 'pages.privacy')->name('privacy');
Route::view('/terms-and-conditions', 'pages.terms')->name('terms');

Route::get('/ads.txt', function () {
    $raw = DB::table('marketpg_settings')->where('key', 'ads')->value('value');
    $settings = is_string($raw) ? json_decode($raw, true) : [];
    $publisherId = is_array($settings) && ($settings['enabled'] ?? false) ? (string) ($settings['web_publisher_id'] ?? '') : '';
    abort_unless(preg_match('/^ca-pub-[0-9]{6,30}$/', $publisherId), 404);
    return response("google.com, " . substr($publisherId, 7) . ", DIRECT, f08c47fec0942fa0\n", 200, [
        'Content-Type' => 'text/plain; charset=UTF-8', 'Cache-Control' => 'public, max-age=3600',
    ]);
});

// Admin URLs are not linked from public pages. The login and dashboard remain protected
// by the authenticated admin middleware and configured administrator allowlist.
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
