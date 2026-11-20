<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>MarketPG Admin</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="min-h-screen bg-slate-100 text-slate-900">
<header class="bg-slate-950 text-white">
    <div class="mx-auto flex max-w-7xl items-center justify-between px-5 py-5">
        <div><a href="/" class="text-2xl font-black text-lime-400">MarketPG</a><span class="ml-3 text-slate-400">Administration</span></div>
        <form method="POST" action="{{ route('admin.logout') }}">@csrf<button class="rounded-lg border border-slate-700 px-4 py-2 text-sm">Sign out</button></form>
    </div>
</header>
<main class="mx-auto max-w-7xl space-y-8 px-5 py-8">
    <div>
        <h1 class="text-3xl font-black">Admin dashboard</h1>
        <p class="mt-1 text-slate-500">Review listings, payments, orders and identity checks.</p>
    </div>
    @if (session('status'))
        <div class="rounded-xl border border-lime-300 bg-lime-50 px-4 py-3 text-lime-900">{{ session('status') }}</div>
    @endif
    <section class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        @foreach (['Users' => $counts['users'], 'Active listings' => $counts['active_listings'], 'Pending listings' => $counts['pending_listings'], 'Orders' => $counts['orders']] as $label => $value)
            <div class="rounded-2xl bg-white p-5 shadow-sm"><p class="text-sm text-slate-500">{{ $label }}</p><p class="mt-2 text-3xl font-black">{{ number_format($value) }}</p></div>
        @endforeach
    </section>

    <section class="rounded-2xl bg-white p-5 shadow-sm">
        <div class="mb-4 flex items-center justify-between"><h2 class="text-xl font-bold">Pending listings</h2><span class="text-sm text-slate-500">{{ $pendingListings->count() }} awaiting review</span></div>
        @forelse ($pendingListings as $listing)
            <div class="grid gap-4 border-t py-4 lg:grid-cols-[1fr_360px]">
                <div><h3 class="font-bold">{{ $listing->title }}</h3><p class="mt-1 text-sm text-slate-500">K {{ number_format($listing->price, 2) }} · {{ $listing->location }} · {{ $listing->condition }}</p><p class="mt-2 text-sm">{{ $listing->description }}</p><p class="mt-2 text-xs text-slate-500">Seller: {{ $listing->user?->name }} ({{ $listing->user?->email }})</p></div>
                <form method="POST" action="{{ route('admin.listings.review', $listing) }}" class="space-y-2">@csrf
                    <textarea name="reason" rows="2" placeholder="Optional moderation note" class="w-full rounded-lg border p-2 text-sm"></textarea>
                    <div class="flex gap-2"><button name="decision" value="active" class="flex-1 rounded-lg bg-lime-500 px-3 py-2 text-sm font-bold">Approve</button><button name="decision" value="rejected" class="flex-1 rounded-lg bg-red-100 px-3 py-2 text-sm font-bold text-red-800">Reject</button></div>
                </form>
            </div>
        @empty <p class="border-t py-5 text-sm text-slate-500">No listings are waiting for moderation.</p> @endforelse
    </section>

    <section class="rounded-2xl bg-white p-5 shadow-sm">
        <div class="mb-4 flex items-center justify-between"><h2 class="text-xl font-bold">Orders</h2><span class="text-sm text-slate-500">Latest 40</span></div>
        @forelse ($orders as $order)
            <div class="grid gap-3 border-t py-4 lg:grid-cols-[1fr_360px]">
                <div><h3 class="font-bold">{{ $order->order_number }} · {{ $order->listing?->title }}</h3><p class="mt-1 text-sm text-slate-500">K {{ number_format($order->amount, 2) }} {{ $order->currency }} · {{ str_replace('_', ' ', $order->status) }} · {{ $order->payment_method }}</p><p class="mt-1 text-xs text-slate-500">Buyer: {{ $order->buyer?->email }} · Seller: {{ $order->seller?->email }}</p></div>
                <form method="POST" action="{{ route('admin.orders.review', $order) }}" class="space-y-2">@csrf
                    <select name="status" class="w-full rounded-lg border p-2 text-sm"><option value="paid">Mark paid</option><option value="cancelled">Cancel</option><option value="disputed">Mark disputed</option></select>
                    <input name="admin_note" placeholder="Admin note" class="w-full rounded-lg border p-2 text-sm">
                    <button class="w-full rounded-lg bg-slate-900 px-3 py-2 text-sm font-bold text-white">Save order status</button>
                </form>
            </div>
        @empty <p class="border-t py-5 text-sm text-slate-500">No orders yet.</p> @endforelse
    </section>

    <section class="rounded-2xl bg-white p-5 shadow-sm">
        <div class="mb-4 flex items-center justify-between"><h2 class="text-xl font-bold">Open disputes</h2><span class="text-sm text-slate-500">{{ $disputes->count() }} open</span></div>
        @forelse ($disputes as $dispute)
            <div class="grid gap-3 border-t py-4 lg:grid-cols-[1fr_360px]">
                <div><h3 class="font-bold">{{ $dispute->order?->order_number }} · {{ $dispute->order?->listing?->title }}</h3><p class="mt-1 text-sm text-slate-500">{{ str_replace('_', ' ', $dispute->reason) }} · Raised by {{ $dispute->raiser?->name }}</p><p class="mt-2 text-sm">{{ $dispute->details }}</p><p class="mt-1 text-xs text-slate-500">Buyer: {{ $dispute->order?->buyer?->email }} · Seller: {{ $dispute->order?->seller?->email }}</p></div>
                <form method="POST" action="{{ route('admin.disputes.resolve', $dispute) }}" class="space-y-2">@csrf
                    <textarea name="resolution" required minlength="10" rows="3" placeholder="Record the resolution or reason for rejection" class="w-full rounded-lg border p-2 text-sm"></textarea>
                    <div class="flex gap-2"><button name="decision" value="resolved" class="flex-1 rounded-lg bg-lime-500 px-3 py-2 text-sm font-bold">Resolve</button><button name="decision" value="rejected" class="flex-1 rounded-lg bg-red-100 px-3 py-2 text-sm font-bold text-red-800">Reject</button></div>
                </form>
            </div>
        @empty <p class="border-t py-5 text-sm text-slate-500">No open disputes.</p> @endforelse
    </section>

    <section class="rounded-2xl bg-white p-5 shadow-sm">
        <div class="mb-4 flex items-center justify-between"><h2 class="text-xl font-bold">Payment receipts</h2><span class="text-sm text-slate-500">{{ $pendingReceipts->count() }} pending</span></div>
        @forelse ($pendingReceipts as $receipt)
            <div class="grid gap-3 border-t py-4 lg:grid-cols-[1fr_360px]">
                <div><h3 class="font-bold">{{ $receipt->order?->order_number ?? $receipt->order_reference }}</h3><p class="mt-1 text-sm text-slate-500">{{ $receipt->user?->name }} · {{ $receipt->user?->email }}</p><p class="mt-1 text-sm">Listing: {{ $receipt->listing?->title }}</p><p class="mt-1 text-xs text-slate-500">Receipt file is in private storage; downloads remain disabled until persistent storage is configured.</p></div>
                <form method="POST" action="{{ route('admin.receipts.review', $receipt) }}" class="space-y-2">@csrf
                    <input name="notes" placeholder="Required if rejecting" class="w-full rounded-lg border p-2 text-sm">
                    <div class="flex gap-2"><button name="decision" value="approved" class="flex-1 rounded-lg bg-lime-500 px-3 py-2 text-sm font-bold">Approve</button><button name="decision" value="rejected" class="flex-1 rounded-lg bg-red-100 px-3 py-2 text-sm font-bold text-red-800">Reject</button></div>
                </form>
            </div>
        @empty <p class="border-t py-5 text-sm text-slate-500">No pending payment receipts.</p> @endforelse
    </section>

    <section class="rounded-2xl bg-white p-5 shadow-sm">
        <div class="mb-4 flex items-center justify-between"><h2 class="text-xl font-bold">Identity verification</h2><span class="text-sm text-slate-500">{{ $pendingVerifications->count() }} pending</span></div>
        <p class="mb-4 rounded-lg bg-amber-50 p-3 text-sm text-amber-900">New identity uploads are disabled until persistent encrypted storage and the privacy review are completed.</p>
        @forelse ($pendingVerifications as $verification)
            <div class="grid gap-3 border-t py-4 lg:grid-cols-[1fr_360px]">
                <div><h3 class="font-bold">{{ $verification->user?->name }}</h3><p class="mt-1 text-sm text-slate-500">{{ $verification->user?->email }} · {{ $verification->document_type }}</p><p class="mt-1 text-xs text-slate-500">Document ending {{ $verification->document_last_four ?: '—' }}</p>
                    <div class="mt-3 flex flex-wrap gap-3 text-sm font-semibold">
                        @foreach (['front' => 'View ID front', 'back' => 'View ID back', 'selfie' => 'View selfie'] as $kind => $label)
                            @if ($kind !== 'back' || $verification->back_path)
                                @if (($kind === 'front' && $verification->front_path) || ($kind === 'back' && $verification->back_path) || ($kind === 'selfie' && $verification->selfie_path))
                                    <a class="text-lime-700 underline" href="{{ route('admin.identity.document', [$verification, $kind]) }}" target="_blank" rel="noopener noreferrer">{{ $label }}</a>
                                @endif
                            @endif
                        @endforeach
                    </div>
                </div>
                <form method="POST" action="{{ route('admin.identity.review', $verification) }}" class="space-y-2">@csrf
                    <input name="reason" placeholder="Required if rejecting" class="w-full rounded-lg border p-2 text-sm">
                    <div class="flex gap-2"><button name="decision" value="approved" class="flex-1 rounded-lg bg-lime-500 px-3 py-2 text-sm font-bold">Approve</button><button name="decision" value="rejected" class="flex-1 rounded-lg bg-red-100 px-3 py-2 text-sm font-bold text-red-800">Reject</button></div>
                </form>
            </div>
        @empty <p class="border-t py-5 text-sm text-slate-500">No pending identity checks.</p> @endforelse
    </section>
</main>
</body>
</html>
