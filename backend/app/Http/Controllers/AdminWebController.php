<?php

namespace App\Http\Controllers;

use App\Models\IdentityVerification;
use App\Models\Listing;
use App\Models\Order;
use App\Models\OrderDispute;
use App\Models\PaymentReceipt;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;
use Illuminate\Validation\Rule;

class AdminWebController extends Controller
{
    public function loginForm()
    {
        return view('admin.login');
    }

    public function login(Request $request)
    {
        $credentials = $request->validate([
            'email' => ['required', 'email'],
            'password' => ['required', 'string'],
        ]);

        if (!Auth::attempt(['email' => strtolower($credentials['email']), 'password' => $credentials['password']])) {
            return back()->withErrors(['email' => 'The supplied credentials are incorrect.'])->onlyInput('email');
        }

        $request->session()->regenerate();
        $admins = config('marketpg.admin_emails', []);
        if (!in_array(strtolower((string) Auth::user()->email), $admins, true)) {
            Auth::logout();
            $request->session()->invalidate();
            $request->session()->regenerateToken();
            return back()->withErrors(['email' => 'This account is not on the MarketPG administrator allowlist.'])->onlyInput('email');
        }

        return redirect()->route('admin.dashboard');
    }

    public function dashboard()
    {
        return view('admin.dashboard', [
            'pendingListings' => Listing::with('user:id,name,email')->where('status', 'pending')->latest()->get(),
            'orders' => Order::with(['listing:id,title', 'buyer:id,name,email', 'seller:id,name,email'])->latest()->take(40)->get(),
            'disputes' => OrderDispute::with(['order.listing:id,title', 'order.buyer:id,name,email', 'order.seller:id,name,email', 'raiser:id,name,email'])->where('status', 'open')->latest()->get(),
            'pendingReceipts' => PaymentReceipt::with(['user:id,name,email', 'listing:id,title', 'order:id,order_number,amount,currency'])->where('status', 'pending')->latest()->get(),
            'pendingVerifications' => IdentityVerification::with('user:id,name,email')->where('status', 'pending')->latest()->get(),
            'ads' => $this->adsSettings(),
            'carouselItems' => $this->carouselSettings(),
            'pendingSmes' => User::where('sme_status', 'pending')->whereNotNull('sme_tin')->latest()->get(),
            'counts' => [
                'users' => \App\Models\User::count(),
                'active_listings' => Listing::where('status', 'active')->count(),
                'pending_listings' => Listing::where('status', 'pending')->count(),
                'orders' => Order::count(),
            ],
        ]);
    }

    private function adsSettings(): array
    {
        $raw = DB::table('marketpg_settings')->where('key', 'ads')->value('value');
        $settings = is_string($raw) ? json_decode($raw, true) : [];
        return array_merge([
            'enabled' => false,
            'web_publisher_id' => '',
            'web_slot_id' => '',
            'mobile_app_id' => '',
            'mobile_banner_ad_unit_id' => '',
        ], is_array($settings) ? $settings : []);
    }

    public function updateAds(Request $request)
    {
        $data = $request->validate([
            'enabled' => ['nullable', 'boolean'],
            'web_publisher_id' => ['nullable', 'string', 'regex:/^ca-pub-[0-9]{6,30}$/'],
            'web_slot_id' => ['nullable', 'string', 'regex:/^[0-9]{6,30}$/'],
            'mobile_app_id' => ['nullable', 'string', 'regex:/^ca-app-pub-[0-9]{6,30}~[0-9]{6,30}$/'],
            'mobile_banner_ad_unit_id' => ['nullable', 'string', 'regex:#^ca-app-pub-[0-9]{6,30}/[0-9]{6,30}$#'],
        ]);

        $settings = [
            'enabled' => (bool) ($data['enabled'] ?? false),
            'web_publisher_id' => trim((string) ($data['web_publisher_id'] ?? '')),
            'web_slot_id' => trim((string) ($data['web_slot_id'] ?? '')),
            'mobile_app_id' => trim((string) ($data['mobile_app_id'] ?? '')),
            'mobile_banner_ad_unit_id' => trim((string) ($data['mobile_banner_ad_unit_id'] ?? '')),
        ];

        if ($settings['enabled']) {
            foreach (['web_publisher_id', 'web_slot_id', 'mobile_app_id', 'mobile_banner_ad_unit_id'] as $required) {
                abort_if($settings[$required] === '', 422, 'Complete all AdSense and AdMob IDs before enabling ads.');
            }
        }

        DB::table('marketpg_settings')->updateOrInsert(
            ['key' => 'ads'],
            ['value' => json_encode($settings, JSON_THROW_ON_ERROR), 'updated_at' => now(), 'created_at' => now()]
        );

        return back()->with('status', $settings['enabled'] ? 'Google advertising settings saved and enabled.' : 'Google advertising has been disabled.');
    }

    private function carouselSettings(): array
    {
        $raw = DB::table('marketpg_settings')->where('key', 'home_carousel')->value('value');
        $items = is_string($raw) ? json_decode($raw, true) : [];
        return is_array($items) ? array_values($items) : [];
    }

