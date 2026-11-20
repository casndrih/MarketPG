<?php

namespace App\Http\Controllers;

use App\Models\Listing;
use App\Models\Message;
use App\Models\Order;
use App\Models\User;
use Illuminate\Http\Request;

class MessageController extends Controller
{
    public function index(Request $request)
    {
        $messages = Message::with([
            'listing:id,title,image_path',
            'sender:id,name',
            'recipient:id,name',
        ])->where(fn ($q) => $q->where('sender_id', $request->user()->id)
            ->orWhere('recipient_id', $request->user()->id))
            ->latest()->paginate(40);

        return response()->json($messages);
    }

    public function thread(Request $request, Listing $listing, User $participant)
    {
        abort_unless($participant->id !== $request->user()->id, 404);
        abort_unless(
            $listing->user_id === $request->user()->id ||
            $listing->user_id === $participant->id,
            403
        );

        $messages = Message::with(['sender:id,name', 'recipient:id,name'])
            ->where('listing_id', $listing->id)
            ->where(fn ($q) => $q->where(function ($inner) use ($request, $participant) {
                $inner->where('sender_id', $request->user()->id)->where('recipient_id', $participant->id);
            })->orWhere(function ($inner) use ($request, $participant) {
                $inner->where('sender_id', $participant->id)->where('recipient_id', $request->user()->id);
            }))
            ->oldest()->paginate(100);

        Message::where('listing_id', $listing->id)
            ->where('sender_id', $participant->id)
            ->where('recipient_id', $request->user()->id)
            ->whereNull('read_at')->update(['read_at' => now()]);

        return response()->json([
            'listing' => ['id' => $listing->id, 'title' => $listing->title, 'price' => $listing->price, 'image_path' => $listing->image_path],
            'participant' => ['id' => $participant->id, 'name' => $participant->name],
            'data' => $messages,
        ]);
    }

    public function store(Request $request, Listing $listing)
    {
        $data = $request->validate([
            'recipient_id' => ['required', 'integer', 'exists:users,id'],
            'body' => ['required', 'string', 'max:4000'],
        ]);

        $sender = $request->user();
        $recipient = User::findOrFail($data['recipient_id']);
        abort_if($sender->id === $recipient->id, 422, 'You cannot message yourself.');

        if ($sender->id === $listing->user_id) {
            $hasConversation = Message::where('listing_id', $listing->id)
                ->where('sender_id', $recipient->id)
                ->where('recipient_id', $sender->id)->exists();
            $hasOrder = Order::where('listing_id', $listing->id)
                ->where(fn ($q) => $q->where('buyer_id', $recipient->id)->where('seller_id', $sender->id))->exists();
            abort_unless($hasConversation || $hasOrder, 403, 'You can only reply to an existing buyer conversation.');
        } else {
            abort_unless($recipient->id === $listing->user_id, 422, 'Messages must be sent to the listing seller.');
            abort_unless(in_array($listing->status, ['active', 'reserved'], true), 409, 'This listing is not accepting messages.');
        }

        $message = Message::create([
            'listing_id' => $listing->id,
            'sender_id' => $sender->id,
            'recipient_id' => $recipient->id,
            'body' => trim($data['body']),
        ]);

        return response()->json([
            'message' => 'Message sent.',
            'data' => $message->load(['sender:id,name', 'recipient:id,name']),
        ], 201);
    }
}
