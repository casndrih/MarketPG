<?php

namespace App\\Http\\Controllers;

use App\\Models\\User;
use Illuminate\\Http\\Request;
use Illuminate\\Support\\Facades\\Auth;
use Illuminate\\Support\\Facades\\Hash;
use Illuminate\\Validation\\Rules\\Password;

class WebAuthController extends Controller
{
    public function login(Request $request)
    {
        $credentials = $request->validate([
            'email' => ['required', 'email'],
            'password' => ['required', 'string'],
        ]);
        if (!Auth::attempt(['email' => strtolower($credentials['email']), 'password' => $credentials['password']])) {
            return back()->withErrors(['email' => 'The email address or password is incorrect.'])->onlyInput('email');
        }
        $request->session()->regenerate();
        return redirect()->intended(route('home'));
    }

    public function register(Request $request)
    {
        $data = $request->validate([
            'name' => ['required', 'string', 'max:120'],
            'email' => ['required', 'email', 'max:255', 'unique:users,email'],
            'password' => ['required', 'confirmed', Password::min(10)->letters()->numbers()],
            'account_type' => ['required', 'in:buyer,seller'],
        ]);
        $user = User::create([
            'name' => trim($data['name']),
            'email' => strtolower($data['email']),
            'password' => Hash::make($data['password']),
            'account_type' => $data['account_type'],
        ]);
        Auth::login($user);
        $request->session()->regenerate();
        return redirect()->route('home')->with('status', 'Your MarketPG account is ready. Complete your profile photos to unlock selling features.');
    }

    public function logout(Request $request)
    {
        Auth::guard('web')->logout();
        $request->session()->invalidate();
        $request->session()->regenerateToken();
        return redirect()->route('home');
    }
}
