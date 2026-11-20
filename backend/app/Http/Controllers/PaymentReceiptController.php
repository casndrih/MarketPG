<?php

namespace App\Http\Controllers;

use App\Models\Order;
use App\Models\PaymentReceipt;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;
use Illuminate\Validation\Rule;

class PaymentReceiptController extends Controller
{
    public function index()
    {
        return response()->json([
            'data' => PaymentReceipt::with([
                'user:id,name,email',
                'listing:id,title',
                'order:id,order_number,amount,currency,status',
            ])->latest()->paginate(30),
        ]);
    }

    public function upload(Request $request, Order $order)
    {
        abort_unless(config('marketpg.private_uploads_enabled'), 503, 'Receipt uploads are temporarily unavailable until secure persistent storage is configured.');
        abort_unless($order->buyer_id === $request->user()->id, 403);
        abort_unless($order->payment_method === 'bank_transfer' && $order->status === 'awaiting_payment', 409, 'This order is not awaiting payment.');
        abort_if($order->receipts()->whereIn('status', ['pending', 'approved'])->exists(), 409, 'A payment receipt has already been submitted.');
        $data = $request->validate(['receipt' => ['required', 'file', 'mimes:jpg,jpeg,png,pdf', 'max:10240']]);
        $disk = config('marketpg.private_disk');
        $path = $request->file('receipt')->store('payment-receipts', $disk);
        $receipt = PaymentReceipt::create(['user_id' => $request->user()->id, 'listing_id' => $order->listing_id, 'order_id' => $order->id, 'order_reference' => $order->order_number, 'file_path' => $path, 'status' => 'pending']);
        return response()->json(['message' => 'Receipt submitted for MarketPG payment verification.', 'data' => $receipt], 201);
    }

    public function document(PaymentReceipt $receipt)
    {
        $disk = config('marketpg.private_disk');
        abort_unless(Storage::disk($disk)->exists($receipt->file_path), 404);
        return Storage::disk($disk)->download($receipt->file_path);
    }

    public function review(Request $request, PaymentReceipt $receipt)
    {
        abort_unless($receipt->status === 'pending', 409, 'Only pending receipts can be reviewed.');
        $data = $request->validate([
            'decision' => ['required', Rule::in(['approved', 'rejected'])],
            'notes' => ['nullable', 'string', 'max:1000'],
        ]);
        if ($data['decision'] === 'rejected') {
            abort_if(trim((string) ($data['notes'] ?? '')) === '', 422, 'A rejection reason is required.');
        }

        DB::transaction(function () use ($receipt, $data) {
            $lockedReceipt = PaymentReceipt::whereKey($receipt->id)->lockForUpdate()->firstOrFail();
            abort_unless($lockedReceipt->status === 'pending', 409);
            $lockedReceipt->update([
                'status' => $data['decision'],
                'notes' => $data['notes'] ?? null,
            ]);

            if ($lockedReceipt->order_id) {
                $order = Order::whereKey($lockedReceipt->order_id)->lockForUpdate()->first();
                if ($order && $order->status === 'awaiting_payment' && $data['decision'] === 'approved') {
                    $order->update(['status' => 'paid', 'paid_at' => now()]);
                }
            }
        });

        return response()->json(['message' => 'Receipt review saved.', 'data' => $receipt->fresh()]);
    }
}
