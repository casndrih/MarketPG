<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Order extends Model
{
    protected $fillable = [
        'order_number', 'listing_id', 'buyer_id', 'seller_id', 'amount',
        'currency', 'payment_method', 'status', 'buyer_note', 'admin_note', 'paid_at',
        'completed_at', 'cancelled_at',
    ];

    protected $casts = [
        'amount' => 'decimal:2',
        'paid_at' => 'datetime',
        'completed_at' => 'datetime',
        'cancelled_at' => 'datetime',
    ];

    public function listing(): BelongsTo { return $this->belongsTo(Listing::class); }
    public function buyer(): BelongsTo { return $this->belongsTo(User::class, 'buyer_id'); }
    public function seller(): BelongsTo { return $this->belongsTo(User::class, 'seller_id'); }
    public function receipts(): HasMany { return $this->hasMany(PaymentReceipt::class); }
}
