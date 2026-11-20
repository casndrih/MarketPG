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
use App\Http\Controllers\CommunityController;
use App\Http\Middleware\EnsureMarketplaceAdmin;

Route::get('/home-carousel', function () {
    $raw = DB::table('marketpg_settings')->where('key', 'home_carousel')->value('value');
    $items = is_string($raw) ? json_decode($raw, true) : [];
    $items = is_array($items) ? $items : [];
    return response()->json(['data' => array_values(array_filter($items, fn ($item) => ($item['active'] ?? false) && !empty($item['image_url'])) )]);
});

Route::get('/public-config', function () {
    $raw = DB::table('marketpg_settings')->where('key', 'ads')->value('value');
    $settings = is_string($raw) ? json_decode($raw, true) : [];
    $settings = is_array($settings) ? $settings : [];
    return response()->json(['ads' => [
        'enabled' => (bool) ($settings['enabled'] ?? false),
        'mobile_app_id' => $settings['mobile_app_id'] ?? null,
        'mobile_banner_ad_unit_id' => $settings['mobile_banner_ad_unit_id'] ?? null,
    ]]);
});

Route::get('/health', function () {
    DB::select('select 1');
    return response()->json(['status' => 'ok', 'app' => 'MarketPG', 'database' => 'connected']);
});
Route::post('/auth/register', [AuthController::class, 'register'])->middleware('throttle:10,1');
Route::post('/auth/login', [AuthController::class, 'login'])->middleware('throttle:5,1');
Route::get('/categories', [MarketplaceController::class, 'categories']);
Route::get('/listings', [MarketplaceController::class, 'listings']);
Route::get('/listings/{listing}', [MarketplaceController::class, 'show']);
Route::get('/sellers/{seller}/listings', [MarketplaceController::class, 'sellerListings']);

Route::middleware('auth:sanctum')->group(function () {
    Route::get('/auth/me', [AuthController::class, 'me']);\n    Route::get('/notifications', fn (\\Illuminate\\Http\\Request $request) => response()->json($request->user()->notifications()->latest()->paginate(30)));\n    Route::post('/notifications/{notification}/read', function (\\Illuminate\\Http\\Request $request, string $notification) { $item = $request->user()->notifications()->whereKey($notification)->firstOrFail(); $item->markAsRead(); return response()->json(['message' => 'Notification marked as read.']); });\n    Route::get('/sellers/following', [CommunityController::class, 'following']);\n    Route::post('/sellers/{seller}/follow', [CommunityController::class, 'follow']);\n    Route::delete('/sellers/{seller}/follow', [CommunityController::class, 'unfollow']);\n    Route::get('/seller-block-reasons', [CommunityController::class, 'blockReasons']);\n    Route::post('/buyers/{buyer}/block', [CommunityController::class, 'blockBuyer']);\n    Route::delete('/buyers/{buyer}/block', [CommunityController::class, 'unblockBuyer']);\n    Route::get('/support/tickets', [CommunityController::class, 'tickets']);\n    Route::post('/support/tickets', [CommunityController::class, 'createTicket'])->middleware('throttle:10,1');\n    Route::get('/support/tickets/{ticket}', [CommunityController::class, 'showTicket']);\n    Route::post('/support/tickets/{ticket}/replies', [CommunityController::class, 'replyTicket'])->middleware('throttle:20,1');
    Route::get('/profile', [ProfileController::class, 'show']);
    Route::patch('/profile', [ProfileController::class, 'update']);
    Route::post('/profile/media', [ProfileController::class, 'uploadMedia']);
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
    Route::post('/orders/{order}/receipt', [PaymentReceiptController::class, 'upload'])->middleware('throttle:5,10');
    Route::post('/orders/{order}/ship', [OrderController::class, 'ship']);
    Route::post('/orders/{order}/confirm-received', [OrderController::class, 'confirmReceived']);
    Route::get('/identity-verification', [IdentityVerificationController::class, 'status']);
    Route::post('/identity-verification', [IdentityVerificationController::class, 'submit'])->middleware('throttle:3,60');

    Route::prefix('admin')->middleware(EnsureMarketplaceAdmin::class)->group(function () {\n        Route::get('/support/tickets', [CommunityController::class, 'adminTickets']);\n        Route::patch('/support/tickets/{ticket}', [CommunityController::class, 'adminUpdateTicket']);
        Route::get('/listings', [MarketplaceController::class, 'adminQueue']);
        Route::post('/listings/{listing}/review', [MarketplaceController::class, 'adminReview']);
        Route::get('/orders', [OrderController::class, 'adminIndex']);
        Route::get('/disputes', [OrderDisputeController::class, 'adminIndex']);
        Route::post('/disputes/{dispute}/resolve', [OrderDisputeController::class, 'adminResolve']);
        Route::get('/payment-receipts', [PaymentReceiptController::class, 'index']);
        Route::get('/payment-receipts/{receipt}/document', [PaymentReceiptController::class, 'document']);
        Route::post('/payment-receipts/{receipt}/review', [PaymentReceiptController::class, 'review']);
        Route::patch('/orders/{order}', [OrderController::class, 'adminUpdate']);
        Route::post('/orders/{order}/release-payout', [OrderController::class, 'adminReleasePayout']);
        Route::get('/identity-verifications', [IdentityVerificationController::class, 'queue']);
        Route::post('/identity-verifications/{verification}/review', [IdentityVerificationController::class, 'review']);
        Route::get('/identity-verifications/{verification}/documents/{kind}', [IdentityVerificationController::class, 'document']);
    });
});
