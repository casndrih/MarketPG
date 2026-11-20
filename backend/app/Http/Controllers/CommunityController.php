<?php

namespace App\Http\Controllers;

use App\Models\Listing;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Validation\Rule;

class CommunityController extends Controller
{
    private const BLOCK_REASONS = [
        'harassment' => 'Harassment or abusive messages',
        'spam' => 'Spam or repeated unwanted contact',
        'fraud' => 'Suspected fraud or scam activity',
        'non_payment' => 'Repeated orders without payment',
        'misconduct' => 'Threatening or inappropriate conduct',
        'other' => 'Other policy concern',
    ];

    public function follow(Request $request, User $seller)
    {
        abort_unless(in_array($request->user()->account_type, ['buyer', 'both'], true), 403, 'Only buyer accounts can follow sellers.');
        abort_unless(in_array($seller->account_type, ['seller', 'both'], true), 422, 'This account is not a seller.');
        abort_if($seller->is($request->user()), 422, 'You cannot follow your own account.');
        DB::table('seller_follows')->insertOrIgnore([
            'buyer_id' => $request->user()->id, 'seller_id' => $seller->id,
            'created_at' => now(), 'updated_at' => now(),
        ]);
        return response()->json(['message' => 'Seller followed.', 'following' => true]);
    }

    public function unfollow(Request $request, User $seller)
    {
        DB::table('seller_follows')->where('buyer_id', $request->user()->id)->where('seller_id', $seller->id)->delete();
        return response()->json(['message' => 'Seller unfollowed.', 'following' => false]);
    }

    public function following(Request $request)
    {
        $ids = DB::table('seller_follows')->where('buyer_id', $request->user()->id)->pluck('seller_id');
        return response()->json(['data' => User::whereIn('id', $ids)->withCount('completedSales')->get()]);
    }

    public function blockBuyer(Request $request, User $buyer)
    {
        abort_unless(in_array($request->user()->account_type, ['seller', 'both'], true), 403, 'Only sellers can block buyers.');
        abort_unless(in_array($buyer->account_type, ['buyer', 'both'], true), 422, 'The selected account is not a buyer.');
        abort_if($buyer->is($request->user()), 422, 'You cannot block your own account.');
        $data = $request->validate([
            'reason' => ['required', Rule::in(array_keys(self::BLOCK_REASONS))],
            'details' => ['nullable', 'string', 'max:1000'],
        ]);
        DB::table('seller_blocks')->updateOrInsert(
            ['seller_id' => $request->user()->id, 'buyer_id' => $buyer->id],
            ['reason' => $data['reason'], 'details' => $data['details'] ?? null, 'updated_at' => now(), 'created_at' => now()]
        );
        return response()->json(['message' => 'Buyer blocked.', 'reason_label' => self::BLOCK_REASONS[$data['reason']]]);
    }

    public function unblockBuyer(Request $request, User $buyer)
    {
        DB::table('seller_blocks')->where('seller_id', $request->user()->id)->where('buyer_id', $buyer->id)->delete();
        return response()->json(['message' => 'Buyer unblocked.']);
    }

    public function blockReasons()
    {
        return response()->json(['data' => collect(self::BLOCK_REASONS)->map(fn ($label, $key) => ['value' => $key, 'label' => $label])->values()]);
    }

    public function tickets(Request $request)
    {
        $tickets = DB::table('support_tickets')->where('user_id', $request->user()->id)->latest('updated_at')->paginate(20);
        return response()->json($tickets);
    }

