<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

class ProfileController extends Controller
{
    public function show(Request $request)
    {
        return response()->json(['user' => $request->user()]);
    }

    public function update(Request $request)
    {
        $data = $request->validate([
            'name' => ['required', 'string', 'max:120'],
            'phone' => ['nullable', 'string', 'max:40'],
            'location' => ['nullable', 'string', 'max:120'],\n            'town' => ['nullable', 'string', 'max:100'],\n            'province' => ['nullable', 'string', 'max:100'],
            'account_type' => ['required', Rule::in(['buyer', 'seller', 'both'])],
        ]);

        $request->user()->update($data);

        return response()->json(['message' => 'Profile updated.', 'user' => $request->user()->fresh()]);
    }
}
