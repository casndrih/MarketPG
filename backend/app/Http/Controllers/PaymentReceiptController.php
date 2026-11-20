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

    public function document(PaymentReceipt $receipt)
    {
        abort_unless(Storage::disk('local')->exists($receipt->file_path), 404);
        return Storage::disk('local')->download($receipt->file_path);
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