    public function createTicket(Request $request)
    {
        $data = $request->validate([
            'subject' => ['required', 'string', 'max:160'],
            'category' => ['required', Rule::in(['account', 'orders', 'payments', 'seller', 'buyer', 'suggestion', 'bug_report', 'technical', 'other'])],
            'priority' => ['nullable', Rule::in(['low', 'normal', 'high'])],
            'message' => ['required', 'string', 'min:10', 'max:5000'],
        ]);
        $ticketId = DB::transaction(function () use ($request, $data) {
            $id = DB::table('support_tickets')->insertGetId([
                'ticket_number' => 'MPG-' . now()->format('ymd') . '-' . Str::upper(Str::random(6)),
                'user_id' => $request->user()->id, 'subject' => $data['subject'],
                'category' => $data['category'], 'priority' => $data['priority'] ?? 'normal',
                'status' => 'open', 'last_reply_at' => now(), 'created_at' => now(), 'updated_at' => now(),
            ]);
            DB::table('support_ticket_messages')->insert([
                'ticket_id' => $id, 'user_id' => $request->user()->id,
                'body' => $data['message'], 'is_admin' => false, 'created_at' => now(), 'updated_at' => now(),
            ]);
            return $id;
        });
        return response()->json(['message' => 'Support ticket created.', 'data' => $this->ticketPayload($ticketId)], 201);
    }

    public function showTicket(Request $request, int $ticket)
    {
        $row = DB::table('support_tickets')->where('id', $ticket)->first();
        abort_unless($row && ((int) $row->user_id === (int) $request->user()->id || $this->isAdmin($request)), 404);
        return response()->json(['data' => $this->ticketPayload($ticket)]);
    }

    public function replyTicket(Request $request, int $ticket)
    {
        $row = DB::table('support_tickets')->where('id', $ticket)->first();
        abort_unless($row && ((int) $row->user_id === (int) $request->user()->id || $this->isAdmin($request)), 404);
        abort_unless($row->status !== 'closed', 409, 'This ticket is closed.');
        $data = $request->validate(['message' => ['required', 'string', 'min:2', 'max:5000']]);
        $admin = $this->isAdmin($request);
        DB::transaction(function () use ($ticket, $request, $data, $admin) {
            DB::table('support_ticket_messages')->insert([
                'ticket_id' => $ticket, 'user_id' => $request->user()->id,
                'body' => $data['message'], 'is_admin' => $admin, 'created_at' => now(), 'updated_at' => now(),
            ]);
            DB::table('support_tickets')->where('id', $ticket)->update([
                'status' => $admin ? 'waiting_user' : 'waiting_admin',
                'last_reply_at' => now(), 'updated_at' => now(),
            ]);
        });
        return response()->json(['message' => 'Reply sent.', 'data' => $this->ticketPayload($ticket)]);
    }

    public function adminTickets()
    {
        return response()->json(['data' => DB::table('support_tickets')->join('users', 'users.id', '=', 'support_tickets.user_id')
            ->select('support_tickets.*', 'users.name as user_name', 'users.email as user_email')
            ->orderByRaw("CASE WHEN support_tickets.status = 'closed' THEN 1 ELSE 0 END")
            ->latest('support_tickets.updated_at')->paginate(50)]);
    }

    public function adminUpdateTicket(Request $request, int $ticket)
    {
        $data = $request->validate(['status' => ['required', Rule::in(['open', 'waiting_user', 'waiting_admin', 'resolved', 'closed'])]]);
        abort_unless(DB::table('support_tickets')->where('id', $ticket)->exists(), 404);
        DB::table('support_tickets')->where('id', $ticket)->update(['status' => $data['status'], 'updated_at' => now()]);
        return response()->json(['message' => 'Ticket status updated.', 'data' => $this->ticketPayload($ticket)]);
    }

    private function ticketPayload(int $ticket): array
    {
        $row = (array) DB::table('support_tickets')->where('id', $ticket)->first();
        $row['messages'] = DB::table('support_ticket_messages')->join('users', 'users.id', '=', 'support_ticket_messages.user_id')
            ->where('ticket_id', $ticket)->select('support_ticket_messages.*', 'users.name as sender_name')->orderBy('support_ticket_messages.created_at')->get();
        return $row;
    }

    private function isAdmin(Request $request): bool
    {
        return in_array(strtolower((string) $request->user()->email), config('marketpg.admin_emails', []), true);
    }
}
