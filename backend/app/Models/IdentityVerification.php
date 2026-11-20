<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class IdentityVerification extends Model
{
    protected $fillable = [
        'user_id', 'document_type', 'document_last_four', 'front_path',
        'back_path', 'selfie_path', 'status', 'reviewed_by',
        'reviewed_at', 'rejection_reason',
    ];

    protected $casts = ['reviewed_at' => 'datetime'];

    protected $hidden = ['front_path', 'back_path', 'selfie_path', 'document_last_four'];

    public function user(): BelongsTo { return $this->belongsTo(User::class); }
    public function reviewer(): BelongsTo { return $this->belongsTo(User::class, 'reviewed_by'); }
}