    public function addCarouselItem(Request $request)
    {
        $data = $request->validate([
            'image_url' => ['nullable', 'url', 'regex:/^https:\/\//i', 'max:2048'],
            'link_url' => ['nullable', 'url', 'regex:/^https:\/\//i', 'max:2048'],
            'image' => ['nullable', 'image', 'mimes:jpg,jpeg,png,webp', 'max:2048'],
            'alt_text' => ['nullable', 'string', 'max:160'],
        ]);
        abort_unless(($request->filled('image_url') xor $request->hasFile('image')), 422, 'Provide either an image URL or upload one image.');
        $path = null;
        $imageUrl = trim((string) ($data['image_url'] ?? ''));
        if ($request->hasFile('image')) {
            abort_unless(config('marketpg.banner_uploads_enabled'), 503, 'Carousel image uploads require configured persistent public storage.');
            $path = $request->file('image')->store('home-carousel', config('marketpg.public_disk'));
            $imageUrl = Storage::disk(config('marketpg.public_disk'))->url($path);
        }
        $items = $this->carouselSettings();
        $items[] = [
            'id' => (string) \Illuminate\Support\Str::uuid(),
            'image_url' => $imageUrl,
            'storage_path' => $path,
            'link_url' => trim((string) ($data['link_url'] ?? '')),
            'alt_text' => trim((string) ($data['alt_text'] ?? 'MarketPG promotion')),
            'active' => true,
        ];
        DB::table('marketpg_settings')->updateOrInsert(['key' => 'home_carousel'], ['value' => json_encode($items, JSON_THROW_ON_ERROR), 'updated_at' => now(), 'created_at' => now()]);
        return back()->with('status', 'Home carousel slide added.');
    }

    public function deleteCarouselItem(Request $request, string $id)
    {
        $items = $this->carouselSettings();
        $removed = collect($items)->firstWhere('id', $id);
        abort_unless($removed, 404);
        $items = array_values(array_filter($items, fn ($item) => ($item['id'] ?? null) !== $id));
        DB::table('marketpg_settings')->updateOrInsert(['key' => 'home_carousel'], ['value' => json_encode($items, JSON_THROW_ON_ERROR), 'updated_at' => now(), 'created_at' => now()]);
        if (!empty($removed['storage_path'])) Storage::disk(config('marketpg.public_disk'))->delete($removed['storage_path']);
        return back()->with('status', 'Carousel slide removed.');
    }

    public function reviewSme(Request $request, User $user)
    {
        abort_unless($user->sme_status === 'pending' && filled($user->sme_tin), 409);
        $data = $request->validate([
            'decision' => ['required', Rule::in(['verified', 'rejected'])],
            'reason' => ['nullable', 'string', 'max:1000'],
        ]);
        abort_if($data['decision'] === 'rejected' && trim((string) ($data['reason'] ?? '')) === '', 422, 'A rejection reason is required.');
        $user->update(['sme_status' => $data['decision'], 'sme_review_note' => $data['reason'] ?? null, 'sme_reviewed_at' => now()]);
        return back()->with('status', 'SME verification status updated.');
    }

    public function logout(Request $request)
    {
        Auth::logout();
        $request->session()->invalidate();
        $request->session()->regenerateToken();
        return redirect()->route('admin.login');
    }

    public function reviewListing(Request $request, Listing $listing)
    {
        abort_unless($listing->status === 'pending', 409);
        $data = $request->validate([
            'decision' => ['required', Rule::in(['active', 'rejected'])],
            'reason' => ['nullable', 'string', 'max:1000'],
        ]);
        $listing->update(['status' => $data['decision'], 'moderation_note' => $data['reason'] ?? null]);
        return back()->with('status', 'Listing moderation decision saved.');
    }


    public function releasePayout(Request $request, Order $order)
    {
        abort_unless($order->status === 'delivered' && $order->payout_status === 'pending', 409, 'Only delivered orders awaiting payout can be released.');
        $payoutDetails = $order->seller?->payout_account_details ?? [];
        abort_unless(!empty($payoutDetails['bank_name']) && !empty($payoutDetails['account_name']) && !empty($payoutDetails['account_number']), 409, 'Seller payout bank details are incomplete.');
        $data = $request->validate(['payout_reference' => ['required', 'string', 'max:120']]);
        $order->update(['payout_status' => 'released', 'released_at' => now(), 'payout_reference' => $data['payout_reference']]);
        return back()->with('status', 'Payout release recorded. Verify the actual bank transfer against the reference before closing the order.');
    }

