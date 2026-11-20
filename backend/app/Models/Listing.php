<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Listing extends Model
{
    protected $fillable = ['user_id','category_id','title','description','price','condition','location','town','province','image_path','status','moderation_note'];
    protected $casts = ['price' => 'decimal:2'];
    protected $appends = ['image_url'];

    public function getImageUrlAttribute(): ?string
    {
        return $this->image_path ? \\Illuminate\\Support\\Facades\\Storage::disk(config('marketpg.public_disk'))->url($this->image_path) : null;
    }

    public function images()
    {
        return $this->hasMany(ListingImage::class)->orderBy('position');
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}