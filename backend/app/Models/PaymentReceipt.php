<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class PaymentReceipt extends Model
{
    protected $fillable = ['user_id','listing_id','order_reference','file_path','status','notes'];
}