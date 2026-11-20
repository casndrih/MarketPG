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

    public function completedSales()
    {
        return $this->hasMany(Order::class, 'seller_id')->where('status', 'completed');
    }

    public function getSellerRankAttribute(): string
    {
        $sales = array_key_exists('completed_sales_count', $this->attributes)
            ? (int) $this->attributes['completed_sales_count']
            : $this->completedSales()->count();
        return match (true) {
            $sales >= 100 => 'Platinum',
            $sales >= 50 => 'Gold',
            $sales >= 20 => 'Silver',
            $sales >= 5 => 'Bronze',
            default => 'New Seller',
        };
    }

    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
        ];
    }
}
