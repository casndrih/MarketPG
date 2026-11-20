<?php

namespace App\Http\Controllers;

use App\Models\Listing;
use App\Models\PaymentReceipt;
use App\Models\Order;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Schema;

class MarketplaceController extends Controller
{
    public function categories()
    {
        return response()->json(['data' => [
            ['id' => 1, 'name' => 'Electronics', 'slug' => 'electronics'],
            ['id' => 2, 'name' => 'Phones & Tablets', 'slug' => 'phones-tablets'],
            ['id' => 3, 'name' => 'Vehicles', 'slug' => 'vehicles'],
            ['id' => 4, 'name' => 'Furniture', 'slug' => 'furniture'],
            ['id' => 5, 'name' => 'Home & Living', 'slug' => 'home-living'],
            ['id' => 6, 'name' => 'Fashion', 'slug' => 'fashion'],
            ['id' => 7, 'name' => 'Sports', 'slug' => 'sports'],
            ['id' => 8, 'name' => 'Other', 'slug' => 'other'],
        ]]);
    }

    public function listings(Request $request)
    {
        $request->validate([
            'min_price' => 'nullable|numeric|min:0',
            'max_price' => 'nullable|numeric|min:0',
            'condition' => 'nullable|in:new,used,refurbished',
            'sort' => 'nullable|in:price_asc,price_desc,oldest,newest',
        ]);
        $query = Listing::query()->with(['images', 'user' => fn ($user) => $user->withCount('completedSales')])->where('status', 'active')->latest();
        if ($request->filled('q')) $query->where('title', 'ilike', '%'.$request->string('q').'%');
        if ($request->filled('category_id')) $query->where('category_id', $request->integer('category_id'));
        if ($request->filled('location')) $query->where('location', 'ilike', '%'.$request->string('location').'%');
        if ($request->filled('condition')) $query->where('condition', $request->string('condition'));
        if ($request->filled('min_price')) $query->where('price', '>=', $request->input('min_price'));
        if ($request->filled('max_price')) $query->where('price', '<=', $request->input('max_price'));
        $sort = $request->string('sort')->toString();
        match ($sort) {
            'price_asc' => $query->orderBy('price'),
            'price_desc' => $query->orderByDesc('price'),
            'oldest' => $query->oldest(),
            default => $query->latest(),
        };
        return response()->json($query->paginate(20));
    }

    public function show(Listing $listing)
    {
        abort_unless($listing->status === 'active', 404);
        return response()->json(['data' => $listing->load(['images', 'user' => fn ($user) => $user->withCount('completedSales')])]);
    }

    public function store(Request $request)
    {
        abort_unless($request->user()->profile_photo_path && $request->user()->cover_photo_path, 403, 'Complete your profile photo and cover photo before publishing listings.');
        $data = $request->validate([
            'title' => 'required|string|max:120', 'description' => 'required|string|max:5000',
            'price' => 'required|numeric|min:0', 'condition' => 'required|in:new,used,refurbished',
            'location' => 'required|string|max:120', 'town' => 'required|string|max:100', 'province' => 'required|in:'.implode(',', config('marketpg.png_provinces')), 'category_id' => 'nullable|integer',
            'image' => 'nullable|image|max:5120',
            'images' => 'sometimes|array|max:10',
            'images.*' => 'image|mimes:jpg,jpeg,png,webp|max:5120',
        ]);
        if (isset($data['province'], $data['town'])) $data['location'] = trim($data['town'].', '.$data['province']);

        if ($request->hasFile('image') || $request->hasFile('images')) {
            abort_unless(config('marketpg.private_uploads_enabled'), 503, 'Listing image uploads are temporarily unavailable while persistent storage is being configured.');
            $data['image_path'] = $request->file('image')?->store('listings', config('marketpg.public_disk'));
        }
        $data['location'] = trim($data['town'].', '.$data['province']);
        unset($data['image'], $data['images']);
        $data['user_id'] = $request->user()->id;
        $data['status'] = 'pending';
        $listing = Listing::create($data);
        foreach ($request->file('images', []) as $position => $image) {
            $listing->images()->create(['image_path' => $image->store('listing-gallery', config('marketpg.public_disk')), 'position' => $position]);
        }
        return response()->json(['data' => $listing->load('images')], 201);
    }


    public function mine(Request $request)
    {
        return response()->json(Listing::with('images')->where('user_id', $request->user()->id)->latest()->paginate(20));
    }

    public function update(Request $request, Listing $listing)
    {
        abort_unless($listing->user_id === $request->user()->id, 403);
        abort_unless(in_array($listing->status, ['pending', 'rejected', 'active'], true), 409);

        $data = $request->validate([
            'title' => 'sometimes|required|string|max:120',
            'description' => 'sometimes|required|string|max:5000',
            'price' => 'sometimes|required|numeric|min:0',
            'condition' => 'sometimes|required|in:new,used,refurbished',
            'location' => 'sometimes|required|string|max:120',
            'town' => 'sometimes|required|string|max:100',
            'province' => 'sometimes|required|in:'.implode(',', config('marketpg.png_provinces')),
            'category_id => 'nullable|integer',
            'image' => 'nullable|image|max:5120',
            'images' => 'sometimes|array|max:10',
            'images.*' => 'image|mimes:jpg,jpeg,png,webp|max:5120',
        ]);

        if ($request->hasFile('image') || $request->hasFile('images')) {
            abort_unless(config('marketpg.private_uploads_enabled'), 503, 'Listing image uploads are temporarily unavailable while persistent storage is being configured.');
            if ($request->hasFile('image')) $data['image_path'] = $request->file('image')->store('listings', config('marketpg.public_disk'));
            unset($data['image'], $data['images']);
            foreach ($request->file('images', []) as $position => $image) {
                $listing->images()->create(['image_path' => $image->store('listing-gallery', config('marketpg.public_disk')), 'position' => $position]);
            }
        }

        if ($listing->status === 'active') $data['status'] = 'pending';
        $listing->update($data);

        return response()->json(['message' => 'Listing saved and submitted for moderation.', 'data' => $listing->fresh()]);
    }

    public function remove(Request $request, Listing $listing)
    {
        abort_unless($listing->user_id === $request->user()->id, 403);
        abort_if($listing->status === 'reserved' || $listing->status === 'sold', 409, 'Listings with an active or completed sale cannot be deleted.');
        $listing->delete();
        return response()->json(['message' => 'Listing deleted.']);
    }

    public function adminQueue()
    {
        return response()->json(['data' => Listing::with('user:id,name,email')
            ->where('status', 'pending')->latest()->paginate(30)]);
    }

    public function adminReview(Request $request, Listing $listing)
    {
        abort_unless($listing->status === 'pending', 409, 'Only pending listings can be reviewed.');
        $data = $request->validate([
            'decision' => 'required|in:active,rejected',
            'reason' => 'nullable|string|max:1000',
        ]);
        $listing->update(['status' => $data['decision'], 'moderation_note' => $data['reason'] ?? null]);
        return response()->json(['message' => 'Listing moderation decision saved.', 'data' => $listing->fresh()]);
    }


}