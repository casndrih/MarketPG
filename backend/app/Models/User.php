<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    protected $fillable = [
        'name',
        'email',
        'password',
        'phone',
        'location',
        'account_type',
        'town',
        'province',
        'profile_photo_path',
        'cover_photo_path',
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    public function completedSales()\n    {\n        return $this->hasMany(Order::class, 'seller_id')->where('status', 'completed');\n    }\n\n    public function getSellerRankAttribute(): string\n    {\n        $sales = array_key_exists('completed_sales_count', $this->attributes)\n            ? (int) $this->attributes['completed_sales_count']\n            : $this->completedSales()->count();\n        return match (true) {\n            $sales >= 100 => 'Platinum',\n            $sales >= 50 => 'Gold',\n            $sales >= 20 => 'Silver',\n            $sales >= 5 => 'Bronze',\n            default => 'New Seller',\n        };\n    }\n\n    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
        ];
    }
}
