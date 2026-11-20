<?php

use Illuminate\Support\Facades\Route;
use App\Models\Listing;

Route::get('/', function () {
    $listings = Listing::query()->where('status', 'active')->latest()->take(12)->get();
    return view('marketplace', compact('listings'));
});
Route::view('/admin', 'admin.dashboard')->middleware(['web', 'auth']);