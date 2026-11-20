<?php

namespace App\Http\Controllers;

use App\Models\IdentityVerification;
use App\Models\Listing;
use App\Models\Order;
use App\Models\OrderDispute;
use App\Models\PaymentReceipt;
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
            'counts' => [
                'users' => \App\Models\User::count(),
                'active_listings' => Listing::where('status', 'active')->count(),
                'pending_listings' => Listing::where('status', 'pending')->count(),
                'orders' => Order::count(),
            ],
        ]);
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