    public function reviewOrder(Request $request, Order $order)
    {
        $data = $request->validate([
            'status' => ['required', Rule::in(['paid', 'cancelled', 'disputed'])],
            'admin_note' => ['nullable', 'string', 'max:1000'],
        ]);
        abort_unless(in_array($order->status, ['awaiting_payment', 'paid'], true), 409);

        DB::transaction(function () use ($order, $data) {
            $locked = Order::whereKey($order->id)->lockForUpdate()->firstOrFail();
            $updates = ['status' => $data['status'], 'admin_note' => $data['admin_note'] ?? null];
            if ($data['status'] === 'paid') $updates['paid_at'] = $locked->paid_at ?? now();
            if ($data['status'] === 'cancelled') {
                $updates['cancelled_at'] = now();
                Listing::whereKey($locked->listing_id)->where('status', 'reserved')->update(['status' => 'active']);
            }
            $locked->update($updates);
        });

        return back()->with('status', 'Order status updated.');
    }

    public function reviewReceipt(Request $request, PaymentReceipt $receipt)
    {
        abort_unless($receipt->status === 'pending', 409);
        $data = $request->validate([
            'decision' => ['required', Rule::in(['approved', 'rejected'])],
            'notes' => ['nullable', 'string', 'max:1000'],
        ]);
        if ($data['decision'] === 'rejected') {
            abort_if(trim((string) ($data['notes'] ?? '')) === '', 422, 'A rejection reason is required.');
        }

        DB::transaction(function () use ($receipt, $data) {
            $locked = PaymentReceipt::whereKey($receipt->id)->lockForUpdate()->firstOrFail();
            $locked->update(['status' => $data['decision'], 'notes' => $data['notes'] ?? null]);
            if ($locked->order_id && $data['decision'] === 'approved') {
                $order = Order::whereKey($locked->order_id)->lockForUpdate()->first();
                if ($order && $order->status === 'awaiting_payment') $order->update(['status' => 'paid', 'paid_at' => now()]);
            }
        });

        return back()->with('status', 'Payment receipt review saved.');
    }


    public function resolveDispute(Request $request, OrderDispute $dispute)
    {
        abort_unless($dispute->status === 'open', 409);
        $data = $request->validate([
            'decision' => ['required', Rule::in(['resolved', 'rejected'])],
            'resolution' => ['required', 'string', 'min:10', 'max:2000'],
        ]);

        DB::transaction(function () use ($request, $dispute, $data) {
            $locked = OrderDispute::whereKey($dispute->id)->lockForUpdate()->firstOrFail();
            abort_unless($locked->status === 'open', 409);
            $locked->update([
                'status' => $data['decision'],
                'resolution' => $data['resolution'],
                'resolved_by' => Auth::id(),
                'resolved_at' => now(),
            ]);
            Order::whereKey($locked->order_id)->where('status', 'disputed')
                ->update(['status' => $locked->previous_order_status]);
        });

        return back()->with('status', 'Dispute decision saved.');
    }


    public function receiptDocument(PaymentReceipt $receipt)
    {
        $disk = config('marketpg.private_disk');
        abort_unless(Storage::disk($disk)->exists($receipt->file_path), 404);
        $mime = Storage::disk($disk)->mimeType($receipt->file_path);
        abort_unless(in_array($mime, ['image/jpeg', 'image/png', 'application/pdf'], true), 404);
        return Storage::disk($disk)->response($receipt->file_path, null, [
            'Cache-Control' => 'private, no-store, max-age=0',
            'Pragma' => 'no-cache',
            'X-Content-Type-Options' => 'nosniff',
            'Content-Security-Policy' => "default-src 'none'; sandbox",
        ], 'inline');
    }

    public function identityDocument(IdentityVerification $verification, string $kind)
    {
        abort_unless(in_array($kind, ['front', 'back', 'selfie'], true), 404);

        $path = match ($kind) {
            'front' => $verification->front_path,
            'back' => $verification->back_path,
            'selfie' => $verification->selfie_path,
        };

        abort_unless($path && Storage::disk(config('marketpg.private_disk'))->exists($path), 404);
        $mime = Storage::disk(config('marketpg.private_disk'))->mimeType($path);
        abort_unless(in_array($mime, ['image/jpeg', 'image/png', 'application/pdf'], true), 404);

        return Storage::disk(config('marketpg.private_disk'))->response($path, null, [
            'Cache-Control' => 'private, no-store, max-age=0',
            'Pragma' => 'no-cache',
            'X-Content-Type-Options' => 'nosniff',
            'Content-Security-Policy' => "default-src 'none'; sandbox",
        ], 'inline');
    }

    public function reviewIdentity(Request $request, IdentityVerification $verification)
    {
        abort_unless($verification->status === 'pending', 409);
        $data = $request->validate([
            'decision' => ['required', Rule::in(['approved', 'rejected'])],
            'reason' => ['nullable', 'string', 'max:1000'],
        ]);
        if ($data['decision'] === 'rejected') {
            abort_if(trim((string) ($data['reason'] ?? '')) === '', 422, 'A rejection reason is required.');
        }

        $verification->update([
            'status' => $data['decision'],
            'reviewed_by' => Auth::id(),
            'reviewed_at' => now(),
            'rejection_reason' => $data['reason'] ?? null,
        ]);

        return back()->with('status', 'Identity verification decision saved.');
    }
}
