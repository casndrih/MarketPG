<?php

namespace App\Http\Controllers;

use App\Models\Listing;
use App\Models\Wishlist;
use Illuminate\Http\Request;

class WishlistController extends Controller
{
    public function index(Request $request)
    {
        return response()->json([
            'data' => Wishlist::with('listing.user:id,name')
                ->where('user_id', $request->user()->id)
                ->whereHas('listing', fn ($q) => $q->where('status', 'active'))
                ->latest()->paginate(30),
        ]);
    }

    public function store(Request $request, Listing $listing)
    {
        abort_unless($listing->status === 'active', 404);
        abort_if($listing->user_id === $request->user()->id, 422, 'You cannot save your own listing.');

        $item = Wishlist::firstOrCreate([
            'user_id' => $request->user()->id,
            'listing_id' => $listing->id,
        ]);

        return response()->json(['message' => 'Listing saved.', 'data' => $item->load('listing')], 201);
    }

    public function destroy(Request $request, Listing $listing)
    {
        Wishlist::where('user_id', $request->user()->id)
            ->where('listing_id', $listing->id)->delete();

        return response()->json(['message' => 'Listing removed from saved items.']);
    }
}
