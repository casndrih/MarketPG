<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Listing extends Model
{
    protected $fillable = ['user_id','category_id','title','description','price','condition','location','image_path','status'];
    protected $casts = ['price' => 'decimal:2'];
}