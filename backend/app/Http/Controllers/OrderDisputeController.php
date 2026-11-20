<?php

namespace App\Http\Controllers;

use App\Models\Order;
use App\Models\OrderDispute;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

class OrderDisputeController extends Controller
{
    public function index(Request $request)
    {
        return response()->json([
            'data' => OrderDispute::with(['order.listing:id,title', 'raiser:id,name'])
                ->whereHas('order', fn ($q) => $q->where('buyer_id', $request->user()->id)->orWhere('seller_id', $request->user()->id))
                ->latest()->paginate(20),
        ]);
    }

    public function store(Request $request, Order $order)
    {
        abort_unless(in_array($request->user()->id, [$order->buyer_id, $order->seller_id], true), 403);
        abort_unless(in_array($order->status, ['paid', 'completed'], true), 409, 'Only paid or completed orders can be disputed.');

        $data = $request->validate([
            'reason' => ['required', Rule::in(['item_not_received', 'not_as_described', 'damaged_item', 'payment_issue', 'other'])],
            'details' => ['required', 'string', 'min:20', 'max:4000'],
        ]);

        $dispute = DB::transaction(function () use ($request, $order, $data) {
            $locked = Order::whereKey($order->id)->lockForUpdate()->firstOrFail();
            abort_unless(in_array($locked->status, ['paid', 'completed'], true), 409);
            abort_if(OrderDispute::where('order_id', $locked->id)->exists(), 409, 'A dispute has already been raised for this order.');

            $dispute = OrderDispute::create([
                'order_id' => $locked->id,
                'raised_by' => $request->user()->id,
                'reason' => $data['reason'],
                'details' => $data['details'],
                'previous_order_status' => $locked->status,
                'status' => 'open',
            ]);
            $locked->update(['status' => 'disputed']);
            return $dispute;
        });

        return response()->json(['message' => 'Dispute submitted for administrator review.', 'data' => $dispute->load('order')], 201);
    }

    public function adminIndex()
    {
        return response()->json([
            'data' => OrderDispute::with(['order.listing:id,title', 'order.buyer:id,name,email', 'order.seller:id,name,email', 'raiser:id,name,email'])
                ->latest()->paginate(30),
        ]);
    }

    public function adminResolve(Request $request, OrderDispute $dispute)
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
                'resolved_by' => $request->user()->id,
                'resolved_at' => now(),
            ]);
            Order::whereKey($locked->order_id)->where('status', 'disputed')
                ->update(['status' => $locked->previous_order_status]);
        });

        return response()->json(['message' => 'Dispute decision saved.', 'data' => $dispute->fresh()]);
    }
}
