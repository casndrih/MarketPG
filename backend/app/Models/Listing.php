<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Listing extends Model
{
    protected $fillable = ['user_id','category_id','title','description','price','condition','location','image_path','status','moderation_note'];
    protected $casts = ['price' => 'decimal:2'];

    public function images()\n    {\n        return $this->hasMany(ListingImage::class)->orderBy('position');\n    }\n\n    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}