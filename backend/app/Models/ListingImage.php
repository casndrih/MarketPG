<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ListingImage extends Model
{
    protected $fillable = ['listing_id', 'image_path', 'position'];

    protected $appends = ['url'];

    public function getUrlAttribute(): string
    {
        return \\Illuminate\\Support\\Facades\\Storage::disk(config('marketpg.public_disk'))->url($this->image_path);
    }

    public function listing(): BelongsTo
    {
        return $this->belongsTo(Listing::class);
    }
}
