<?php

namespace App\Http\Controllers;

use App\Models\Listing;
use App\Models\Order;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Validation\Rule;

class OrderController extends Controller
{
    public function index(Request $request)
    {
        $user = $request->user();
        $orders = Order::with(['listing:id,title,image_path,location', 'buyer:id,name', 'seller:id,name'])
            ->where(fn ($q) => $q->where('buyer_id', $user->id)->orWhere('seller_id', $user->id))
            ->latest()->paginate(20);

        return response()->json($orders);
    }

    public function store(Request $request, Listing $listing)
    {
        $data = $request->validate([
            'buyer_note' => ['nullable', 'string', 'max:1000'],
        ]);

        $order = DB::transaction(function () use ($request, $listing, $data) {
            $locked = Listing::whereKey($listing->id)->lockForUpdate()->firstOrFail();
            abort_unless($locked->status === 'active', 409, 'This listing is no longer available.');
            abort_if($locked->user_id === $request->user()->id, 422, 'You cannot purchase your own listing.');

            $order = Order::create([
                'order_number' => 'MPG-' . strtoupper(Str::random(10)),
                'listing_id' => $locked->id,
                'buyer_id' => $request->user()->id,
                'seller_id' => $locked->user_id,
                'amount' => $locked->price,
                'currency' => 'PGK',
                'status' => 'awaiting_payment',
                'buyer_note' => $data['buyer_note'] ?? null,
            ]);

            $locked->update(['status' => 'reserved']);
            return $order;
        });

        return response()->json([
            'message' => 'Order created. Complete payment using the instructions provided by the seller or MarketPG support.',
            'data' => $order->load(['listing:id,title,price,image_path', 'seller:id,name']),
        ], 201);
    }

    public function show(Request $request, Order $order)
    {
        $this->authorizeParticipant($request, $order);
        return response()->json(['data' => $order->load([
            'listing:id,title,description,price,image_path,location',
            'buyer:id,name,email', 'seller:id,name,email', 'receipts:id,order_id,status,created_at',
        ])]);
    }

    public function cancel(Request $request, Order $order)
    {
        $this->authorizeParticipant($request, $order);
        abort_unless($order->buyer_id === $request->user()->id, 403);
        abort_unless($order->status === 'awaiting_payment', 409, 'Only unpaid orders can be cancelled.');

        DB::transaction(function () use ($order) {
            $locked = Order::whereKey($order->id)->lockForUpdate()->firstOrFail();
            abort_unless($locked->status === 'awaiting_payment', 409);
            $locked->update(['status' => 'cancelled', 'cancelled_at' => now()]);
            Listing::whereKey($locked->listing_id)->where('status', 'reserved')->update(['status' => 'active']);
        });

        return response()->json(['message' => 'Order cancelled.', 'data' => $order->fresh()]);
    }

    public function complete(Request $request, Order $order)
    {
        $this->authorizeParticipant($request, $order);
        abort_unless($order->seller_id === $request->user()->id, 403);
        abort_unless($order->status === 'paid', 409, 'The order must be marked paid by an administrator first.');

        $order->update(['status' => 'completed', 'completed_at' => now()]);
        Listing::whereKey($order->listing_id)->update(['status' => 'sold']);

        return response()->json(['message' => 'Order marked as completed.', 'data' => $order->fresh()]);
    }

    public function adminIndex()
    {
        return response()->json(['data' => Order::with(['listing:id,title', 'buyer:id,name,email', 'seller:id,name,email'])
            ->latest()->paginate(30)]);
    }

    public function adminUpdate(Request $request, Order $order)
    {
        $data = $request->validate([
            'status' => ['required', Rule::in(['paid', 'cancelled', 'disputed'])],
            'admin_note' => ['nullable', 'string', 'max:1000'],
        ]);

        abort_unless(in_array($order->status, ['awaiting_payment', 'paid'], true), 409);
        $updates = ['status' => $data['status'], 'admin_note' => $data['admin_note'] ?? null];
        if ($data['status'] === 'paid') $updates['paid_at'] = $order->paid_at ?? now();
        if ($data['status'] === 'cancelled') {
            $updates['cancelled_at'] = now();
            Listing::whereKey($order->listing_id)->where('status', 'reserved')->update(['status' => 'active']);
        }
        $order->update($updates);

        return response()->json(['message' => 'Order status updated.', 'data' => $order->fresh()]);
    }

    private function authorizeParticipant(Request $request, Order $order): void
    {
        abort_unless(in_array($request->user()->id, [$order->buyer_id, $order->seller_id], true), 403);
    }
}
