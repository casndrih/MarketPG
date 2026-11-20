<?php

namespace App\Http\Controllers;

use App\Models\Listing;
use App\Models\PaymentReceipt;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Schema;

class MarketplaceController extends Controller
{
    public function categories()
    {
        return response()->json(['data' => ['Electronics','Phones & Tablets','Vehicles','Furniture','Home & Living','Fashion','Sports','Other']]);
    }

    public function listings(Request $request)
    {
        $query = Listing::query()->where('status', 'active')->latest();
        if ($request->filled('q')) $query->where('title', 'ilike', '%'.$request->string('q').'%');
        if ($request->filled('category_id')) $query->where('category_id', $request->integer('category_id'));
        if ($request->filled('location')) $query->where('location', 'ilike', '%'.$request->string('location').'%');
        return response()->json($query->paginate(20));
    }

    public function show(Listing $listing)
    {
        abort_unless($listing->status === 'active', 404);
        return response()->json(['data' => $listing->load('user:id,name')]);
    }

    public function store(Request $request)
    {
        $data = $request->validate([
            'title' => 'required|string|max:120', 'description' => 'required|string|max:5000',
            'price' => 'required|numeric|min:0', 'condition' => 'required|in:new,used,refurbished',
            'location' => 'required|string|max:120', 'category_id' => 'nullable|integer',
            'image' => 'nullable|image|max:5120',
        ]);
        if ($request->hasFile('image')) $data['image_path'] = $request->file('image')->store('listings','public');
        unset($data['image']);
        $data['user_id'] = $request->user()->id;
        $data['status'] = 'pending';
        return response()->json(['data' => Listing::create($data)], 201);
    }

    public function uploadReceipt(Request $request, Listing $listing)
    {
        $data = $request->validate([
            'receipt' => 'required|file|mimes:jpg,jpeg,png,pdf|max:10240',
            'order_reference' => 'required|string|max:80',
        ]);
        $path = $request->file('receipt')->store('payment-receipts','local');
        $receipt = PaymentReceipt::create([
            'user_id' => $request->user()->id, 'listing_id' => $listing->id,
            'order_reference' => $data['order_reference'], 'file_path' => $path, 'status' => 'pending',
        ]);
        return response()->json(['message' => 'Receipt submitted for admin verification.', 'data' => $receipt], 201);
    }
}