<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class OrderDispute extends Model
{
    protected $fillable = [
        'order_id', 'raised_by', 'reason', 'details', 'status',
        'previous_order_status', 'resolution', 'resolved_by', 'resolved_at',
    ];

    protected $casts = ['resolved_at' => 'datetime'];

    public function order(): BelongsTo { return $this->belongsTo(Order::class); }
    public function raiser(): BelongsTo { return $this->belongsTo(User::class, 'raised_by'); }
    public function resolver(): BelongsTo { return $this->belongsTo(User::class, 'resolved_by'); }
}
