<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class PaymentReceipt extends Model
{
    protected $fillable = ['user_id','listing_id','order_id','order_reference','file_path','status','notes'];

    public function user(): BelongsTo { return $this->belongsTo(User::class); }
    public function listing(): BelongsTo { return $this->belongsTo(Listing::class); }
    public function order(): BelongsTo { return $this->belongsTo(Order::class); }
}
